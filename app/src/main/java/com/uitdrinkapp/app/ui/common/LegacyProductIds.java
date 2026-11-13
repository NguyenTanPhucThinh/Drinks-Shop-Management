package com.uitdrinkapp.app.ui.common;

/** Đơn/giỏ demo cũ lưu ID kiểu "milk-tea-01"; đổi sang ID danh mục chung. */
public final class LegacyProductIds {
    private LegacyProductIds() { }

    public static String toCatalogId(String productId) {
        if (productId == null) return null;
        switch (productId) {
            case "milk-tea-01": return "milk_tea";
            case "fruit-tea-02": return "peach_tea";
            case "coffee-03": return "salt_coffee";
            case "matcha-04": return "matcha";
            default: return productId;
        }
    }
}
