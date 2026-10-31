package com.uitdrinkapp.app.data.mock;

import com.uitdrinkapp.app.data.model.Voucher;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Mock voucher catalog shown by the voucher screen and home banners. */
public final class VoucherMockData {
    private VoucherMockData() { }
    public static final List<Voucher> VOUCHERS = Collections.unmodifiableList(Arrays.asList(
            new Voucher("drink50", "Giảm 50% đồ uống (tối đa size L)", "Không áp dụng cho món giá đặc biệt, không gộp ưu đãi khác", "HSD 22/02/2027"),
            new Voucher("drink20", "Giảm 20% đồ uống (tối đa size L)", "Không áp dụng cho món giá đặc biệt, không gộp ưu đãi khác", "HSD 22/02/2027"),
            new Voucher("buy1get1", "Mua 1 tặng 1 (tối đa size M)", "Áp dụng cho trà sữa và trà trái cây", "HSD 04/03/2027"),
            new Voucher("delivery", "Miễn phí giao hàng", "Có thể gộp với ưu đãi khác", "Hết hạn ngày mai 23:00")));
}
