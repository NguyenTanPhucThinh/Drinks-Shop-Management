package com.uitdrinkapp.app.ui.product;

import android.content.res.ColorStateList;
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
        restoreSelection(state, repository.getToppings());
        binding.btnDetailBack.setOnClickListener(clicked -> NavHostFragment.findNavController(this).popBackStack());
        binding.tvDetailName.setText(product.getName());
        binding.tvDetailDescription.setText(product.getDescription());
        binding.ivDetailImage.setContentDescription(getString(R.string.detail_image, product.getName()));
        Glide.with(this).load(LocalImages.resolve(product.getImageUrl())).centerCrop().into(binding.ivDetailImage);
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
        binding.btnFavorite.setOnClickListener(clicked -> { selection.setFavorite(!selection.isFavorite()); updateFavorite(); });
        bindChoice(binding.cgTemperature, new int[]{R.id.chip_iced, R.id.chip_hot}, Temperature.values(), selection.getTemperature(), selection::setTemperature);
        bindSizeLabels();
        bindChoice(binding.cgSize, new int[]{R.id.chip_size_s, R.id.chip_size_m, R.id.chip_size_l}, Size.values(), selection.getSize(), selection::setSize);
        bindChoice(binding.cgSugar, new int[]{R.id.chip_sugar_0, R.id.chip_sugar_30, R.id.chip_sugar_50, R.id.chip_sugar_70, R.id.chip_sugar_100}, SugarLevel.values(), selection.getSugar(), selection::setSugar);
        bindChoice(binding.cgFulfillment, new int[]{R.id.chip_counter, R.id.chip_takeaway, R.id.chip_delivery}, FulfillmentMethod.values(), selection.getFulfillment(), selection::setFulfillment);
        for (Topping topping : repository.getToppings()) addTopping(topping);
        binding.btnDetailAddCart.setOnClickListener(null);
        binding.btnDetailBuy.setOnClickListener(null);
        updatePrice();
        updateFavorite();
    }
    private <T extends Enum<T>> void bindChoice(ChipGroup group, int[] ids, T[] values, T selected, Consumer<T> setter) {
        group.check(ids[selected.ordinal()]);
        group.setOnCheckedStateChangeListener((chips, checked) -> {
            for (int index = 0; index < ids.length; index++) {
                if (chips.getCheckedChipId() == ids[index]) { setter.accept(values[index]); updatePrice(); break; }
            }
        });
    }
    private void bindSizeLabels() {
        binding.chipSizeS.setText(getString(R.string.detail_size_s));
        binding.chipSizeM.setText(getString(R.string.detail_size_with_extra, Size.M.name(), money(Size.M.getExtraPrice())));
        binding.chipSizeL.setText(getString(R.string.detail_size_with_extra, Size.L.name(), money(Size.L.getExtraPrice())));
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
        binding.btnDetailBuy.setText(getString(R.string.detail_buy_price, money(selection.getTotalPrice())));
    }
    private String money(long value) {
        return getString(R.string.home_price, NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(value));
    }
    private void updateFavorite() {
        binding.btnFavorite.setSelected(selection.isFavorite());
        binding.btnFavorite.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selection.isFavorite() ? R.color.home_green : R.color.home_muted)));
        binding.btnFavorite.setContentDescription(getString(selection.isFavorite() ? R.string.detail_favorite_remove : R.string.detail_favorite_add));
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
        selection.setFavorite(state.getBoolean("favorite"));
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
        out.putBoolean("favorite", selection.isFavorite());
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
