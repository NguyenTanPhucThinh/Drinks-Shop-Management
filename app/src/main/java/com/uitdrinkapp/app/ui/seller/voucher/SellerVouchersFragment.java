package com.uitdrinkapp.app.ui.seller.voucher;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.SellerVoucher;
import com.uitdrinkapp.app.data.repository.SellerVoucherRepository;
import com.uitdrinkapp.app.databinding.FragmentSellerVouchersBinding;
import java.util.List;

public final class SellerVouchersFragment extends Fragment {
    private FragmentSellerVouchersBinding binding;
    private boolean showExpired;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentSellerVouchersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        binding.btnSellerVouchersBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnSellerVoucherAdd.setOnClickListener(v -> openForm(null));
        binding.btnSellerVoucherCreate.setOnClickListener(v -> openForm(null));
        binding.chipVoucherRunning.setOnClickListener(v -> selectTab(false));
        binding.chipVoucherExpired.setOnClickListener(v -> selectTab(true));
        refresh();
    }

    @Override public void onResume() { super.onResume(); if (binding != null) refresh(); }

    private void selectTab(boolean expired) { showExpired = expired; refresh(); }

    private void refresh() {
        if (binding == null) return;
        binding.chipVoucherRunning.setSelected(!showExpired);
        binding.chipVoucherExpired.setSelected(showExpired);
        int active = ContextCompat.getColor(requireContext(), android.R.color.white);
        int idle = ContextCompat.getColor(requireContext(), R.color.seller_text_primary);
        binding.chipVoucherRunning.setTextColor(showExpired ? idle : active);
        binding.chipVoucherExpired.setTextColor(showExpired ? active : idle);
        binding.llSellerVoucherCards.removeAllViews();
        List<SellerVoucher> all = SellerVoucherRepository.get().getAll();
        int count = 0;
        for (SellerVoucher voucher : all) {
            if (voucher.expired != showExpired) continue;
            binding.llSellerVoucherCards.addView(createCard(voucher));
            count++;
        }
        if (count == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText(showExpired ? "Chưa có voucher hết hạn" : "Chưa có voucher đang chạy");
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.seller_text_secondary));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(32), 0, dp(32));
            binding.llSellerVoucherCards.addView(empty);
        }
    }

    private View createCard(SellerVoucher voucher) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(9));
        card.setBackgroundResource(R.drawable.bg_seller_surface_card);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(9);
        card.setLayoutParams(cardParams);

        LinearLayout top = new LinearLayout(requireContext());
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top);
        TextView code = text(voucher.code, 10, R.color.seller_nav_active, true);
        code.setBackgroundResource(R.drawable.bg_seller_icon_circle);
        code.setPadding(dp(8), dp(4), dp(8), dp(4));
        top.addView(code, new LinearLayout.LayoutParams(0, -2, 1f));
        Switch toggle = new Switch(requireContext());
        toggle.setChecked(voucher.active);
        toggle.setEnabled(!voucher.expired);
        toggle.setContentDescription("Kích hoạt " + voucher.code);
        toggle.setThumbTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.white)));
        toggle.setTrackTintList(new android.content.res.ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{ContextCompat.getColor(requireContext(), R.color.seller_nav_active),
                        ContextCompat.getColor(requireContext(), R.color.seller_surface_border)}));
        top.addView(toggle);
        toggle.setOnCheckedChangeListener((button, checked) ->
                SellerVoucherRepository.get().setActive(voucher.id, checked));

        TextView description = text(voucher.description(), 12, R.color.seller_text_primary, true);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = dp(5);
        card.addView(description, descriptionParams);
        TextView expiry = text("Hết hạn: " + (voucher.expiryDate.isEmpty() ? "—" : voucher.expiryDate),
                10, R.color.seller_text_secondary, false);
        LinearLayout.LayoutParams expiryParams = new LinearLayout.LayoutParams(-1, -2);
        expiryParams.topMargin = dp(3);
        card.addView(expiry, expiryParams);
        if (voucher.expiryDate.isEmpty()) expiry.setVisibility(View.GONE);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, dp(34));
        actionsParams.topMargin = dp(5);
        card.addView(actions, actionsParams);
        TextView edit = action(getString(R.string.seller_voucher_edit), R.drawable.bg_seller_outline_button,
                R.color.seller_nav_active);
        TextView delete = action(getString(R.string.seller_voucher_delete), R.drawable.bg_seller_danger_button,
                R.color.seller_danger);
        actions.addView(edit, new LinearLayout.LayoutParams(0, -1, 1f));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(0, -1, 1f);
        deleteParams.leftMargin = dp(6);
        actions.addView(delete, deleteParams);
        edit.setOnClickListener(v -> openForm(voucher));
        delete.setOnClickListener(v -> confirmDelete(voucher));
        return card;
    }

    private void confirmDelete(SellerVoucher voucher) {
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.seller_voucher_delete_confirm, voucher.code))
                .setNegativeButton(R.string.seller_voucher_cancel, null)
                .setPositiveButton(R.string.seller_voucher_delete, (dialog, which) -> {
                    SellerVoucherRepository.get().delete(voucher.id);
                    refresh();
                }).show();
    }

    private void openForm(SellerVoucher voucher) {
        Bundle args = new Bundle();
        if (voucher != null) args.putString("voucher_id", voucher.id);
        NavHostFragment.findNavController(this).navigate(R.id.sellerVoucherFormFragment, args);
    }

    private TextView action(String value, int background, int color) {
        TextView button = text(value, 12, color, false);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundResource(background);
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView result = new TextView(requireContext());
        result.setText(value);
        result.setTextSize(size);
        result.setTextColor(ContextCompat.getColor(requireContext(), color));
        if (bold) result.setTypeface(null, android.graphics.Typeface.BOLD);
        return result;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override public void onDestroyView() { binding = null; super.onDestroyView(); }
}
