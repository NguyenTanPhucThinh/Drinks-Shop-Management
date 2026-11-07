package com.uitdrinkapp.app.ui.seller.product;

import com.uitdrinkapp.app.data.model.Category;
import com.uitdrinkapp.app.data.model.Product;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Pure, testable filter used by Seller screen 17; includes hidden/out-of-stock products. */
public final class SellerProductUiPolicy {
    public static final String ALL = "";
    private SellerProductUiPolicy() { }

    public static List<Product> filter(List<Product> products, List<Category> categories,
                                       String query, String categoryId) {
        if (products == null) return Collections.emptyList();
        String needle = normalize(query);
        String chosen = categoryId == null ? ALL : categoryId;
        List<Product> result = new ArrayList<>();
        for (Product product : products) {
            if (product == null) continue;
            if (!chosen.isEmpty() && !chosen.equals(product.getCategoryId())) continue;
            if (needle.isEmpty() || normalize(product.getName()).contains(needle)
                    || normalize(product.getId()).contains(needle)
                    || normalize(categoryName(categories, product.getCategoryId())).contains(needle)) {
                result.add(product);
            }
        }
        return result;
    }

    public static String categoryName(List<Category> categories, String categoryId) {
        if (categories != null) for (Category category : categories) {
            if (category != null && category.getId().equals(categoryId)) return category.getName();
        }
        return categoryId == null ? "" : categoryId;
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }
}
