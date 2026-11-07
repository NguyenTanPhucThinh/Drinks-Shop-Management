package com.uitdrinkapp.app.data.order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Mô hình cục bộ dành riêng demo của Thiên; sẽ ánh xạ sang data.model.CartItem của Owner Core. */
public final class CartLine {
    public enum Size {
        S(0), M(5_000), L(10_000);
        public final long extraPrice;
        Size(long extraPrice) { this.extraPrice = extraPrice; }
    }
    public enum Level {
        P0(0), P30(30), P50(50), P70(70), P100(100);
        public final int percent;
        Level(int percent) { this.percent = percent; }
    }
    public static final class Topping {
        public final String name;
        public final long price;
        public Topping(String name, long price) {
            this.name = name;
            this.price = Math.max(0L, price);
        }
        JSONObject toJson() throws JSONException {
            JSONObject o = new JSONObject(); o.put("name", name); o.put("price", price); return o;
        }
    }

    public final String lineId;
    public final String productId;
    public final String productName;
    public final long basePrice;
    /** Selected size price captured at add-to-cart; -1 means a legacy cart line. */
    public final long sizePrice;
    public final Size size;
    public final Level sugar;
    public final Level ice;
    public final List<Topping> toppings;
    public int quantity;

    /** Existing callers and saved carts use the historic fixed size surcharge. */
    public CartLine(String productId, String productName, long basePrice,
                    Size size, Level sugar, Level ice, List<Topping> toppings, int quantity) {
        this(UUID.randomUUID().toString(), productId, productName, basePrice,
                size, sugar, ice, toppings, quantity, -1L);
    }

    /** Snapshot a seller-configured price for this size; later edits do not reprice orders. */
    public CartLine(String productId, String productName, long basePrice,
                    Size size, Level sugar, Level ice, List<Topping> toppings,
                    int quantity, long sizePrice) {
        this(UUID.randomUUID().toString(), productId, productName, basePrice,
                size, sugar, ice, toppings, quantity, sizePrice);
    }

    private CartLine(String lineId, String productId, String productName, long basePrice,
                     Size size, Level sugar, Level ice, List<Topping> toppings,
                     int quantity, long sizePrice) {
        this.lineId = lineId;
        this.productId = productId;
        this.productName = productName;
        this.basePrice = Math.max(0L, basePrice);
        this.sizePrice = sizePrice >= 0L ? sizePrice : -1L;
        this.size = size;
        this.sugar = sugar;
        this.ice = ice;
        this.toppings = Collections.unmodifiableList(new ArrayList<>(toppings));
        this.quantity = Math.max(1, quantity);
    }

    public boolean hasSizePriceSnapshot() { return sizePrice >= 0L; }
    public long unitPrice() {
        long result = hasSizePriceSnapshot() ? sizePrice : basePrice + size.extraPrice;
        for (Topping topping : toppings) result += topping.price;
        return result;
    }
    public long totalPrice() { return unitPrice() * quantity; }
    public String toppingLabel() {
        if (toppings.isEmpty()) return "";
        StringBuilder s = new StringBuilder();
        for (Topping topping : toppings) {
            if (s.length() > 0) s.append(", ");
            s.append(topping.name);
        }
        return s.toString();
    }
    public CartLine copy() {
        return new CartLine(lineId, productId, productName, basePrice, size, sugar, ice,
                toppings, quantity, sizePrice);
    }
    public boolean sameConfiguration(CartLine other) {
        // Never merge a newly priced selection into a cart entry at an old price.
        if (!productId.equals(other.productId) || size != other.size ||
                basePrice != other.basePrice || sizePrice != other.sizePrice ||
                sugar != other.sugar || ice != other.ice || toppings.size() != other.toppings.size()) return false;
        for (int i = 0; i < toppings.size(); i++) {
            Topping a = toppings.get(i), b = other.toppings.get(i);
            if (!a.name.equals(b.name) || a.price != b.price) return false;
        }
        return true;
    }
    JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("lineId", lineId); o.put("productId", productId);
        o.put("productName", productName); o.put("basePrice", basePrice);
        if (hasSizePriceSnapshot()) o.put("sizePrice", sizePrice);
        o.put("size", size.name()); o.put("sugar", sugar.name()); o.put("ice", ice.name());
        o.put("quantity", quantity);
        JSONArray arr = new JSONArray();
        for (Topping t : toppings) arr.put(t.toJson());
        o.put("toppings", arr); return o;
    }
    static CartLine fromJson(JSONObject o) {
        List<Topping> toppings = new ArrayList<>();
        JSONArray arr = o.optJSONArray("toppings");
        if (arr != null) for (int i = 0; i < arr.length(); i++) {
            JSONObject t = arr.optJSONObject(i);
            if (t != null) toppings.add(new Topping(t.optString("name"), t.optLong("price")));
        }
        return new CartLine(o.optString("lineId", UUID.randomUUID().toString()),
                o.optString("productId"), o.optString("productName"), o.optLong("basePrice"),
                safeSize(o.optString("size")), safeLevel(o.optString("sugar")),
                safeLevel(o.optString("ice")), toppings, o.optInt("quantity", 1),
                o.optLong("sizePrice", -1L));
    }
    private static Size safeSize(String value) {
        try { return Size.valueOf(value); } catch (IllegalArgumentException e) { return Size.M; }
    }
    private static Level safeLevel(String value) {
        try { return Level.valueOf(value); } catch (IllegalArgumentException e) { return Level.P50; }
    }
}
