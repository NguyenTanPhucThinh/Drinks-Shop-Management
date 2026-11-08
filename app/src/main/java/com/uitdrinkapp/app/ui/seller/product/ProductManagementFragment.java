package com.uitdrinkapp.app.ui.seller.product;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.chip.Chip;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.repository.ProductRepository;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentSellerProductBinding;
import java.util.List;

/** GD06: Standalone Seller product catalog. GD07 supplies the add/edit form. */
public final class ProductManagementFragment extends Fragment {
    private static final String QUERY_KEY = "thien_product_query";
    private static final String CATEGORY_KEY = "thien_product_category";
    private FragmentSellerProductBinding binding;
    private ProductRepository catalog;
    private SellerProductAdapter adapter;
    private String category = SellerProductUiPolicy.ALL;
    private String query = "";

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        if (state != null) {
            category = state.getString(CATEGORY_KEY, SellerProductUiPolicy.ALL);
            query = state.getString(QUERY_KEY, "");
        }
        catalog = RepositoryProvider.get(requireContext()).products();
        adapter = new SellerProductAdapter(this::confirmDelete, this::openEditor);
        getChildFragmentManager().setFragmentResultListener(
                ProductEditorDialogFragment.RESULT_KEY, getViewLifecycleOwner(),
                (key, result) -> refresh());
        binding.rvThienSellerProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvThienSellerProducts.setAdapter(adapter);
        binding.btnThienSellerProductBack.setOnClickListener(clicked ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        // Form is shown full-screen without changing the shared Seller navigation graph.
        binding.btnThienSellerProductAdd.setOnClickListener(clicked -> openEditor(null));
        buildCategoryChips();
        binding.etThienSellerProductSearch.setText(query);
        binding.etThienSellerProductSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s.toString(); refresh();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        refresh();
    }

    private void buildCategoryChips() {
        binding.cgThienSellerProductCategories.removeAllViews();
        addCategoryChip(SellerProductUiPolicy.ALL, getString(R.string.thien_seller_product_all));
        for (Category item : catalog.getCategories()) addCategoryChip(item.getId(), item.getName());
    }

    private void addCategoryChip(String id, String title) {
        Chip chip = new Chip(requireContext());
        chip.setText(title);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setTag(id);
        chip.setChecked(id.equals(category));
        chip.setOnClickListener(clicked -> { category = id; paintChips(); refresh(); });
        binding.cgThienSellerProductCategories.addView(chip);
        paintChip(chip, id.equals(category));
    }

    private void paintChips() {
        for (int i = 0; i < binding.cgThienSellerProductCategories.getChildCount(); i++) {
            View view = binding.cgThienSellerProductCategories.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                boolean selected = category.equals(chip.getTag());
                chip.setChecked(selected);
                paintChip(chip, selected);
            }
        }
    }

    private void paintChip(Chip chip, boolean selected) {
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                selected ? R.color.thien_seller_product_accent : R.color.thien_seller_product_card)));
        chip.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.thien_seller_product_on_accent : R.color.thien_seller_product_text));
        chip.setChipStrokeWidth(getResources().getDimension(R.dimen.thien_seller_product_stroke));
        chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                selected ? R.color.thien_seller_product_accent : R.color.thien_seller_product_border)));
    }

    @Override public void onResume() {
        super.onResume();
        if (binding != null && catalog != null && adapter != null) refresh();
    }

    private void refresh() {
        List<Product> all = catalog.getAllProducts();
        List<Product> displayed = SellerProductUiPolicy.filter(all, catalog.getCategories(), query, category);
        adapter.submit(displayed, catalog.getCategories());
        binding.tvThienSellerProductCount.setText(getString(R.string.thien_seller_product_count, displayed.size(), all.size()));
        binding.tvThienSellerProductEmpty.setVisibility(displayed.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvThienSellerProducts.setVisibility(displayed.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(String id) {
        Product target = catalog.getProductForSeller(id);
        if (target == null) { refresh(); return; }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.thien_seller_product_delete_title)
                .setMessage(getString(R.string.thien_seller_product_delete_body, target.getName()))
                .setNegativeButton(R.string.thien_seller_product_cancel, null)
                .setPositiveButton(R.string.thien_seller_product_delete_confirm, (dialog, which) -> {
                    Product fresh = catalog.getProductForSeller(id);
                    if (fresh == null || !catalog.deleteProduct(id)) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage(R.string.thien_seller_product_delete_failed)
                                .setPositiveButton(android.R.string.ok, null).show();
                    }
                    refresh();
                }).show();
    }

    private void openEditor(@Nullable String productId) {
        String tag = ProductEditorDialogFragment.TAG;
        if (getChildFragmentManager().findFragmentByTag(tag) != null) return;
        ProductEditorDialogFragment.create(productId).show(getChildFragmentManager(), tag);
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putString(QUERY_KEY, query);
        state.putString(CATEGORY_KEY, category);
        super.onSaveInstanceState(state);
    }

    @Override public void onDestroyView() {
        binding.rvThienSellerProducts.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }
}
