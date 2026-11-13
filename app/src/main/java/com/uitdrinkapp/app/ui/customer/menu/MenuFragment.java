package com.uitdrinkapp.app.ui.customer.menu;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.databinding.FragmentMenuBinding;
import com.uitdrinkapp.app.databinding.ItemMenuProductBinding;
import com.uitdrinkapp.app.ui.common.LocalImages;
import com.uitdrinkapp.app.ui.customer.product.ProductDetailFragment;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Menu của người mua: toàn bộ đồ uống, lọc theo danh mục và tìm theo tên. */
public final class MenuFragment extends Fragment {
    private FragmentMenuBinding binding;
    private RepositoryProvider repositories;
    private final List<Product> shown = new ArrayList<>();
    private MenuAdapter adapter;
    /** null = Tất cả. */
    private String categoryId;
    private String query = "";

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        repositories = RepositoryProvider.get(requireContext());
        binding.btnMenuBack.setOnClickListener(v -> {
            NavController nav = NavHostFragment.findNavController(this);
            if (!nav.popBackStack(R.id.homeFragment, false)) nav.navigate(R.id.homeFragment);
        });
        adapter = new MenuAdapter();
        binding.rvMenu.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMenu.setAdapter(adapter);
        binding.etMenuSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase(Locale.ROOT);
                refresh();
            }
        });
        buildFilters();
        refresh();
    }

    @Override public void onResume() {
        super.onResume();
        if (binding != null) refresh(); // Seller có thể vừa thêm/sửa món.
    }

    private void buildFilters() {
        binding.llMenuFilters.removeAllViews();
        addChip(getString(R.string.menu_filter_all), null);
        for (Category c : repositories.products().getCategories()) addChip(c.getName(), c.getId());
        styleChips();
    }

    private void addChip(String label, String id) {
        MaterialButton chip = new MaterialButton(requireContext());
        chip.setText(label);
        chip.setTag(id == null ? "" : id);
        chip.setAllCaps(false);
        chip.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.radius_pill));
        chip.setOnClickListener(v -> { categoryId = id; styleChips(); refresh(); });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.spacing_8));
        binding.llMenuFilters.addView(chip, lp);
    }

    private void styleChips() {
        for (int i = 0; i < binding.llMenuFilters.getChildCount(); i++) {
            MaterialButton chip = (MaterialButton) binding.llMenuFilters.getChildAt(i);
            String tag = (String) chip.getTag();
            boolean active = categoryId == null ? tag.isEmpty() : categoryId.equals(tag);
            chip.setBackgroundTintList(ColorStateList.valueOf(
                    requireContext().getColor(active ? R.color.primary : R.color.primary_light)));
            chip.setTextColor(requireContext().getColor(active ? R.color.on_primary : R.color.primary));
        }
    }

    private void refresh() {
        if (binding == null) return;
        shown.clear();
        for (Product p : repositories.products().getProducts()) {
            if (categoryId != null && !categoryId.equals(p.getCategoryId())) continue;
            if (!query.isEmpty() && !p.getName().toLowerCase(Locale.ROOT).contains(query)) continue;
            shown.add(p);
        }
        adapter.notifyDataSetChanged();
        binding.tvMenuEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openProduct(String productId) {
        NavController nav = NavHostFragment.findNavController(this);
        if (nav.getCurrentDestination() == null
                || nav.getCurrentDestination().getId() != R.id.menuFragment) return;
        Bundle args = new Bundle();
        args.putString(ProductDetailFragment.ARG_PRODUCT_ID, productId);
        nav.navigate(R.id.productDetailFragment, args);
    }

    @Override public void onDestroyView() {
        binding.rvMenu.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }

    private final class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.Holder> {
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemMenuProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        @Override public void onBindViewHolder(@NonNull Holder h, int position) {
            Product p = shown.get(position);
            ItemMenuProductBinding b = h.binding;
            b.tvMenuName.setText(p.getName());
            b.tvMenuDesc.setText(p.getDescription());
            String price = getString(R.string.home_price,
                    NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(p.getPrice()));
            b.tvMenuPrice.setText(p.isAvailable() ? price
                    : price + " · " + getString(R.string.thien_buyer_out_of_stock));
            b.btnMenuAdd.setEnabled(p.isAvailable());
            b.btnMenuAdd.setContentDescription(getString(R.string.home_add_product, p.getName()));
            LocalImages.load(Glide.with(b.ivMenuImage), p.getImageUrl()).centerCrop().into(b.ivMenuImage);
            b.getRoot().setOnClickListener(v -> openProduct(p.getId()));
            b.btnMenuAdd.setOnClickListener(v -> { if (p.isAvailable()) openProduct(p.getId()); });
        }
        @Override public int getItemCount() { return shown.size(); }
        final class Holder extends RecyclerView.ViewHolder {
            final ItemMenuProductBinding binding;
            Holder(ItemMenuProductBinding binding) { super(binding.getRoot()); this.binding = binding; }
        }
    }
}
