package com.uitdrinkapp.app.data.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class OrderItem {
    public static final class Topping {
        public final String name;
        public final long price;

        public Topping(String name, long price) {
            this.name = name == null ? "" : name;
            this.price = Math.max(0L, price);
        }

        JSONObject toJson() throws JSONException {
            JSONObject value = new JSONObject();
            value.put("name", name);
            value.put("price", price);
            return value;
        }

        static Topping fromJson(JSONObject value) {
            return new Topping(value.optString("name"), value.optLong("price"));
        }
    }

    public final String lineId;
    public final String productId;
    public final String productName;
    public final long basePrice;
    public final long unitPrice;
    public final String size;
    public final int sugarPercent;
    public final int icePercent;
    public final List<Topping> toppings;
    public final int quantity;

    public OrderItem(String lineId, String productId, String productName, long basePrice,
                     String size, int sugarPercent, int icePercent,
                     List<Topping> toppings, int quantity) {
        this(lineId, productId, productName, basePrice, size, sugarPercent, icePercent,
                toppings, quantity, null);
    }

    public OrderItem(String lineId, String productId, String productName, long basePrice,
                     String size, int sugarPercent, int icePercent,
                     List<Topping> toppings, int quantity, long unitPriceAtPurchase) {
        this(lineId, productId, productName, basePrice, size, sugarPercent, icePercent,
                toppings, quantity, Long.valueOf(unitPriceAtPurchase));
    }

    private OrderItem(String lineId, String productId, String productName, long basePrice,
                      String size, int sugarPercent, int icePercent,
                      List<Topping> toppings, int quantity, Long savedUnitPrice) {
        this.lineId = lineId == null || lineId.isEmpty() ? UUID.randomUUID().toString() : lineId;
        this.productId = productId == null ? "" : productId;
        this.productName = productName == null ? "" : productName;
        this.basePrice = Math.max(0L, basePrice);
        this.size = size == null || size.isEmpty() ? "M" : size;
        this.sugarPercent = clampPercent(sugarPercent);
        this.icePercent = clampPercent(icePercent);
        List<Topping> safeToppings = toppings == null ? new ArrayList<>() : new ArrayList<>(toppings);
        this.toppings = Collections.unmodifiableList(safeToppings);
        this.quantity = Math.max(1, quantity);
        long calculatedPrice = this.basePrice + sizeExtraPrice(this.size);
        for (Topping topping : this.toppings) calculatedPrice += topping.price;
        this.unitPrice = savedUnitPrice == null ? calculatedPrice : Math.max(0L, savedUnitPrice);
    }

    public long unitPrice() { return unitPrice; }

    public long totalPrice() { return unitPrice() * quantity; }

    public String toppingLabel() {
        StringBuilder result = new StringBuilder();
        for (Topping topping : toppings) {
            if (result.length() > 0) result.append(", ");
            result.append(topping.name);
        }
        return result.toString();
    }

    public OrderItem copy() {
        return new OrderItem(lineId, productId, productName, basePrice,
                size, sugarPercent, icePercent, toppings, quantity, unitPrice);
    }

    JSONObject toJson() throws JSONException {
        JSONObject value = new JSONObject();
        value.put("lineId", lineId);
        value.put("productId", productId);
        value.put("productName", productName);
        value.put("basePrice", basePrice);
        value.put("unitPrice", unitPrice);
        value.put("size", size);
        value.put("sugar", sugarPercent);
        value.put("ice", icePercent);
        value.put("quantity", quantity);
        JSONArray toppingValues = new JSONArray();
        for (Topping topping : toppings) toppingValues.put(topping.toJson());
        value.put("toppings", toppingValues);
        return value;
    }

    static OrderItem fromJson(JSONObject value) {
        List<Topping> toppingValues = new ArrayList<>();
        JSONArray array = value.optJSONArray("toppings");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject topping = array.optJSONObject(i);
                if (topping != null) toppingValues.add(Topping.fromJson(topping));
            }
        }
        Long savedUnitPrice = value.has("unitPrice") ? value.optLong("unitPrice") : null;
        return new OrderItem(value.optString("lineId"), value.optString("productId"),
                value.optString("productName"), value.optLong("basePrice"),
                value.optString("size", "M"), readPercent(value, "sugar", 50),
                readPercent(value, "ice", 50), toppingValues, value.optInt("quantity", 1),
                savedUnitPrice);
    }

    private static int readPercent(JSONObject value, String key, int fallback) {
        Object raw = value.opt(key);
        if (raw instanceof Number) return clampPercent(((Number) raw).intValue());
        String text = raw == null ? "" : raw.toString();
        if (text.startsWith("P")) text = text.substring(1);
        try { return clampPercent(Integer.parseInt(text)); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static int clampPercent(int value) { return Math.max(0, Math.min(100, value)); }

    private static long sizeExtraPrice(String size) {
        if ("L".equalsIgnoreCase(size)) return 10_000L;
        if ("M".equalsIgnoreCase(size)) return 5_000L;
        return 0L;
    }
}
