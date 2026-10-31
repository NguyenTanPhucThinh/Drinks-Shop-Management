package com.uitdrinkapp.app.ui.customer.voucher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentVouchersBinding;

public final class VouchersFragment extends Fragment {
    private FragmentVouchersBinding binding;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentVouchersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        binding.btnVoucherBack.setOnClickListener(clicked -> NavHostFragment.findNavController(this).popBackStack());
        binding.rvVouchers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVouchers.setAdapter(new VoucherAdapter(RepositoryProvider.get(requireContext()).vouchers().getVouchers()));
    }
    @Override public void onDestroyView() {
        binding.rvVouchers.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
