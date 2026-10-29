package com.uitdrinkapp.app.ui.customer.order.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Trạng thái mock; quản lý trạng thái thực thuộc về phía người bán/backend. */
public final class DemoOrder {
    public enum Status { PENDING, DELIVERING, COMPLETED, CANCELLED }

    public final String orderId;
    public final long createdAt;
    public final List<CartLine> items;
    public final long subtotal;
    public final long discount;
    public final String voucherCode;
    public Status status;

    DemoOrder(String orderId, long createdAt, List<CartLine> items,
              long subtotal, long discount, String voucherCode, Status status) {
        this.orderId = orderId;
        this.createdAt = createdAt;
        List<CartLine> safeCopy = new ArrayList<>();
        for (CartLine item : items) safeCopy.add(item.copy());
        this.items = Collections.unmodifiableList(safeCopy);
        this.subtotal = subtotal;
        this.discount = discount;
        this.voucherCode = voucherCode;
        this.status = status;
    }
    public long totalAmount() { return Math.max(0L, subtotal - discount); }

    JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("orderId", orderId); o.put("createdAt", createdAt);
        o.put("subtotal", subtotal); o.put("discount", discount);
        o.put("voucherCode", voucherCode); o.put("status", status.name());
        JSONArray lines = new JSONArray();
        for (CartLine item : items) lines.put(item.toJson());
        o.put("items", lines);
        return o;
    }
    static DemoOrder fromJson(JSONObject o) {
        List<CartLine> items = new ArrayList<>();
        JSONArray arr = o.optJSONArray("items");
        if (arr != null) for (int i = 0; i < arr.length(); i++) {
            JSONObject row = arr.optJSONObject(i);
            if (row != null) items.add(CartLine.fromJson(row));
        }
        Status status = Status.PENDING;
        try { status = Status.valueOf(o.optString("status")); }
        catch (IllegalArgumentException ignored) { }
        return new DemoOrder(o.optString("orderId"), o.optLong("createdAt"),
                items, o.optLong("subtotal"), o.optLong("discount"),
                o.optString("voucherCode"), status);
    }
}
