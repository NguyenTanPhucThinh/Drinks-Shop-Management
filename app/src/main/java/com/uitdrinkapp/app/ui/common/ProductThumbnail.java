package com.uitdrinkapp.app.ui.common;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;

/** Nạp ảnh thu nhỏ của sản phẩm vào ImageView; không tìm thấy sản phẩm thì giữ icon cốc mặc định. */
public final class ProductThumbnail {
    private static final int CORNER_DP = 10;

    private ProductThumbnail() { }

    /** Đơn demo cũ lưu ID kiểu "milk-tea-01"; đổi sang ID danh mục chung để tìm được ảnh. */
    private static String catalogId(String productId) {
        switch (productId) {
            case "milk-tea-01": return "milk_tea";
            case "fruit-tea-02": return "peach_tea";
            case "coffee-03": return "salt_coffee";
            case "matcha-04": return "matcha";
            default: return productId;
        }
    }

    public static void bind(ImageView view, String productId) {
        Product product = productId == null ? null
                : RepositoryProvider.get(view.getContext()).products().getProductById(catalogId(productId));
        int radius = Math.round(CORNER_DP * view.getResources().getDisplayMetrics().density);
        RequestBuilder<Drawable> request = product == null
                ? Glide.with(view).load(R.drawable.ic_thien_cup_asset)
                : LocalImages.load(Glide.with(view), product.getImageUrl());
        request.placeholder(R.drawable.ic_thien_cup_asset)
                .transform(new CenterCrop(), new RoundedCorners(radius))
                .into(view);
    }
}
