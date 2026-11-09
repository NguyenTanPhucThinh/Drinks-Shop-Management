package com.uitdrinkapp.app.ui.seller.voucher;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.SellerVoucher;
import com.uitdrinkapp.app.data.repository.SellerVoucherRepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentSellerVoucherFormBinding;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class SellerVoucherFormFragment extends Fragment {
    private FragmentSellerVoucherFormBinding binding;
    private String voucherId = "";
    private String selectedType = "Giảm tiền";

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentSellerVoucherFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        Bundle args = getArguments();
        voucherId = args == null ? "" : args.getString("voucher_id", "");
        SellerVoucher voucher = SellerVoucherRepositoryProvider.get(requireContext()).getById(voucherId);
        binding.btnVoucherFormBack.setOnClickListener(v -> goBack());
        binding.btnSellerVoucherCancel.setOnClickListener(v -> goBack());
        binding.btnSellerVoucherSave.setOnClickListener(v -> save());
        binding.etSellerVoucherStart.setOnClickListener(v -> pickDate(binding.etSellerVoucherStart));
        binding.etSellerVoucherEnd.setOnClickListener(v -> pickDate(binding.etSellerVoucherEnd));
        binding.switchSellerVoucherActive.setThumbTintList(
                android.content.res.ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white)));
        binding.switchSellerVoucherActive.setTrackTintList(new android.content.res.ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{ContextCompat.getColor(requireContext(), R.color.seller_nav_active),
                        ContextCompat.getColor(requireContext(), R.color.seller_surface_border)}));
        binding.chipVoucherTypeMoney.setOnClickListener(v -> setType("Giảm tiền"));
        binding.chipVoucherTypePercent.setOnClickListener(v -> setType("Giảm %"));
        binding.chipVoucherTypeTopping.setOnClickListener(v -> setType("Tặng topping"));
        if (voucher == null) {
            binding.tvVoucherFormTitle.setText(R.string.seller_voucher_new_title);
            binding.btnSellerVoucherSave.setText(R.string.seller_voucher_save);
        } else {
            binding.tvVoucherFormTitle.setText(R.string.seller_voucher_edit_title);
            binding.btnSellerVoucherSave.setText(R.string.seller_voucher_save_edit);
            bindVoucher(voucher);
        }
        setType(selectedType);
    }

    private void bindVoucher(SellerVoucher voucher) {
        binding.etSellerVoucherCode.setText(voucher.code);
        binding.etSellerVoucherValue.setText(voucher.value);
        binding.etSellerVoucherMinimum.setText(voucher.minimumOrder);
        binding.etSellerVoucherMaximum.setText(voucher.maximumDiscount);
        binding.etSellerVoucherStart.setText(voucher.startDate);
        binding.etSellerVoucherEnd.setText(voucher.expiryDate);
        binding.etSellerVoucherTerms.setText(voucher.terms);
        binding.switchSellerVoucherActive.setChecked(voucher.active);
        selectedType = voucher.type;
    }

    private void setType(String type) {
        selectedType = type;
        TextView[] chips = {binding.chipVoucherTypeMoney, binding.chipVoucherTypePercent,
                binding.chipVoucherTypeTopping};
        String[] values = {"Giảm tiền", "Giảm %", "Tặng topping"};
        for (int i = 0; i < chips.length; i++) {
            boolean selected = values[i].equals(type);
            chips[i].setSelected(selected);
            chips[i].setTextColor(ContextCompat.getColor(requireContext(), selected
                    ? android.R.color.white : R.color.seller_text_primary));
        }
        binding.llVoucherValueFields.setVisibility("Tặng topping".equals(type) ? View.GONE : View.VISIBLE);
        binding.etSellerVoucherValue.setHint("Giảm %".equals(type) ? "VD: 20" : "VD: 10.000đ");
        binding.etSellerVoucherMaximum.setEnabled("Giảm %".equals(type));
        binding.etSellerVoucherMaximum.setAlpha("Giảm %".equals(type) ? 1f : .55f);
    }

    private void pickDate(EditText target) {
        Calendar initial = Calendar.getInstance();
        String current = target.getText().toString();
        try {
            Date parsed = dateFormat().parse(current);
            if (parsed != null) initial.setTime(parsed);
        } catch (ParseException ignored) { }
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, day);
            target.setText(dateFormat().format(chosen.getTime()));
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String code = value(binding.etSellerVoucherCode).toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            binding.etSellerVoucherCode.setError(getString(R.string.seller_voucher_required));
            binding.etSellerVoucherCode.requestFocus();
            return;
        }
        String amount = value(binding.etSellerVoucherValue);
        if (!"Tặng topping".equals(selectedType)
                && (amount.isEmpty() || !amount.matches("\\d+"))) {
            binding.etSellerVoucherValue.setError(getString(R.string.seller_voucher_invalid_amount));
            binding.etSellerVoucherValue.requestFocus();
            return;
        }
        String start = value(binding.etSellerVoucherStart);
        String end = value(binding.etSellerVoucherEnd);
        if (!start.isEmpty() && !end.isEmpty()) {
            try {
                if (dateFormat().parse(end).before(dateFormat().parse(start))) {
                    binding.etSellerVoucherEnd.setError(getString(R.string.seller_voucher_invalid_dates));
                    return;
                }
            } catch (ParseException exception) {
                binding.etSellerVoucherEnd.setError(getString(R.string.seller_voucher_invalid_dates));
                return;
            }
        }
        SellerVoucherRepositoryProvider.get(requireContext()).save(voucherId, code, selectedType, amount,
                value(binding.etSellerVoucherMinimum), value(binding.etSellerVoucherMaximum),
                start, end, value(binding.etSellerVoucherTerms), binding.switchSellerVoucherActive.isChecked());
        Toast.makeText(requireContext(), R.string.seller_voucher_saved, Toast.LENGTH_SHORT).show();
        goBack();
    }

    private String value(EditText field) { return field.getText() == null ? "" : field.getText().toString().trim(); }
    private SimpleDateFormat dateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        format.setLenient(false);
        return format;
    }
    private void goBack() { NavHostFragment.findNavController(this).popBackStack(); }

    @Override public void onDestroyView() { binding = null; super.onDestroyView(); }
}
