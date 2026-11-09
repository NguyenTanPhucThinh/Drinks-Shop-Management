package com.uitdrinkapp.app.data.repository;

import com.uitdrinkapp.app.data.model.SellerVoucher;
import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/** In-memory seller voucher store for the UI prototype. */
public final class SellerVoucherRepository {
    private static final SellerVoucherRepository INSTANCE = new SellerVoucherRepository();
    private final List<SellerVoucher> vouchers = new ArrayList<>();

    private SellerVoucherRepository() {
        vouchers.add(new SellerVoucher("voucher-1", "GIAM10", "Giảm tiền", "10000",
                "50000", "", "01/10/2026", "31/10/2026", "", true, false));
        vouchers.add(new SellerVoucher("voucher-2", "NEWBIE", "Giảm %", "20",
                "", "30000", "01/11/2026", "15/11/2026", "Khách mới", true, false));
        vouchers.add(new SellerVoucher("voucher-3", "TOPFREE", "Tặng topping", "",
                "", "", "", "", "", false, false));
        vouchers.add(new SellerVoucher("voucher-4", "OLD2026", "Giảm tiền", "5000",
                "30000", "", "01/08/2026", "30/08/2026", "", false, true));
    }

    public static SellerVoucherRepository get() { return INSTANCE; }

    public synchronized List<SellerVoucher> getAll() { return new ArrayList<>(vouchers); }

    public synchronized SellerVoucher getById(String id) {
        for (SellerVoucher voucher : vouchers) if (voucher.id.equals(id)) return voucher;
        return null;
    }

    public synchronized SellerVoucher save(String id, String code, String type, String value,
            String minimumOrder, String maximumDiscount, String startDate, String expiryDate,
            String terms, boolean active) {
        SellerVoucher current = id == null || id.isEmpty() ? null : getById(id);
        if (current == null) {
            current = new SellerVoucher(UUID.randomUUID().toString(), code, type, value,
                    minimumOrder, maximumDiscount, startDate, expiryDate, terms, active, false);
            vouchers.add(0, current);
        } else {
            current.code = code;
            current.type = type;
            current.value = value;
            current.minimumOrder = minimumOrder;
            current.maximumDiscount = maximumDiscount;
            current.startDate = startDate;
            current.expiryDate = expiryDate;
            current.terms = terms;
            current.active = active;
        }
        current.expired = isExpired(expiryDate);
        return current;
    }

    private boolean isExpired(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) return false;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        format.setLenient(false);
        try {
            Date end = format.parse(expiryDate);
            if (end == null) return false;
            java.util.Calendar endDay = java.util.Calendar.getInstance();
            endDay.setTime(end);
            endDay.set(java.util.Calendar.HOUR_OF_DAY, 23);
            endDay.set(java.util.Calendar.MINUTE, 59);
            endDay.set(java.util.Calendar.SECOND, 59);
            return endDay.getTimeInMillis() < System.currentTimeMillis();
        } catch (ParseException ignored) { return false; }
    }

    public synchronized void delete(String id) { vouchers.removeIf(voucher -> voucher.id.equals(id)); }
    public synchronized void setActive(String id, boolean active) {
        SellerVoucher voucher = getById(id);
        if (voucher != null) voucher.active = active;
    }
}
