package com.uitdrinkapp.app.ui.customer.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.uitdrinkapp.app.MainActivity;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.User;
import com.uitdrinkapp.app.data.repository.AuthRepository;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {
    private static final String PREFS = "vy_customer_profile_store";
    private static final String KEY_ADDRESS = "customer_address";
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AuthRepository auth = RepositoryProvider.get(requireContext()).auth();
        User user = auth.getCurrentUser();
        if (user == null) {
            goToLogin();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String savedAddress = prefs.getString(KEY_ADDRESS, "Thủ Đức, TP.HCM");

        binding.etProfileName.setText(user.fullName);
        binding.tvProfileHeaderName.setText(user.fullName);
        binding.etProfileEmail.setText(user.email);
        binding.etProfilePhone.setText(user.phone);
        binding.etProfileAddress.setText(savedAddress);

        binding.btnProfileSave.setOnClickListener(clicked -> {
            CharSequence nameValue = binding.etProfileName.getText();
            CharSequence phoneValue = binding.etProfilePhone.getText();
            CharSequence addressValue = binding.etProfileAddress.getText();
            String name = nameValue == null ? "" : nameValue.toString().trim();
            String phone = phoneValue == null ? "" : phoneValue.toString().trim();
            String address = addressValue == null ? "" : addressValue.toString().trim();

            if (name.isEmpty()) {
                binding.tilProfileName.setError(getString(R.string.vy_error_required));
                return;
            }
            binding.tilProfileName.setError(null);

            auth.updateProfile(name, phone);
            prefs.edit().putString(KEY_ADDRESS, address).apply();
            binding.tvProfileHeaderName.setText(name);

            ((MainActivity) requireActivity()).showMessage(R.string.vy_profile_saved);
        });

        binding.btnProfileLogout.setOnClickListener(clicked -> {
            auth.logout();
            goToLogin();
        });
    }

    private void goToLogin() {
        NavHostFragment.findNavController(this).navigate(R.id.loginFragment, null,
                new NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
