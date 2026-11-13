package com.uitdrinkapp.app.ui.customer.order;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.button.MaterialButton;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentThienCartBinding;
import com.uitdrinkapp.app.databinding.ItemThienCartBinding;
import com.uitdrinkapp.app.data.order.BuyerOrderRepository;
import com.uitdrinkapp.app.data.order.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.data.order.CartLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        binding.btnThienCartBack.setOnClickListener(v -> navigateToHome());
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateToHome();
                    }
                });
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
    private void navigateToHome() {
        NavController nav = NavHostFragment.findNavController(this);
        if (!nav.popBackStack(R.id.homeFragment, false)) {
            nav.navigate(R.id.homeFragment);
        }
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
        int count = 0;
        for (CartLine row : chosen) { count += row.quantity; }
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
            ItemThienCartBinding row = ItemThienCartBinding.inflate(getLayoutInflater(),
                    binding.thienCartContainer, false);
            row.tvThienCartItemName.setText(item.productName);
            row.tvThienCartItemOptions.setText(getString(R.string.thien_cart_options,
                    item.size.name(), item.sugar.percent, item.ice.percent));
            row.tvThienCartItemTopping.setVisibility(item.toppings.isEmpty() ? View.GONE : View.VISIBLE);
            row.tvThienCartItemTopping.setText(item.toppings.isEmpty() ? "" :
                    getString(R.string.thien_topping_label, item.toppingLabel()));
            row.tvThienCartItemPrice.setText(OrderUiFormat.money(item.unitPrice()));
            row.tvThienCartItemQuantity.setText(String.valueOf(item.quantity));
            row.btnThienCartMinus.setEnabled(true);
            row.btnThienCartMinus.setAlpha(1f);
            // Bấm vùng tên/ảnh/thẻ để xem chi tiết; không ảnh hưởng tới nút +/- số lượng.
            row.getRoot().setContentDescription(getString(R.string.thien_cart_open_detail, item.productName));
            row.getRoot().setClickable(true);
            row.getRoot().setFocusable(true);
            row.getRoot().setOnClickListener(v -> openProductDetails(item));
            row.tvThienCartItemName.setOnClickListener(v -> openProductDetails(item));
            row.btnThienCartPlus.setOnClickListener(v -> {
                repo.updateQuantity(item.lineId, item.quantity + 1);
                render();
            });
            row.btnThienCartMinus.setOnClickListener(v -> {
                repo.updateQuantity(item.lineId, item.quantity - 1);
                render();
            });
            binding.thienCartContainer.addView(row.getRoot());
        }
    }
    /** Ánh xạ tạm ID demo của Thiên sang ID danh mục chung của nhóm. */
    private void openProductDetails(CartLine line) {
        String sharedId;
        switch (line.productId) {
            case "milk-tea-01": sharedId = "milk_tea"; break;
            case "fruit-tea-02": sharedId = "peach_tea"; break;
            case "coffee-03": sharedId = "salt_coffee"; break;
            case "matcha-04": sharedId = "matcha"; break;
            default: sharedId = line.productId;
        }
        Product product = RepositoryProvider.get(requireContext()).products().getProductById(sharedId);
        if (product == null || !product.getName().equalsIgnoreCase(line.productName)) {
            // Chưa có trong danh mục phía Duyên: vẫn xem được thông tin demo,
            // không điều hướng tới ID thiếu khiến màn chi tiết lập tức tự đóng.
            new AlertDialog.Builder(requireContext())
                    .setTitle(line.productName)
                    .setMessage(getString(R.string.thien_cart_missing_product_price,
                            OrderUiFormat.money(line.unitPrice())) + "\n\n" +
                            getString(R.string.thien_cart_missing_product_note))
                    .setPositiveButton(R.string.thien_close, null).show();
            return;
        }
        Bundle args = new Bundle();
        args.putString("product_id", product.getId());
        NavHostFragment.findNavController(this).navigate(R.id.productDetailFragment, args);
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
