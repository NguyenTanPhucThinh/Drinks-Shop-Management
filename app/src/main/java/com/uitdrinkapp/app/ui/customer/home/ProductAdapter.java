package com.uitdrinkapp.app.ui.customer.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.databinding.ItemHomeProductBinding;
import com.uitdrinkapp.app.ui.common.LocalImages;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

final class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.Holder> {
    private final List<Product> products;
    private final java.util.function.Consumer<String> onProductClick;
    ProductAdapter(List<Product> products, java.util.function.Consumer<String> onProductClick) { this.products = products; this.onProductClick = onProductClick; }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Product product = products.get(position);
        ItemHomeProductBinding b = holder.binding;
        b.tvProductName.setText(product.getName());
        b.tvProductPrice.setText(b.getRoot().getContext().getString(R.string.home_price,
                NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(product.getPrice())));
        b.btnAddProduct.setContentDescription(b.getRoot().getContext().getString(R.string.home_add_product, product.getName()));
        Glide.with(b.ivProductImage).load(LocalImages.resolve(product.getImageUrl())).centerCrop().into(b.ivProductImage);
        b.getRoot().setOnClickListener(view -> onProductClick.accept(product.getId()));
        b.btnAddProduct.setOnClickListener(view -> onProductClick.accept(product.getId()));
    }
    @Override public int getItemCount() { return products.size(); }
    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeProductBinding binding;
        Holder(ItemHomeProductBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
