package com.uitdrinkapp.app.data.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Order {
    public final String orderId;
    public final long createdAt;
    public final String tableNumber;
    public final List<OrderItem> items;
    public final long subtotal;
    public final long discount;
    public final String voucherCode;
    public OrderStatus status;

    public Order(String orderId, long createdAt, String tableNumber, List<OrderItem> items,
                 long subtotal, long discount, String voucherCode, OrderStatus status) {
        this.orderId = orderId == null ? "" : orderId;
        this.createdAt = createdAt;
        this.tableNumber = tableNumber == null ? "" : tableNumber;
        List<OrderItem> safeItems = new ArrayList<>();
        if (items != null) for (OrderItem item : items) if (item != null) safeItems.add(item.copy());
        this.items = Collections.unmodifiableList(safeItems);
        this.subtotal = Math.max(0L, subtotal);
        this.discount = Math.max(0L, discount);
        this.voucherCode = voucherCode == null ? "" : voucherCode;
        this.status = status == null ? OrderStatus.PENDING : status;
    }

    public long totalAmount() { return Math.max(0L, subtotal - discount); }

    public JSONObject toJson() throws JSONException {
        JSONObject value = new JSONObject();
        value.put("orderId", orderId);
        value.put("createdAt", createdAt);
        value.put("tableNumber", tableNumber);
        value.put("subtotal", subtotal);
        value.put("discount", discount);
        value.put("voucherCode", voucherCode);
        value.put("status", status.name());
        JSONArray itemValues = new JSONArray();
        for (OrderItem item : items) itemValues.put(item.toJson());
        value.put("items", itemValues);
        return value;
    }

    public static Order fromJson(JSONObject value) {
        List<OrderItem> items = new ArrayList<>();
        JSONArray itemValues = value.optJSONArray("items");
        if (itemValues != null) {
            for (int i = 0; i < itemValues.length(); i++) {
                JSONObject item = itemValues.optJSONObject(i);
                if (item != null) items.add(OrderItem.fromJson(item));
            }
        }
        OrderStatus status;
        try { status = OrderStatus.valueOf(value.optString("status")); }
        catch (IllegalArgumentException ignored) { status = OrderStatus.PENDING; }
        return new Order(value.optString("orderId"), value.optLong("createdAt"),
                value.optString("tableNumber"), items, value.optLong("subtotal"),
                value.optLong("discount"), value.optString("voucherCode"), status);
    }
}
