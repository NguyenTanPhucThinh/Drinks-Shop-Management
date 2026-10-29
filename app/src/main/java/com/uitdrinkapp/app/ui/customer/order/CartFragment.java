package com.uitdrinkapp.app.ui.customer.order;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import com.google.android.material.button.MaterialButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.databinding.FragmentThienCartBinding;
import com.uitdrinkapp.app.databinding.ItemThienCartBinding;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepository;
import com.uitdrinkapp.app.ui.customer.order.data.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.ui.customer.order.data.CartLine;
import java.util.List;

/** Màn 06: giỏ hàng, tab Đặt hàng của MainActivity. */
public final class CartFragment extends Fragment {
    private FragmentThienCartBinding binding;
    private BuyerOrderRepository repo;
    private int selectedCategory = 0; // 0 tất cả, 1 trà sữa, 2 trà trái cây, 3 cà phê
    private String selectedVoucher = "GIAM10";

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                    @Nullable ViewGroup container,
                                                    @Nullable Bundle savedInstanceState) {
        binding = FragmentThienCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = BuyerOrderRepositoryProvider.get(requireContext());
        binding.btnThienCheckout.setOnClickListener(v -> {
            if (!repo.getCart().isEmpty())
                { Bundle args = new Bundle(); args.putString("voucher_code",selectedVoucher);
                NavHostFragment.findNavController(this).navigate(R.id.action_thien_cart_to_checkout,args); }
        });
        binding.btnThienCartVoucher.setOnClickListener(v -> chooseVoucher());
        binding.btnThienFilterAll.setOnClickListener(v -> {selectedCategory=0;render();});
        binding.btnThienFilterMilk.setOnClickListener(v -> {selectedCategory=1;render();});
        binding.btnThienFilterPeach.setOnClickListener(v -> {selectedCategory=2;render();});
        binding.btnThienFilterCoffee.setOnClickListener(v -> {selectedCategory=3;render();});
        binding.btnThienSeedCart.setOnClickListener(v -> {
            repo.addDemoProductsForTesting(); render();
        });
        render();
    }
    @Override public void onResume() { super.onResume(); if (binding != null && repo != null) render(); }

    private void chooseVoucher(){
        String[] choices=getResources().getStringArray(R.array.thien_voucher_choices);
        int current=selectedVoucher.isEmpty()?0:("GIAM10".equals(selectedVoucher)?1:2);
        new AlertDialog.Builder(requireContext()).setTitle(R.string.thien_choose_voucher)
          .setSingleChoiceItems(choices,current,(d,which)->{
              selectedVoucher=which==1?"GIAM10":(which==2?"GIAM15K":"");
              d.dismiss();render();
          }).setNegativeButton(R.string.thien_close,null).show();
    }
    private void styleFilter(MaterialButton b, boolean active){
        int bg=requireContext().getColor(active?R.color.primary:R.color.thien_white);
        int fg=requireContext().getColor(active?R.color.on_primary:R.color.primary);
        b.setBackgroundTintList(ColorStateList.valueOf(bg));b.setTextColor(fg);
    }
    private boolean matchesFilter(CartLine item){
        String n=item.productName.toLowerCase(java.util.Locale.ROOT);
        if(selectedCategory==1) return n.contains("trà sữa") || n.contains("matcha");
        if(selectedCategory==2) return n.contains("trà đào") || n.contains("trà trái");
        if(selectedCategory==3) return n.contains("cà phê") || n.contains("cafe");
        return true;
    }
    private void render() {
        List<CartLine> cart = repo.getCart();
        binding.thienCartContainer.removeAllViews();
        boolean empty = cart.isEmpty();
        binding.tvThienCartEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.btnThienSeedCart.setVisibility(View.VISIBLE);
        styleFilter(binding.btnThienFilterAll,selectedCategory==0);
        styleFilter(binding.btnThienFilterMilk,selectedCategory==1);
        styleFilter(binding.btnThienFilterPeach,selectedCategory==2);
        styleFilter(binding.btnThienFilterCoffee,selectedCategory==3);
        long discount=repo.calculateDiscount(selectedVoucher,repo.getSubtotal());
        binding.tvThienCartVoucher.setText(selectedVoucher.isEmpty()?getString(R.string.thien_no_voucher):
            selectedVoucher+" · -"+OrderUiFormat.money(discount));
        binding.btnThienCheckout.setEnabled(!empty);
        binding.tvThienCartCount.setText(getString(R.string.thien_products_count, cart.size()));
        binding.tvThienSubtotal.setText(getString(R.string.thien_demo_due,
                OrderUiFormat.money(Math.max(0,repo.getSubtotal()-repo.calculateDiscount(selectedVoucher,repo.getSubtotal())))));
        binding.btnThienCheckout.setText(getString(R.string.thien_demo_checkout_btn,
                OrderUiFormat.money(Math.max(0,repo.getSubtotal()-repo.calculateDiscount(selectedVoucher,repo.getSubtotal())))));
        for (CartLine item : cart) {
            if (!matchesFilter(item)) continue;
            ItemThienCartBinding row = ItemThienCartBinding.inflate(getLayoutInflater(), binding.thienCartContainer, false);
            row.tvThienCartItemName.setText(item.productName);
            row.tvThienCartItemOptions.setText(getString(R.string.thien_cart_options,
                    item.size.name(), item.sugar.percent, item.ice.percent));
            row.tvThienCartItemTopping.setText(item.toppings.isEmpty()
                    ? getString(R.string.thien_no_topping)
                    : getString(R.string.thien_topping_label, item.toppingLabel()));
            row.tvThienCartItemPrice.setText(OrderUiFormat.money(item.totalPrice()));
            row.tvThienCartItemQuantity.setText(String.valueOf(item.quantity));
            row.btnThienCartPlus.setOnClickListener(v -> {
                repo.updateQuantity(item.lineId, item.quantity + 1); render();
            });
            row.btnThienCartMinus.setOnClickListener(v -> {
                repo.updateQuantity(item.lineId, item.quantity - 1); render();
            });
            row.btnThienCartRemove.setOnClickListener(v -> {
                repo.removeFromCart(item.lineId); render();
            });
            binding.thienCartContainer.addView(row.getRoot());
        }
    }
    @Override public void onDestroyView() {
        super.onDestroyView(); binding = null;
    }
}
