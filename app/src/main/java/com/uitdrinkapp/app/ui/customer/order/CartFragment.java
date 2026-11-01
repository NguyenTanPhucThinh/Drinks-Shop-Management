package com.uitdrinkapp.app.ui.customer.order;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.button.MaterialButton;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentThienCartBinding;
import com.uitdrinkapp.app.databinding.ItemThienCartBinding;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepository;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.ui.customer.order.data.CartLine;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Màn 06: danh sách món hiện có; chọn số lượng để đưa vào giỏ, không dùng nút Thêm món khác. */
public final class CartFragment extends Fragment {
    private FragmentThienCartBinding binding;
    private BuyerOrderRepository repo;
    private int category = 0;
    private String voucher = "GIAM10";

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                   @Nullable ViewGroup container,
                                                   @Nullable Bundle state) {
        binding = FragmentThienCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        repo = BuyerOrderRepositoryProvider.get(requireContext());
        binding.btnThienCartBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnThienCheckout.setOnClickListener(v -> {
            if (repo.getCart().isEmpty()) return;
            Bundle args = new Bundle(); args.putString("voucher_code", voucher);
            NavHostFragment.findNavController(this).navigate(R.id.action_thien_cart_to_checkout, args);
        });
        binding.btnThienCartVoucher.setOnClickListener(v -> chooseVoucher());
        binding.btnThienFilterAll.setOnClickListener(v -> { category = 0; render(); });
        binding.btnThienFilterMilk.setOnClickListener(v -> { category = 1; render(); });
        binding.btnThienFilterPeach.setOnClickListener(v -> { category = 2; render(); });
        binding.btnThienFilterCoffee.setOnClickListener(v -> { category = 3; render(); });
        render();
    }
    @Override public void onResume() {
        super.onResume();
        BuyerNavBars.setVisible(requireActivity(), false); // Xác nhận của nhóm: màn 06 ẩn thanh dưới.
        if (binding != null && repo != null) render();
    }
    private void chooseVoucher() {
        String[] options = getResources().getStringArray(R.array.thien_voucher_choices);
        int selected = voucher.isEmpty() ? 0 : ("GIAM10".equals(voucher) ? 1 : 2);
        new AlertDialog.Builder(requireContext()).setTitle(R.string.thien_choose_voucher)
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    voucher = which == 1 ? "GIAM10" : which == 2 ? "GIAM15K" : "";
                    dialog.dismiss(); render();
                }).setNegativeButton(R.string.thien_close, null).show();
    }
    private boolean matches(CartLine line) {
        String name = line.productName.toLowerCase(Locale.ROOT);
        if (category == 1) return name.contains("trà sữa") || name.contains("matcha");
        if (category == 2) return name.contains("trà đào") || name.contains("trà trái") || name.contains("trà vải");
        if (category == 3) return name.contains("cà phê") || name.contains("cafe");
        return true;
    }
    private void filter(MaterialButton button, boolean active) {
        button.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(
                active ? R.color.primary : R.color.thien_white)));
        button.setTextColor(requireContext().getColor(active ? R.color.on_primary : R.color.text_primary));
    }
    private void render() {
        List<CartLine> chosen = repo.getCart();
        List<CartLine> visible = new ArrayList<>(chosen);
        Set<String> alreadyAdded = new HashSet<>();
        int count = 0;
        for (CartLine row : chosen) { alreadyAdded.add(row.productId); count += row.quantity; }
        for (CartLine catalogItem : repo.getAvailableProducts())
            if (!alreadyAdded.contains(catalogItem.productId)) visible.add(catalogItem);
        binding.thienCartContainer.removeAllViews();
        binding.tvThienCartEmpty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        filter(binding.btnThienFilterAll, category == 0);
        filter(binding.btnThienFilterMilk, category == 1);
        filter(binding.btnThienFilterPeach, category == 2);
        filter(binding.btnThienFilterCoffee, category == 3);
        long subtotal = repo.getSubtotal();
        long discount = repo.calculateDiscount(voucher, subtotal);
        long finalTotal = Math.max(0L, subtotal - discount);
        binding.tvThienCartCount.setText(getString(R.string.thien_cart_count_quantity, count));
        binding.tvThienCartVoucher.setText(voucher.isEmpty() ? getString(R.string.thien_no_voucher) :
                "✓  " + voucher + " · -" + OrderUiFormat.money(discount));
        binding.tvThienSubtotal.setText(getString(R.string.thien_cart_after_discount,
                OrderUiFormat.money(finalTotal)));
        // Giá trên nút là giá sau giảm, cập nhật ngay khi số lượng/voucher thay đổi.
        binding.btnThienCheckout.setText(getString(R.string.thien_demo_checkout_btn,
                OrderUiFormat.money(finalTotal)));
        binding.btnThienCheckout.setEnabled(!chosen.isEmpty());
        for (CartLine item : visible) {
            if (!matches(item)) continue;
            boolean inCart = alreadyAdded.contains(item.productId);
            ItemThienCartBinding row = ItemThienCartBinding.inflate(getLayoutInflater(),
                    binding.thienCartContainer, false);
            row.tvThienCartItemName.setText(item.productName);
            row.tvThienCartItemOptions.setText(getString(R.string.thien_cart_options,
                    item.size.name(), item.sugar.percent, item.ice.percent));
            row.tvThienCartItemTopping.setVisibility(item.toppings.isEmpty() ? View.GONE : View.VISIBLE);
            row.tvThienCartItemTopping.setText(item.toppings.isEmpty() ? "" :
                    getString(R.string.thien_topping_label, item.toppingLabel()));
            row.tvThienCartItemPrice.setText(OrderUiFormat.money(item.unitPrice()));
            row.tvThienCartItemQuantity.setText(String.valueOf(inCart ? item.quantity : 0));
            row.btnThienCartMinus.setEnabled(inCart);
            row.btnThienCartMinus.setAlpha(inCart ? 1f : 0.4f);
            row.btnThienCartPlus.setOnClickListener(v -> {
                if (inCart) repo.updateQuantity(item.lineId, item.quantity + 1);
                else repo.addToCart(item);
                render();
            });
            row.btnThienCartMinus.setOnClickListener(v -> {
                if (inCart) repo.updateQuantity(item.lineId, item.quantity - 1);
                render();
            });
            binding.thienCartContainer.addView(row.getRoot());
        }
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
