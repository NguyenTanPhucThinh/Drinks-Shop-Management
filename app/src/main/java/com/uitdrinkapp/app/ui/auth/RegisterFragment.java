package com.uitdrinkapp.app.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.repository.AuthRepository;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AuthRepository auth = RepositoryProvider.get(requireContext()).auth();

        binding.btnRegister.setOnClickListener(clicked -> {
            String email = read(binding.etRegisterEmail);
            String phone = read(binding.etRegisterPhone);
            String username = read(binding.etRegisterUsername);
            CharSequence passwordValue = binding.etRegisterPassword.getText();
            CharSequence confirmValue = binding.etRegisterConfirm.getText();
            String password = passwordValue == null ? "" : passwordValue.toString();
            String confirm = confirmValue == null ? "" : confirmValue.toString();

            boolean valid = true;

            boolean emailOk = !email.isEmpty() && email.contains("@");
            binding.tilRegisterEmail.setError(email.isEmpty() ? getString(R.string.vy_error_required)
                    : (email.contains("@") ? null : getString(R.string.vy_error_email)));
            valid &= emailOk;

            boolean phoneOk = !phone.isEmpty();
            binding.tilRegisterPhone.setError(phoneOk ? null : getString(R.string.vy_error_required));
            valid &= phoneOk;

            boolean userOk = !username.isEmpty();
            binding.tilRegisterUsername.setError(userOk ? null : getString(R.string.vy_error_required));
            valid &= userOk;

            boolean passOk = password.length() >= 6;
            binding.tilRegisterPassword.setError(passOk ? null : getString(R.string.vy_error_password_short));
            valid &= passOk;

            boolean sameOk = password.equals(confirm);
            binding.tilRegisterConfirm.setError(sameOk ? null : getString(R.string.vy_error_password_mismatch));
            valid &= sameOk;

            if (!valid) return;

            String fullName = username.isEmpty() ? email : username;

            if (!auth.register(fullName, email, phone, password)) {
                binding.tilRegisterEmail.setError(getString(R.string.vy_error_email_exists));
                return;
            }

            NavHostFragment.findNavController(this).navigate(R.id.homeFragment, null,
                    new NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build());
        });

        View.OnClickListener socialClickListener = v ->
                Toast.makeText(requireContext(), R.string.home_unavailable, Toast.LENGTH_SHORT).show();

        binding.btnSocialGoogle.setOnClickListener(socialClickListener);
        binding.btnSocialWechat.setOnClickListener(socialClickListener);
        binding.btnSocialFacebook.setOnClickListener(socialClickListener);

        binding.tvRegisterGoLogin.setOnClickListener(clicked ->
                NavHostFragment.findNavController(this).popBackStack());
    }

    private static String read(TextInputEditText field) {
        CharSequence value = field.getText();
        return value == null ? "" : value.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
