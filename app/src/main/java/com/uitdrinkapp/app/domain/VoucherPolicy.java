package com.uitdrinkapp.app.domain;

import com.uitdrinkapp.app.data.model.SellerVoucher;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Domain policy encapsulating all business rules for seller and buyer vouchers (expiration, discounts). */
public final class VoucherPolicy {
    private VoucherPolicy() {}

    public static boolean isExpired(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) return false;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        format.setLenient(false);
        try {
            Date end = format.parse(expiryDate);
            if (end == null) return false;
            Calendar endDay = Calendar.getInstance();
            endDay.setTime(end);
            endDay.set(Calendar.HOUR_OF_DAY, 23);
            endDay.set(Calendar.MINUTE, 59);
            endDay.set(Calendar.SECOND, 59);
            return endDay.getTimeInMillis() < System.currentTimeMillis();
        } catch (ParseException ignored) { return false; }
    }

    public static long calculateDiscount(List<SellerVoucher> activeVouchers, String code, long subtotal) {
        if (code == null || code.trim().isEmpty()) return 0L;
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (activeVouchers != null) {
            for (SellerVoucher v : activeVouchers) {
                if (v.code != null && v.code.trim().toUpperCase(Locale.ROOT).equals(normalized)) {
                    long minOrder = 0L;
                    try { if (!v.minimumOrder.isEmpty()) minOrder = Long.parseLong(v.minimumOrder); } catch (Exception ignored) {}
                    if (subtotal < minOrder) return 0L;

                    long discount = 0L;
                    if ("Giảm tiền".equals(v.type)) {
                        try { discount = Long.parseLong(v.value); } catch (Exception ignored) {}
                    } else if ("Giảm %".equals(v.type)) {
                        try {
                            double percent = Double.parseDouble(v.value);
                            discount = Math.round(subtotal * percent / 100.0);
                            if (!v.maximumDiscount.isEmpty()) {
                                long maxDisc = Long.parseLong(v.maximumDiscount);
                                discount = Math.min(discount, maxDisc);
                            }
                        } catch (Exception ignored) {}
                    } else if ("Tặng topping".equals(v.type)) {
                        discount = 5_000L;
                    }
                    return Math.min(subtotal, discount);
                }
            }
        }
        if ("WELCOME10".equals(normalized)) return Math.min(30_000L, subtotal / 10L);
        if ("GIAM10".equals(normalized)) return Math.min(10_000L, subtotal);
        if ("GIAM15K".equals(normalized) && subtotal >= 80_000L) return 15_000L;
        return 0L;
    }
}
