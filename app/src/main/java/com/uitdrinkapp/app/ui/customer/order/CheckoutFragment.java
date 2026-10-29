package com.uitdrinkapp.app.ui.customer.order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentThienCheckoutBinding;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepository;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.ui.customer.order.data.CartLine;
import com.uitdrinkapp.app.ui.customer.order.data.DemoOrder;

/** Màn 08. Voucher chỉ là danh mục mock tạm thời, sẽ kết nối picker của Duyên. */
public final class CheckoutFragment extends Fragment {
    private FragmentThienCheckoutBinding binding;
    private BuyerOrderRepository repo;
    private String voucher = "";

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                    @Nullable ViewGroup container,
                                                    @Nullable Bundle savedInstanceState) {
        binding = FragmentThienCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = BuyerOrderRepositoryProvider.get(requireContext());
        voucher = getArguments()==null?"":getArguments().getString("voucher_code","");
        binding.rbThienCash.setOnClickListener(v -> binding.rbThienWallet.setChecked(false));
        binding.rbThienWallet.setOnClickListener(v -> {
            binding.rbThienCash.setChecked(false);
            Toast.makeText(requireContext(),R.string.thien_demo_wallet_note,Toast.LENGTH_SHORT).show();
        });
        binding.btnThienCheckoutBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnThienChooseVoucher.setOnClickListener(v -> chooseVoucher());
        binding.btnThienPlaceOrder.setOnClickListener(v -> {
            if (binding.rbThienWallet.isChecked()) {
                Toast.makeText(requireContext(),R.string.thien_demo_wallet_note,Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                DemoOrder newOrder = repo.placeOrder(voucher);
                Bundle args = new Bundle(); args.putString("order_id", newOrder.orderId);
                NavHostFragment.findNavController(this).navigate(R.id.action_thien_checkout_to_tracking, args);
            } catch (IllegalStateException ex) {
                Toast.makeText(requireContext(), R.string.thien_cart_empty, Toast.LENGTH_SHORT).show();
            }
        });
        render();
    }
    private void chooseVoucher() {
        String[] choices = getResources().getStringArray(R.array.thien_voucher_choices);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.thien_choose_voucher)
                .setSingleChoiceItems(choices, voucher.isEmpty() ? 0 : (voucher.equals("GIAM10") ? 1 : 2),
                    (dialog, which) -> {
                        voucher = which == 1 ? "GIAM10" : which == 2 ? "GIAM15K" : "";
                        dialog.dismiss(); render();
                    })
                .setNegativeButton(R.string.thien_close, null).show();
    }
    private void render() {
        binding.thienCheckoutItems.removeAllViews();
        for (CartLine item : repo.getCart()) {
            TextView label = new TextView(requireContext());
            label.setText(getString(R.string.thien_checkout_item_line,
                    item.quantity, item.productName, item.size.name(), OrderUiFormat.money(item.totalPrice())));
            label.setTextColor(getResources().getColor(R.color.text_primary, requireContext().getTheme()));
            label.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.text_14));
            label.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_8), 0,
                    getResources().getDimensionPixelSize(R.dimen.spacing_8));
            binding.thienCheckoutItems.addView(label);
        }
        long subtotal = repo.getSubtotal();
        long discount = repo.calculateDiscount(voucher, subtotal);
        binding.tvThienCheckoutSubtotal.setText(OrderUiFormat.money(subtotal));
        binding.tvThienCheckoutDiscount.setText(OrderUiFormat.money(discount));
        binding.tvThienCheckoutTotal.setText(OrderUiFormat.money(Math.max(0L, subtotal - discount)));
        binding.tvThienSelectedVoucher.setText(voucher.isEmpty()
                ? getString(R.string.thien_no_voucher)
                : voucher + (discount == 0 ? getString(R.string.thien_voucher_not_eligible) : ""));
        binding.btnThienPlaceOrder.setEnabled(!repo.getCart().isEmpty());
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
