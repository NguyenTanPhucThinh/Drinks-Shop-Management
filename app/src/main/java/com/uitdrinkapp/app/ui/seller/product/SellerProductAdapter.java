package com.uitdrinkapp.app.ui.seller.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.databinding.ItemThienSellerProductBinding;
import com.uitdrinkapp.app.ui.common.LocalImages;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Products are always reloaded from the shared catalog, never shadow-copied for Seller. */
public final class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.Holder> {
    public interface ProductAction { void onProduct(String id); }
    private static final Locale VI = Locale.forLanguageTag("vi-VN");
    private final List<Product> items = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private final ProductAction deleteAction;
    private final ProductAction editAction;

    public SellerProductAdapter(ProductAction deleteAction, ProductAction editAction) {
        this.deleteAction = deleteAction;
        this.editAction = editAction;
    }

    public void submit(List<Product> products, List<Category> categories) {
        items.clear();
        if (products != null) items.addAll(products);
        this.categories = categories == null ? new ArrayList<>() : new ArrayList<>(categories);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemThienSellerProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        Product product = items.get(position);
        ItemThienSellerProductBinding b = h.binding;
        b.tvThienSellerProductName.setText(product.getName());
        b.tvThienSellerProductCategory.setText(
                SellerProductUiPolicy.categoryName(categories, product.getCategoryId()));
        b.tvThienSellerProductPrice.setText(b.getRoot().getContext().getString(
                R.string.thien_seller_product_price,
                NumberFormat.getIntegerInstance(VI).format(product.getPriceS())));
        b.tvThienSellerProductStatus.setText(product.isVisible()
                ? product.isAvailable() ? R.string.thien_seller_product_available : R.string.thien_seller_product_out_of_stock
                : R.string.thien_seller_product_hidden);
        b.tvThienSellerProductStatus.setTextColor(b.getRoot().getContext().getColor(
                product.isVisible() && product.isAvailable()
                        ? R.color.thien_seller_product_success : R.color.thien_seller_product_warning));
        LocalImages.load(Glide.with(b.ivThienSellerProductImage), product.getImageUrl())
                .centerCrop().into(b.ivThienSellerProductImage);
        b.btnThienSellerProductDelete.setOnClickListener(clicked -> deleteAction.onProduct(product.getId()));
        b.btnThienSellerProductEdit.setOnClickListener(clicked -> editAction.onProduct(product.getId()));
    }

    @Override public int getItemCount() { return items.size(); }
    public static final class Holder extends RecyclerView.ViewHolder {
        final ItemThienSellerProductBinding binding;
        Holder(ItemThienSellerProductBinding binding) {
            super(binding.getRoot()); this.binding = binding;
        }
    }
}
