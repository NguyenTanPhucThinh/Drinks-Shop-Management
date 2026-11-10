package com.uitdrinkapp.app.ui.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.User;
import com.uitdrinkapp.app.data.repository.AuthRepository;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentLoginBinding;
import com.uitdrinkapp.app.ui.seller.main.SellerMainActivity;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private boolean isSeller = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AuthRepository auth = RepositoryProvider.get(requireContext()).auth();

        // Role switch listeners
        binding.btnRoleBuyer.setOnClickListener(v -> setSellerMode(false));
        binding.btnRoleSeller.setOnClickListener(v -> setSellerMode(true));

        binding.btnLogin.setOnClickListener(clicked -> {
            CharSequence emailValue = binding.etLoginEmail.getText();
            CharSequence passwordValue = binding.etLoginPassword.getText();
            String email = emailValue == null ? "" : emailValue.toString().trim();
            String password = passwordValue == null ? "" : passwordValue.toString();

            boolean valid = true;
            if (email.isEmpty()) {
                binding.tilLoginEmail.setError(getString(R.string.vy_error_required));
                valid = false;
            } else {
                binding.tilLoginEmail.setError(null);
            }
            if (password.isEmpty()) {
                binding.tilLoginPassword.setError(getString(R.string.vy_error_required));
                valid = false;
            } else {
                binding.tilLoginPassword.setError(null);
            }
            if (!valid) return;

            User user = auth.login(email, password);
            if (user == null) {
                binding.tilLoginPassword.setError(getString(R.string.vy_error_login_failed));
                return;
            }

            // Route based on role
            if (isSeller || user.role == User.Role.SELLER) {
                startActivity(new Intent(requireContext(), SellerMainActivity.class));
                requireActivity().finish();
                return;
            }

            NavHostFragment.findNavController(this).navigate(R.id.homeFragment, null,
                    new NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build());
        });

        binding.tvLoginGoRegister.setOnClickListener(clicked ->
                NavHostFragment.findNavController(this).navigate(R.id.registerFragment));
    }

    private void setSellerMode(boolean seller) {
        isSeller = seller;
        int colorPrimary = seller ? Color.parseColor("#D94A00") : getResources().getColor(R.color.home_green);

        if (seller) {
            binding.flLoginLogoBg.setBackgroundResource(R.drawable.bg_circle_orange);
            binding.tvLoginSubtitle.setVisibility(View.VISIBLE);
            binding.btnRoleBuyer.setBackgroundResource(android.R.color.transparent);
            binding.btnRoleBuyer.setTextColor(getResources().getColor(R.color.text_secondary));
            binding.btnRoleSeller.setBackgroundResource(R.drawable.bg_role_selected_orange);
            binding.btnRoleSeller.setTextColor(Color.WHITE);
            binding.etLoginEmail.setText("seller@uit.edu.vn");
            binding.etLoginPassword.setText("123456");
            binding.tvLoginHintDemo.setText(R.string.vy_login_hint_seller_demo);
        } else {
            binding.flLoginLogoBg.setBackgroundResource(R.drawable.bg_circle_green);
            binding.tvLoginSubtitle.setVisibility(View.GONE);
            binding.btnRoleBuyer.setBackgroundResource(R.drawable.bg_role_selected_green);
            binding.btnRoleBuyer.setTextColor(Color.WHITE);
            binding.btnRoleSeller.setBackgroundResource(android.R.color.transparent);
            binding.btnRoleSeller.setTextColor(getResources().getColor(R.color.text_secondary));
            binding.etLoginEmail.setText("demo@uit.edu.vn");
            binding.etLoginPassword.setText("123456");
            binding.tvLoginHintDemo.setText(R.string.vy_login_hint_demo);
        }

        binding.btnLogin.setBackgroundTintList(ColorStateList.valueOf(colorPrimary));
        binding.tvLoginGoRegister.setTextColor(colorPrimary);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
