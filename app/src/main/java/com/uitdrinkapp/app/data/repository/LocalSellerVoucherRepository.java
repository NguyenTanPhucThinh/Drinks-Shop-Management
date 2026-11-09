package com.uitdrinkapp.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.uitdrinkapp.app.data.model.SellerVoucher;
import com.uitdrinkapp.app.domain.VoucherPolicy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** SharedPreferences-backed implementation of SellerVoucherRepository. */
public final class LocalSellerVoucherRepository implements SellerVoucherRepository {
    private static final String PREFS = "seller_voucher_store_v2";
    private static final String KEY_VOUCHERS = "vouchers";
    private static LocalSellerVoucherRepository instance;
    private final SharedPreferences prefs;

    private LocalSellerVoucherRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (getAll().isEmpty()) {
            saveInternal(new SellerVoucher("voucher-1", "GIAM10", "Giảm tiền", "10000",
                    "50000", "", "01/10/2026", "31/10/2026", "", true, false));
            saveInternal(new SellerVoucher("voucher-2", "NEWBIE", "Giảm %", "20",
                    "", "30000", "01/11/2026", "15/11/2026", "Khách mới", true, false));
            saveInternal(new SellerVoucher("voucher-3", "TOPFREE", "Tặng topping", "",
                    "", "", "", "", "", false, false));
            saveInternal(new SellerVoucher("voucher-4", "OLD2026", "Giảm tiền", "5000",
                    "30000", "", "01/08/2026", "30/08/2026", "", false, true));
        }
    }

    public static synchronized LocalSellerVoucherRepository getInstance(Context context) {
        if (instance == null) instance = new LocalSellerVoucherRepository(context.getApplicationContext());
        return instance;
    }

    private JSONArray load() {
        try { return new JSONArray(prefs.getString(KEY_VOUCHERS, "[]")); }
        catch (JSONException e) { return new JSONArray(); }
    }

    private void saveAll(List<SellerVoucher> items) {
        JSONArray a = new JSONArray();
        for (SellerVoucher v : items) try { a.put(toJson(v)); } catch (JSONException ignored) {}
        prefs.edit().putString(KEY_VOUCHERS, a.toString()).apply();
    }

    private JSONObject toJson(SellerVoucher v) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", v.id);
        o.put("code", v.code);
        o.put("type", v.type);
        o.put("value", v.value);
        o.put("minimumOrder", v.minimumOrder);
        o.put("maximumDiscount", v.maximumDiscount);
        o.put("startDate", v.startDate);
        o.put("expiryDate", v.expiryDate);
        o.put("terms", v.terms);
        o.put("active", v.active);
        o.put("expired", v.expired);
        return o;
    }

    private SellerVoucher fromJson(JSONObject o) {
        return new SellerVoucher(
                o.optString("id", UUID.randomUUID().toString()),
                o.optString("code"),
                o.optString("type"),
                o.optString("value"),
                o.optString("minimumOrder"),
                o.optString("maximumDiscount"),
                o.optString("startDate"),
                o.optString("expiryDate"),
                o.optString("terms"),
                o.optBoolean("active", true),
                o.optBoolean("expired", false)
        );
    }

    @Override public synchronized List<SellerVoucher> getAll() {
        List<SellerVoucher> list = new ArrayList<>();
        JSONArray arr = load();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null) {
                SellerVoucher v = fromJson(o);
                v.expired = VoucherPolicy.isExpired(v.expiryDate);
                list.add(v);
            }
        }
        return list;
    }

    @Override public synchronized List<SellerVoucher> getActive() {
        List<SellerVoucher> list = new ArrayList<>();
        for (SellerVoucher v : getAll()) {
            if (v.active && !v.expired) list.add(v);
        }
        return list;
    }

    @Override public synchronized SellerVoucher getById(String id) {
        for (SellerVoucher voucher : getAll()) if (voucher.id.equals(id)) return voucher;
        return null;
    }

    private void saveInternal(SellerVoucher v) {
        List<SellerVoucher> list = getAll();
        list.add(0, v);
        saveAll(list);
    }

    @Override public synchronized SellerVoucher save(String id, String code, String type, String value,
            String minimumOrder, String maximumDiscount, String startDate, String expiryDate,
            String terms, boolean active) {
        List<SellerVoucher> list = getAll();
        SellerVoucher current = null;
        for (SellerVoucher v : list) {
            if (v.id.equals(id)) { current = v; break; }
        }
        boolean expired = VoucherPolicy.isExpired(expiryDate);
        if (current == null) {
            current = new SellerVoucher(UUID.randomUUID().toString(), code, type, value,
                    minimumOrder, maximumDiscount, startDate, expiryDate, terms, active, expired);
            list.add(0, current);
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
            current.expired = expired;
        }
        saveAll(list);
        return current;
    }

    @Override public synchronized void delete(String id) {
        List<SellerVoucher> list = getAll();
        list.removeIf(voucher -> voucher.id.equals(id));
        saveAll(list);
    }

    @Override public synchronized void setActive(String id, boolean active) {
        List<SellerVoucher> list = getAll();
        for (SellerVoucher v : list) {
            if (v.id.equals(id)) { v.active = active; break; }
        }
        saveAll(list);
    }

    @Override public synchronized long calculateDiscount(String code, long subtotal) {
        return VoucherPolicy.calculateDiscount(getActive(), code, subtotal);
    }
}
