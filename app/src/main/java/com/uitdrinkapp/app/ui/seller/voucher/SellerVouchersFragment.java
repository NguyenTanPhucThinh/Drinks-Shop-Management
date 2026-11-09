package com.uitdrinkapp.app.ui.seller.voucher;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.SellerVoucher;
import com.uitdrinkapp.app.data.repository.SellerVoucherRepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentSellerVouchersBinding;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
        int active = ContextCompat.getColor(requireContext(), R.color.white);
        int idle = ContextCompat.getColor(requireContext(), R.color.seller_text_primary);
        binding.chipVoucherRunning.setTextColor(showExpired ? idle : active);
        binding.chipVoucherExpired.setTextColor(showExpired ? active : idle);
        binding.llSellerVoucherCards.removeAllViews();
        List<SellerVoucher> all = SellerVoucherRepositoryProvider.get(requireContext()).getAll();
        int count = 0;
        for (SellerVoucher voucher : all) {
            if (voucher.expired != showExpired) continue;
            binding.llSellerVoucherCards.addView(createCard(voucher));
            count++;
        }
        if (count == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText(showExpired ? R.string.voucher_empty_expired : R.string.voucher_empty_running);
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.seller_text_secondary));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.voucher_description_text_size));
            empty.setGravity(Gravity.CENTER);
            int emptyPadding = dimension(R.dimen.voucher_empty_vertical_padding);
            empty.setPadding(0, emptyPadding, 0, emptyPadding);
            binding.llSellerVoucherCards.addView(empty);
        }
    }

    private View createCard(SellerVoucher voucher) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dimension(R.dimen.voucher_card_padding_horizontal),
                dimension(R.dimen.voucher_card_padding_top),
                dimension(R.dimen.voucher_card_padding_horizontal),
                dimension(R.dimen.voucher_card_padding_bottom));
        card.setBackgroundResource(R.drawable.bg_voucher_card);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dimension(R.dimen.voucher_card_bottom_margin);
        card.setLayoutParams(cardParams);

        LinearLayout top = new LinearLayout(requireContext());
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top);
        TextView code = text(voucher.code, R.dimen.voucher_code_text_size,
                R.color.voucher_primary, true);
        code.setBackgroundResource(R.drawable.bg_voucher_badge);
        int badgeHorizontalPadding = dimension(R.dimen.voucher_badge_padding_horizontal);
        int badgeVerticalPadding = dimension(R.dimen.voucher_badge_padding_vertical);
        code.setPadding(badgeHorizontalPadding, badgeVerticalPadding,
                badgeHorizontalPadding, badgeVerticalPadding);
        top.addView(code, new LinearLayout.LayoutParams(-2, -2));
        View spacer = new View(requireContext());
        top.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));
        MaterialSwitch toggle = new MaterialSwitch(requireContext());
        toggle.setChecked(voucher.active);
        toggle.setEnabled(!voucher.expired);
        toggle.setContentDescription(getString(R.string.voucher_toggle_content_description,
                voucher.code));
        toggle.setMinimumWidth(dimension(R.dimen.voucher_switch_min_width));
        toggle.setMinimumHeight(dimension(R.dimen.voucher_switch_min_height));
        toggle.setPadding(0, 0, 0, 0);
        toggle.setScaleX(getResources().getFraction(R.fraction.voucher_switch_scale_x, 1, 1));
        toggle.setScaleY(getResources().getFraction(R.fraction.voucher_switch_scale_y, 1, 1));
        toggle.setThumbTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.white)));
        toggle.setTrackTintList(new android.content.res.ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{ContextCompat.getColor(requireContext(), R.color.voucher_primary),
                        ContextCompat.getColor(requireContext(), R.color.voucher_switch_inactive)}));
        toggle.setTrackDecorationTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.TRANSPARENT));
        top.addView(toggle, new LinearLayout.LayoutParams(-2,
                dimension(R.dimen.voucher_switch_view_height)));
        toggle.setOnCheckedChangeListener((button, checked) ->
                SellerVoucherRepositoryProvider.get(requireContext()).setActive(voucher.id, checked));

        TextView description = text(description(voucher), R.dimen.voucher_description_text_size,
                R.color.seller_text_primary, true);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = dimension(R.dimen.voucher_description_margin_top);
        card.addView(description, descriptionParams);
        TextView expiry = text(getString(R.string.voucher_expiry_format,
                        voucher.expiryDate.isEmpty()
                                ? getString(R.string.voucher_no_expiry) : voucher.expiryDate),
                R.dimen.voucher_expiry_text_size, R.color.seller_text_secondary, false);
        LinearLayout.LayoutParams expiryParams = new LinearLayout.LayoutParams(-1, -2);
        expiryParams.topMargin = dimension(R.dimen.voucher_expiry_margin_top);
        card.addView(expiry, expiryParams);
        if (voucher.expiryDate.isEmpty()) expiry.setVisibility(View.GONE);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1,
                dimension(R.dimen.voucher_action_row_height));
        actionsParams.topMargin = dimension(R.dimen.voucher_action_row_margin_top);
        card.addView(actions, actionsParams);
        TextView edit = action(getString(R.string.seller_voucher_edit), R.drawable.bg_voucher_action_edit,
                R.color.voucher_primary);
        TextView delete = action(getString(R.string.seller_voucher_delete), R.drawable.bg_voucher_action_delete,
                R.color.seller_danger);
        actions.addView(edit, new LinearLayout.LayoutParams(0, -1, 1f));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(0, -1, 1f);
        deleteParams.leftMargin = dimension(R.dimen.voucher_action_gap);
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
                    SellerVoucherRepositoryProvider.get(requireContext()).delete(voucher.id);
                    refresh();
                }).show();
    }

    private void openForm(SellerVoucher voucher) {
        Bundle args = new Bundle();
        if (voucher != null) args.putString("voucher_id", voucher.id);
        NavHostFragment.findNavController(this).navigate(R.id.sellerVoucherFormFragment, args);
    }

    private String description(SellerVoucher voucher) {
        String result;
        if (getString(R.string.seller_voucher_type_topping).equals(voucher.type)) {
            result = getString(R.string.voucher_description_topping);
        } else if (getString(R.string.seller_voucher_type_percent).equals(voucher.type)) {
            result = getString(R.string.voucher_description_percent,
                    formatAmount(voucher.value), formatAmount(voucher.maximumDiscount));
        } else if (voucher.minimumOrder.isEmpty()) {
            result = getString(R.string.voucher_description_money, formatAmount(voucher.value));
        } else {
            result = getString(R.string.voucher_description_money_minimum,
                    formatAmount(voucher.value), formatAmount(voucher.minimumOrder));
        }
        return voucher.terms.isEmpty() ? result
                : getString(R.string.voucher_description_terms, result, voucher.terms);
    }

    private String formatAmount(String value) {
        try {
            NumberFormat formatter = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
            return formatter.format(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private TextView action(String value, int background, int color) {
        TextView button = text(value, R.dimen.voucher_action_text_size, color, false);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundResource(background);
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private TextView text(String value, int textSize, int color, boolean bold) {
        TextView result = new TextView(requireContext());
        result.setText(value);
        result.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(textSize));
        result.setTextColor(ContextCompat.getColor(requireContext(), color));
        if (bold) result.setTypeface(null, android.graphics.Typeface.BOLD);
        return result;
    }

    private int dimension(int resourceId) { return Math.round(getResources().getDimension(resourceId)); }
    @Override public void onDestroyView() { binding = null; super.onDestroyView(); }
}
