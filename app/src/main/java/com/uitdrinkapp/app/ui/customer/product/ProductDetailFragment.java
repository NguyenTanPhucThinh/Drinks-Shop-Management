package com.uitdrinkapp.app.ui.customer.product;

import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.uitdrinkapp.app.MainActivity;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.*;
import com.uitdrinkapp.app.data.order.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.data.order.CartLine;
import com.uitdrinkapp.app.data.repository.ProductRepository;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentProductDetailBinding;
import com.uitdrinkapp.app.databinding.ItemDetailToppingBinding;
import com.uitdrinkapp.app.domain.DrinkSelection;
import com.uitdrinkapp.app.ui.common.LocalImages;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class ProductDetailFragment extends Fragment {
    public static final String ARG_PRODUCT_ID = "product_id";
    private FragmentProductDetailBinding binding;
    private DrinkSelection selection;
    private boolean descriptionExpanded;
    private boolean descriptionTooLong;
    private Snackbar feedback;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        ProductRepository repository = RepositoryProvider.get(requireContext()).products();
        String id = getArguments() == null ? null : getArguments().getString(ARG_PRODUCT_ID);
        Product product = repository.getProductById(id);
        if (product == null) {
            NavHostFragment.findNavController(this).popBackStack();
            ((MainActivity) requireActivity()).showMessage(R.string.detail_not_found);
            return;
        }
        selection = new DrinkSelection(product);
        List<Topping> allowedToppings = new ArrayList<>();
        for (Topping t : repository.getToppings()) {
            if (product.getToppingIds().isEmpty() || product.getToppingIds().contains(t.getId()))
                allowedToppings.add(t);
        }
        restoreSelection(state, allowedToppings);
        if (!product.getTemperatures().isEmpty() &&
                !product.getTemperatures().contains(selection.getTemperature()))
            selection.setTemperature(product.getTemperatures().get(0));
        if (!product.getSugarLevels().isEmpty() &&
                !product.getSugarLevels().contains(selection.getSugar()))
            selection.setSugar(product.getSugarLevels().get(0));
        binding.btnDetailBack.setOnClickListener(clicked -> NavHostFragment.findNavController(this).popBackStack());
        binding.tvDetailStockStatus.setText(product.isAvailable() ?
                R.string.detail_available : R.string.thien_buyer_out_of_stock);
        binding.tvDetailName.setText(product.getName());
        binding.tvDetailDescription.setText(product.getDescription());
        binding.ivDetailImage.setContentDescription(getString(R.string.detail_image, product.getName()));
        LocalImages.load(Glide.with(this), product.getImageUrl()).centerCrop().into(binding.ivDetailImage);
        for (Category category : repository.getCategories()) {
            if (category.getId().equals(product.getCategoryId())) binding.tvDetailCategory.setText(category.getName());
        }
        binding.btnDescriptionMore.setOnClickListener(clicked -> {
            if (!descriptionTooLong) return;
            descriptionExpanded = !descriptionExpanded;
            updateDescription();
        });
        updateDescription();
        binding.tvDetailDescription.addOnLayoutChangeListener((changed, left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom) -> checkDescriptionOverflow());
        binding.tvDetailDescription.post(this::checkDescriptionOverflow);
        binding.btnQuantityPlus.setOnClickListener(clicked -> { selection.setQuantity(selection.getQuantity() + 1); updatePrice(); });
        binding.btnQuantityMinus.setOnClickListener(clicked -> { selection.setQuantity(selection.getQuantity() - 1); updatePrice(); });
        bindChoice(binding.cgTemperature, new int[]{R.id.chip_iced, R.id.chip_hot}, Temperature.values(), selection.getTemperature(), selection::setTemperature);
        restrictOptions(new int[]{R.id.chip_iced, R.id.chip_hot}, Temperature.values(), product.getTemperatures());
        bindSizeLabels(product);
        bindChoice(binding.cgSize, new int[]{R.id.chip_size_s, R.id.chip_size_m, R.id.chip_size_l}, Size.values(), selection.getSize(), selection::setSize);
        bindChoice(binding.cgSugar, new int[]{R.id.chip_sugar_0, R.id.chip_sugar_30, R.id.chip_sugar_50, R.id.chip_sugar_70, R.id.chip_sugar_100}, SugarLevel.values(), selection.getSugar(), selection::setSugar);
        restrictOptions(new int[]{R.id.chip_sugar_0, R.id.chip_sugar_30, R.id.chip_sugar_50, R.id.chip_sugar_70, R.id.chip_sugar_100}, SugarLevel.values(), product.getSugarLevels());
        bindChoice(binding.cgFulfillment, new int[]{R.id.chip_counter, R.id.chip_takeaway}, FulfillmentMethod.values(), selection.getFulfillment(), selection::setFulfillment);
        for (Topping topping : allowedToppings) addTopping(topping);
        binding.btnDetailAddCart.setOnClickListener(clicked -> addSelectedToCart(false));
        binding.btnDetailBuy.setOnClickListener(clicked -> addSelectedToCart(true));
        updatePrice();
    }
    private void addSelectedToCart(boolean checkout) {
        String id = getArguments() == null ? null : getArguments().getString(ARG_PRODUCT_ID);
        Product fresh = RepositoryProvider.get(requireContext()).products().getProductById(id);
        if (fresh == null || !fresh.isAvailable() || fresh.getTemperatures().isEmpty()
                || fresh.getSugarLevels().isEmpty()
                || !fresh.getTemperatures().contains(selection.getTemperature())
                || !fresh.getSugarLevels().contains(selection.getSugar())) {
            showMessage(R.string.thien_buyer_unavailable);
            return;
        }
        try {
            BuyerOrderRepositoryProvider.get(requireContext()).addToCart(buildCartLine());
        } catch (IllegalStateException ex) {
            showMessage(R.string.thien_buyer_unavailable);
            return;
        }
        if (checkout) NavHostFragment.findNavController(this).navigate(R.id.thien_flow);
        else {
            showMessage(R.string.detail_added_to_cart);
            NavHostFragment.findNavController(this).popBackStack();
        }
    }
    private CartLine buildCartLine() {
        ProductRepository repository = RepositoryProvider.get(requireContext()).products();
        String id = getArguments() == null ? null : getArguments().getString(ARG_PRODUCT_ID);
        Product product = repository.getProductById(id);
        String productId = product != null ? product.getId() : (id != null ? id : "unknown");
        String productName = product != null ? product.getName() : binding.tvDetailName.getText().toString();
        long basePrice = product != null ? product.getPrice() : 35_000L;

        CartLine.Size cartSize;
        switch (selection.getSize()) {
            case M: cartSize = CartLine.Size.M; break;
            case L: cartSize = CartLine.Size.L; break;
            default: cartSize = CartLine.Size.S; break;
        }
        CartLine.Level cartSugar;
        switch (selection.getSugar()) {
            case P0: cartSugar = CartLine.Level.P0; break;
            case P30: cartSugar = CartLine.Level.P30; break;
            case P70: cartSugar = CartLine.Level.P70; break;
            case P100: cartSugar = CartLine.Level.P100; break;
            default: cartSugar = CartLine.Level.P50; break;
        }
        CartLine.Level cartIce = selection.getTemperature() == Temperature.HOT ? CartLine.Level.P0 : CartLine.Level.P50;
        List<CartLine.Topping> cartToppings = new ArrayList<>();
        for (Topping t : selection.getToppings()) {
            cartToppings.add(new CartLine.Topping(t.getName(), t.getPrice()));
        }
        return new CartLine(productId, productName, basePrice, cartSize, cartSugar, cartIce,
                cartToppings, selection.getQuantity(), product.getPriceFor(selection.getSize()));
    }
    private <T extends Enum<T>> void bindChoice(ChipGroup group, int[] ids, T[] values, T selected, Consumer<T> setter) {
        group.check(ids[selected.ordinal()]);
        group.setOnCheckedStateChangeListener((chips, checked) -> {
            for (int index = 0; index < ids.length; index++) {
                if (chips.getCheckedChipId() == ids[index]) { setter.accept(values[index]); updatePrice(); break; }
            }
        });
    }
    private <T extends Enum<T>> void restrictOptions(int[] ids, T[] values, List<T> allowed) {
        for (int i = 0; i < ids.length; i++) {
            View chip = binding.getRoot().findViewById(ids[i]);
            if (chip != null) chip.setVisibility(allowed.contains(values[i]) ? View.VISIBLE : View.GONE);
        }
    }
    private void bindSizeLabels(Product product) {
        binding.chipSizeS.setText(getString(R.string.thien_buyer_size_price, "S", money(product.getPriceFor(Size.S))));
        binding.chipSizeM.setText(getString(R.string.thien_buyer_size_price, "M", money(product.getPriceFor(Size.M))));
        binding.chipSizeL.setText(getString(R.string.thien_buyer_size_price, "L", money(product.getPriceFor(Size.L))));
    }
    private void addTopping(Topping topping) {
        ItemDetailToppingBinding row = ItemDetailToppingBinding.inflate(getLayoutInflater(), binding.llToppings, false);
        row.cbDetailTopping.setSaveEnabled(false);
        row.cbDetailTopping.setText(topping.getName());
        row.cbDetailTopping.setTag(topping.getId());
        row.tvToppingPrice.setText(getString(R.string.detail_topping_price, money(topping.getPrice())));
        row.cbDetailTopping.setChecked(selection.hasTopping(topping.getId()));
        updateToppingBorder(row);
        row.cbDetailTopping.setOnCheckedChangeListener((button, checked) -> {
            if (!selection.setTopping(topping, checked)) {
                row.cbDetailTopping.setChecked(false);
                showMessage(R.string.detail_topping_limit);
            }
            updateToppingBorder(row);
            updatePrice();
        });
        row.tvToppingPrice.setOnClickListener(clicked -> row.cbDetailTopping.toggle());
        binding.llToppings.addView(row.getRoot());
    }
    private void updateToppingBorder(ItemDetailToppingBinding row) {
        row.getRoot().setStrokeColor(ContextCompat.getColor(requireContext(), row.cbDetailTopping.isChecked() ? R.color.home_green : R.color.home_border));
    }
    private void updatePrice() {
        binding.tvQuantity.setText(String.valueOf(selection.getQuantity()));
        binding.tvQuantity.setContentDescription(getString(R.string.detail_quantity, selection.getQuantity()));
        binding.btnQuantityMinus.setEnabled(selection.getQuantity() > 1);
        binding.btnQuantityPlus.setEnabled(selection.getQuantity() < 99);
        String id = getArguments() == null ? null : getArguments().getString(ARG_PRODUCT_ID);
        Product fresh = RepositoryProvider.get(requireContext()).products().getProductById(id);
        boolean canBuy = fresh != null && fresh.isAvailable()
                && !fresh.getTemperatures().isEmpty() && !fresh.getSugarLevels().isEmpty();
        binding.btnDetailAddCart.setEnabled(canBuy);
        binding.btnDetailBuy.setEnabled(canBuy);
        binding.btnDetailBuy.setText(canBuy
                ? getString(R.string.detail_buy_price, money(selection.getTotalPrice()))
                : getString(R.string.thien_buyer_out_of_stock));
    }
    private String money(long value) {
        return getString(R.string.home_price, NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(value));
    }
    private void updateDescription() {
        binding.tvDetailDescription.setMaxLines(descriptionTooLong && descriptionExpanded ? Integer.MAX_VALUE : 2);
        binding.btnDescriptionMore.setVisibility(descriptionTooLong ? View.VISIBLE : View.GONE);
        binding.btnDescriptionMore.setText(descriptionExpanded ? R.string.detail_show_less : R.string.detail_show_more);
    }
    private void checkDescriptionOverflow() {
        if (binding == null || binding.tvDetailDescription.getWidth() <= 0) return;
        CharSequence text = binding.tvDetailDescription.getText();
        StaticLayout layout = StaticLayout.Builder.obtain(text, 0, text.length(),
                        binding.tvDetailDescription.getPaint(), binding.tvDetailDescription.getWidth())
                .setEllipsize(TextUtils.TruncateAt.END)
                .setMaxLines(2)
                .setBreakStrategy(binding.tvDetailDescription.getBreakStrategy())
                .setHyphenationFrequency(binding.tvDetailDescription.getHyphenationFrequency())
                .build();
        int lastLine = layout.getLineCount() - 1;
        boolean overflow = layout.getEllipsisCount(lastLine) > 0 || layout.getLineEnd(lastLine) < text.length();
        if (descriptionTooLong != overflow) {
            descriptionTooLong = overflow;
            if (!overflow) descriptionExpanded = false;
            updateDescription();
        }
    }
    private void showMessage(int message) {
        if (feedback == null) feedback = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).setAnchorView(binding.llDetailActions);
        feedback.setText(message).show();
    }
    private void restoreSelection(@Nullable Bundle state, List<Topping> toppings) {
        if (state == null) {
            if (!toppings.isEmpty()) selection.setTopping(toppings.get(0), true);
            return;
        }
        selection.setQuantity(state.getInt("quantity", 1));
        selection.setSize(Size.valueOf(state.getString("size", Size.S.name())));
        selection.setSugar(SugarLevel.valueOf(state.getString("sugar", SugarLevel.P50.name())));
        selection.setTemperature(Temperature.valueOf(state.getString("temperature", Temperature.ICED.name())));
        selection.setFulfillment(FulfillmentMethod.valueOf(state.getString("fulfillment", FulfillmentMethod.AT_COUNTER.name())));
        descriptionExpanded = state.getBoolean("description_expanded");
        ArrayList<String> ids = state.getStringArrayList("toppings");
        if (ids != null) for (Topping topping : toppings) if (ids.contains(topping.getId())) selection.setTopping(topping, true);
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (selection == null) return;
        out.putInt("quantity", selection.getQuantity());
        out.putString("size", selection.getSize().name());
        out.putString("sugar", selection.getSugar().name());
        out.putString("temperature", selection.getTemperature().name());
        out.putString("fulfillment", selection.getFulfillment().name());
        out.putBoolean("description_expanded", descriptionExpanded);
        ArrayList<String> ids = new ArrayList<>();
        for (Topping topping : selection.getToppings()) ids.add(topping.getId());
        out.putStringArrayList("toppings", ids);
    }
    @Override public void onDestroyView() {
        if (feedback != null) feedback.dismiss();
        feedback = null;
        binding = null;
        super.onDestroyView();
    }
}
