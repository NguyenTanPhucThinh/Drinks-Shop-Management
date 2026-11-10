package com.uitdrinkapp.app.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
            String name = read(binding.etRegisterName);
            String contact = read(binding.etRegisterEmail);
            String username = read(binding.etRegisterUsername);
            CharSequence passwordValue = binding.etRegisterPassword.getText();
            CharSequence confirmValue = binding.etRegisterConfirm.getText();
            String password = passwordValue == null ? "" : passwordValue.toString();
            String confirm = confirmValue == null ? "" : confirmValue.toString();

            boolean valid = true;

            boolean nameOk = !name.isEmpty();
            binding.tilRegisterName.setError(nameOk ? null : getString(R.string.vy_error_required));
            valid &= nameOk;

            boolean contactOk = !contact.isEmpty();
            binding.tilRegisterEmail.setError(contactOk ? null : getString(R.string.vy_error_required));
            valid &= contactOk;

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

            // Determine email vs phone from contact input
            String email = contact.contains("@") ? contact : contact + "@uit.edu.vn";
            String phone = contact.contains("@") ? "0900000000" : contact;

            if (!auth.register(name, email, phone, password)) {
                binding.tilRegisterEmail.setError(getString(R.string.vy_error_email_exists));
                return;
            }

            NavHostFragment.findNavController(this).navigate(R.id.homeFragment, null,
                    new NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build());
        });

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
