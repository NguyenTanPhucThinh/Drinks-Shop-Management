package com.uitdrinkapp.app.ui.seller.profile;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.uitdrinkapp.app.MainActivity;
import com.uitdrinkapp.app.data.model.Shop;
import com.uitdrinkapp.app.data.model.User;
import com.uitdrinkapp.app.data.repository.AuthRepository;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.data.repository.ShopRepository;
import com.uitdrinkapp.app.databinding.FragmentSellerProfileBinding;

import java.util.Calendar;
import java.util.Locale;

public final class SellerProfileFragment extends Fragment {
    private FragmentSellerProfileBinding binding;
    private String selectedOpenTime = "07:00";
    private String selectedCloseTime = "22:00";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RepositoryProvider repo = RepositoryProvider.get(requireContext());
        AuthRepository auth = repo.auth();
        ShopRepository shopRepo = repo.shops();

        User user = auth.getCurrentUser();
        Shop shop = shopRepo.findById("abc");

        if (user != null) {
            binding.etSellerPhone.setText(user.phone);
            binding.etSellerEmail.setText(user.email);
        }

        if (shop != null) {
            binding.etSellerShopName.setText(shop.getName());
            binding.tvSellerHeaderShopName.setText(shop.getName());
            binding.etSellerAddress.setText(shop.getAddress());
            binding.etSellerHours.setText(shop.getHours());
            String[] parts = shop.getHours().split("-");
            if (parts.length == 2) {
                selectedOpenTime = parts[0].trim();
                selectedCloseTime = parts[1].trim();
            }
        }

        // Opening hours time picker dialog
        binding.etSellerHours.setOnClickListener(v -> showOpeningTimePicker());

        binding.btnSellerSaveProfile.setOnClickListener(v -> {
            String name = binding.etSellerShopName.getText().toString().trim();
            String phone = binding.etSellerPhone.getText().toString().trim();
            String address = binding.etSellerAddress.getText().toString().trim();
            String hours = binding.etSellerHours.getText().toString().trim();

            if (name.isEmpty()) {
                binding.etSellerShopName.setError("Không được để trống");
                return;
            }

            shopRepo.updateShopInfo(name, address, hours);
            auth.updateProfile(name, phone);
            binding.tvSellerHeaderShopName.setText(name);
            Toast.makeText(requireContext(), "Đã lưu hồ sơ quán", Toast.LENGTH_SHORT).show();
        });

        binding.btnSellerLogout.setOnClickListener(v -> {
            auth.logout();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void showOpeningTimePicker() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog openPicker = new TimePickerDialog(requireContext(), (view, hourOfDay, minuteOfHour) -> {
            selectedOpenTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            showClosingTimePicker();
        }, hour, minute, true);
        openPicker.setTitle("Chọn giờ mở cửa");
        openPicker.show();
    }

    private void showClosingTimePicker() {
        TimePickerDialog closePicker = new TimePickerDialog(requireContext(), (view, hourOfDay, minuteOfHour) -> {
            selectedCloseTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            String formattedHours = selectedOpenTime + " - " + selectedCloseTime;
            binding.etSellerHours.setText(formattedHours);
        }, 22, 0, true);
        closePicker.setTitle("Chọn giờ đóng cửa");
        closePicker.show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
