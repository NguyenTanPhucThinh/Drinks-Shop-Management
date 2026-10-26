package com.uitdrinkapp.app.ui.shop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.ViewOutlineProvider;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentJoinShopBinding;
import com.uitdrinkapp.app.domain.JoinShopService;
import com.uitdrinkapp.app.domain.ShopQrCode;
import com.uitdrinkapp.app.ui.home.HomeFragment;

public final class JoinShopFragment extends Fragment {
    private static final String STATE_CODE = "shop_code";
    private static final String STATE_TABLE = "table_number";
    private static final String STATE_PERMISSION = "requested_camera";
    private FragmentJoinShopBinding binding;
    private RepositoryProvider repositories;
    private ShopCameraScanner camera;
    private boolean submitting;
    private boolean requestedCamera;
    private boolean permissionInFlight;
    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                permissionInFlight = false;
                if (binding == null) return;
                if (granted) startCamera(); else showPermissionNeeded();
            });

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentJoinShopBinding.inflate(inflater, container, false);
        submitting = false;
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        repositories = RepositoryProvider.get(requireContext());
        binding.previewCamera.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                        getResources().getDimension(R.dimen.radius_12));
            }
        });
        binding.previewCamera.setClipToOutline(true);
        if (state != null) {
            binding.etShopCode.setText(state.getString(STATE_CODE, ""));
            binding.etTableNumber.setText(state.getString(STATE_TABLE, ""));
            requestedCamera = state.getBoolean(STATE_PERMISSION, false);
        }
        binding.btnBack.setOnClickListener(clicked -> {
            hideKeyboard();
            NavHostFragment.findNavController(this).popBackStack();
        });
        binding.btnJoin.setOnClickListener(clicked -> submit(false));
        binding.btnCameraPermission.setOnClickListener(clicked -> {
            if (hasCameraPermission()) startCamera();
            else if (requestedCamera && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + requireContext().getPackageName())));
            } else requestCameraPermission();
        });
        binding.etTableNumber.setOnEditorActionListener((text, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) { submit(false); return true; }
            return false;
        });
        if (!hasCameraPermission() && !requestedCamera) requestCameraPermission();
    }
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestCameraPermission() {
        if (permissionInFlight) return;
        requestedCamera = true;
        permissionInFlight = true;
        cameraPermission.launch(Manifest.permission.CAMERA);
    }
    private void showPermissionNeeded() {
        applyPermissionLayout();
        boolean settings = requestedCamera && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
        binding.tvCameraStatus.setText(settings ? R.string.join_camera_settings : R.string.join_camera_permission);
        binding.btnCameraPermission.setText(settings ? R.string.join_open_settings : R.string.join_enable_camera);
        binding.btnCameraPermission.setVisibility(View.VISIBLE);
    }
    private void applyPermissionLayout() {
        ViewGroup.LayoutParams cameraParams = binding.flCamera.getLayoutParams();
        int compactCameraSize = getResources().getDimensionPixelSize(R.dimen.join_scan_size_compact);
        cameraParams.width = compactCameraSize;
        cameraParams.height = compactCameraSize;
        binding.flCamera.setLayoutParams(cameraParams);

        ViewGroup.MarginLayoutParams instructionsParams =
                (ViewGroup.MarginLayoutParams) binding.tvJoinInstructions.getLayoutParams();
        instructionsParams.topMargin = getResources().getDimensionPixelSize(R.dimen.join_permission_instructions_margin);
        binding.tvJoinInstructions.setLayoutParams(instructionsParams);

        ViewGroup.MarginLayoutParams manualParams =
                (ViewGroup.MarginLayoutParams) binding.tvJoinManual.getLayoutParams();
        int manualMargin = getResources().getDimensionPixelSize(R.dimen.join_permission_manual_margin);
        manualParams.topMargin = manualMargin;
        manualParams.bottomMargin = manualMargin;
        binding.tvJoinManual.setLayoutParams(manualParams);
    }
    private void startCamera() {
        if (camera != null || binding == null || submitting || !hasCameraPermission()) return;
        binding.tvCameraStatus.setText(R.string.join_camera_starting);
        binding.btnCameraPermission.setVisibility(View.GONE);
        camera = new ShopCameraScanner(requireContext(), new ShopCameraScanner.Listener() {
            @Override public void onReady() {
                if (binding != null) binding.tvCameraStatus.setText(R.string.join_camera_scanning);
            }
            @Override public void onQrCode(String rawValue) {
                if (binding == null || submitting || !getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) return;
                ShopQrCode payload = ShopQrCode.parse(rawValue);
                if (payload == null || repositories.shops().findByCode(payload.getShopCode()) == null) {
                    binding.tvCameraStatus.setText(R.string.join_camera_unknown_qr);
                    return;
                }
                binding.etShopCode.setText(payload.getShopCode());
                if (payload.getTableNumber() != null) binding.etTableNumber.setText(payload.getTableNumber());
                submit(true);
            }
            @Override public void onCameraError() {
                if (binding == null) return;
                stopCamera();
                binding.tvCameraStatus.setText(R.string.join_camera_unavailable);
                binding.btnCameraPermission.setText(R.string.join_camera_retry);
                binding.btnCameraPermission.setVisibility(View.VISIBLE);
            }
            @Override public void onReadError() {
                if (binding != null) binding.tvCameraStatus.setText(R.string.join_camera_read_error);
            }
        });
        camera.start(getViewLifecycleOwner(), binding.previewCamera);
    }
    private void stopCamera() {
        if (camera != null) { camera.close(); camera = null; }
    }
    @Override public void onResume() {
        super.onResume();
        if (hasCameraPermission()) startCamera();
        else if (!permissionInFlight) showPermissionNeeded();
    }
    @Override public void onStop() {
        stopCamera();
        super.onStop();
    }
    private void submit(boolean scanned) {
        if (submitting) return;
        submitting = true;
        binding.btnJoin.setEnabled(false);
        stopCamera();
        new JoinShopService(repositories.shops(), repositories.sessions()).join(
                String.valueOf(binding.etShopCode.getText()), String.valueOf(binding.etTableNumber.getText()));
        hideKeyboard();
        NavController nav = NavHostFragment.findNavController(this);
        NavBackStackEntry home = nav.getPreviousBackStackEntry();
        if (home != null) home.getSavedStateHandle().set(HomeFragment.JOIN_RESULT,
                scanned ? R.string.join_qr_success : R.string.join_manual_success);
        nav.popBackStack();
    }
    private void hideKeyboard() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(requireActivity().getWindow(), binding.getRoot());
        controller.hide(WindowInsetsCompat.Type.ime());
        binding.getRoot().clearFocus();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PERMISSION, requestedCamera);
        if (binding != null) {
            outState.putString(STATE_CODE, String.valueOf(binding.etShopCode.getText()));
            outState.putString(STATE_TABLE, String.valueOf(binding.etTableNumber.getText()));
        }
    }
    @Override public void onDestroyView() {
        stopCamera();
        binding = null;
        super.onDestroyView();
    }
}
