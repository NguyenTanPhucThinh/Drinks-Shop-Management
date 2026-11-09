package com.uitdrinkapp.app.ui.auth;

import android.content.Intent;
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

        binding.btnLogin.setOnClickListener(clicked -> {
            CharSequence emailValue = binding.etLoginEmail.getText();
            CharSequence passwordValue = binding.etLoginPassword.getText();
            String email = emailValue == null ? "" : emailValue.toString().trim();
            String password = passwordValue == null ? "" : passwordValue.toString();

            boolean valid = true;
            if (email.isEmpty()) {
                binding.tilLoginEmail.setError(getString(R.string.vy_error_required));
                valid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilLoginEmail.setError(getString(R.string.vy_error_email));
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

            // Định tuyến theo vai trò: người bán sang SellerMainActivity, người mua vào Home
            if (user.role == User.Role.SELLER) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}