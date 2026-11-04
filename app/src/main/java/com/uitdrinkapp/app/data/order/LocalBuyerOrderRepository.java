package com.uitdrinkapp.app.data.order;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Fake Repository có lưu SharedPreferences để cả 5 màn hình nhìn cùng dữ liệu sau khi điều hướng. */
public final class LocalBuyerOrderRepository implements BuyerOrderRepository {
    private static final String PREFS = "thien_demo_order_store_v1";
    private static final String KEY_CART = "cart";
    private static final String KEY_ORDERS = "orders";
    private static final String KEY_REVIEWS = "reviews";
    private static final String KEY_SEEDED = "seeded";
    private static final String KEY_HISTORY_SEEDED = "history_seeded_v2";
    private static LocalBuyerOrderRepository instance;
    private final SharedPreferences prefs;

    private LocalBuyerOrderRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_SEEDED, false)) {
            prefs.edit().putBoolean(KEY_SEEDED, true).apply();
            addDemoProductsForTesting();
        }
        if (!prefs.getBoolean(KEY_HISTORY_SEEDED, false)) {
            seedHistoryForDemo();
            prefs.edit().putBoolean(KEY_HISTORY_SEEDED, true).apply();
        }
    }
    public static synchronized LocalBuyerOrderRepository getInstance(Context context) {
        if (instance == null) instance = new LocalBuyerOrderRepository(context.getApplicationContext());
        return instance;
    }

    private JSONArray load(String key) {
        try { return new JSONArray(prefs.getString(key, "[]")); }
        catch (JSONException e) { return new JSONArray(); }
    }
    private void save(String key, JSONArray value) { prefs.edit().putString(key, value.toString()).apply(); }
    private void saveCart(List<CartLine> items) {
        JSONArray a = new JSONArray();
        for (CartLine line : items) try { a.put(line.toJson()); } catch (JSONException ignored) { }
        save(KEY_CART, a);
    }
    private void saveOrders(List<DemoOrder> orders) {
        JSONArray a = new JSONArray();
        for (DemoOrder order : orders) try { a.put(order.toJson()); } catch (JSONException ignored) { }
        save(KEY_ORDERS, a);
    }

    @Override public synchronized List<CartLine> getCart() {
        List<CartLine> lines = new ArrayList<>();
        JSONArray data = load(KEY_CART);
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) lines.add(CartLine.fromJson(o));
        }
        return lines;
    }
    @Override public List<CartLine> getAvailableProducts() {
        // Demo: các món đang có trạng thái "hiển thị". Backend thật sẽ cấp danh sách này.
        List<CartLine> products = new ArrayList<>();
        products.add(new CartLine("milk-tea-01", "Trà sữa trân châu", 32_000L,
                CartLine.Size.M, CartLine.Level.P50, CartLine.Level.P70,
                Arrays.asList(new CartLine.Topping("Trân châu đen", 5_000L)), 1));
        products.add(new CartLine("fruit-tea-02", "Trà đào cam sả", 35_000L,
                CartLine.Size.S, CartLine.Level.P30, CartLine.Level.P50,
                Collections.emptyList(), 1));
        products.add(new CartLine("coffee-03", "Cà phê muối", 39_000L,
                CartLine.Size.M, CartLine.Level.P50, CartLine.Level.P50,
                Collections.emptyList(), 1));
        products.add(new CartLine("matcha-04", "Trà sữa matcha", 42_000L,
                CartLine.Size.S, CartLine.Level.P50, CartLine.Level.P50,
                Collections.emptyList(), 1));
        products.add(new CartLine("fruit-tea-05", "Trà vải", 37_000L,
                CartLine.Size.S, CartLine.Level.P50, CartLine.Level.P50,
                Collections.emptyList(), 1));
        return products;
    }
    @Override public synchronized void clearCart() { saveCart(Collections.emptyList()); }

    @Override public synchronized void addToCart(CartLine line) {
        List<CartLine> cart = getCart();
        for (CartLine existing : cart) {
            if (existing.sameConfiguration(line)) {
                existing.quantity += line.quantity;
                saveCart(cart); return;
            }
        }
        cart.add(line.copy()); saveCart(cart);
    }
    @Override public synchronized void updateQuantity(String lineId, int quantity) {
        List<CartLine> cart = getCart();
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).lineId.equals(lineId)) {
                if (quantity <= 0) cart.remove(i);
                else cart.get(i).quantity = Math.min(99, quantity);
                break;
            }
        }
        saveCart(cart);
    }
    @Override public synchronized void removeFromCart(String lineId) { updateQuantity(lineId, 0); }
    @Override public synchronized long getSubtotal() {
        long amount = 0L;
        for (CartLine line : getCart()) amount += line.totalPrice();
        return amount;
    }
    @Override public long calculateDiscount(String code, long subtotal) {
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if ("WELCOME10".equals(normalized)) return Math.min(30_000L, subtotal / 10L);
        if ("GIAM10".equals(normalized)) return Math.min(10_000L,subtotal);
        if ("GIAM15K".equals(normalized) && subtotal >= 80_000L) return 15_000L;
        return 0L;
    }
    @Override public synchronized DemoOrder placeOrder(String voucherCode) {
        List<CartLine> cart = getCart();
        if (cart.isEmpty()) throw new IllegalStateException("Giỏ hàng đang trống");
        long subtotal = getSubtotal();
        long discount = calculateDiscount(voucherCode, subtotal);
        String id = "DH" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        DemoOrder order = new DemoOrder(id, System.currentTimeMillis(), cart, subtotal, discount,
                voucherCode == null ? "" : voucherCode, DemoOrder.Status.PENDING);
        List<DemoOrder> orders = getOrders();
        orders.add(0, order);
        saveOrders(orders);
        saveCart(Collections.emptyList());
        return order;
    }
    @Override public synchronized List<DemoOrder> getOrders() {
        List<DemoOrder> orders = new ArrayList<>();
        JSONArray data = load(KEY_ORDERS);
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) orders.add(DemoOrder.fromJson(o));
        }
        return orders;
    }
    @Override public synchronized DemoOrder getOrder(String orderId) {
        for (DemoOrder order : getOrders()) if (order.orderId.equals(orderId)) return order;
        return null;
    }
    @Override public synchronized boolean cancelOrder(String orderId) {
        List<DemoOrder> orders = getOrders();
        for (DemoOrder order : orders) if (order.orderId.equals(orderId)) {
            if (order.status != DemoOrder.Status.PENDING) return false;
            order.status = DemoOrder.Status.CANCELLED; saveOrders(orders); return true;
        }
        return false;
    }
    @Override public synchronized boolean simulateComplete(String orderId) {
        List<DemoOrder> orders = getOrders();
        for (DemoOrder order : orders) if (order.orderId.equals(orderId)) {
            if (order.status == DemoOrder.Status.CANCELLED || order.status == DemoOrder.Status.COMPLETED) return false;
            order.status = DemoOrder.Status.COMPLETED; saveOrders(orders); return true;
        }
        return false;
    }
    /** Thao tác demo độc lập: không ghi vào đơn của người bán hoặc backend. */
    @Override public synchronized boolean resetDemoOrder(String orderId) {
        if (orderId == null) return false;
        List<DemoOrder> orders = getOrders();
        for (DemoOrder order : orders) {
            if (!order.orderId.equals(orderId)) continue;
            if (order.status == DemoOrder.Status.PENDING) return false;
            order.status = DemoOrder.Status.PENDING;
            saveOrders(orders);
            return true;
        }
        return false;
    }
    @Override public synchronized DemoReview getReview(String orderId) {
        if (orderId == null) return null;
        JSONArray all = load(KEY_REVIEWS);
        for (int i = 0; i < all.length(); i++) {
            JSONObject value = all.optJSONObject(i);
            if (value != null && orderId.equals(value.optString("orderId")))
                return DemoReview.fromJson(value);
        }
        return null;
    }
    @Override public synchronized boolean updateStatus(String orderId, DemoOrder.Status nextStatus) {
        if (orderId == null || nextStatus == null) return false;
        List<DemoOrder> orders = getOrders();
        for (DemoOrder order : orders) {
            if (!order.orderId.equals(orderId)) continue;
            if (order.status == DemoOrder.Status.COMPLETED || order.status == DemoOrder.Status.CANCELLED)
                return false;
            boolean allowed = (order.status == DemoOrder.Status.PENDING &&
                    (nextStatus == DemoOrder.Status.DELIVERING || nextStatus == DemoOrder.Status.CANCELLED))
                    || (order.status == DemoOrder.Status.DELIVERING && nextStatus == DemoOrder.Status.COMPLETED);
            if (!allowed) return false;
            order.status = nextStatus;
            saveOrders(orders);
            return true;
        }
        return false;
    }
    @Override public synchronized boolean hasReview(String orderId) {
        JSONArray all = load(KEY_REVIEWS);
        for (int i = 0; i < all.length(); i++) {
            JSONObject obj = all.optJSONObject(i);
            if (obj != null && orderId.equals(obj.optString("orderId"))) return true;
        }
        return false;
    }
    @Override public synchronized void submitReview(DemoReview review) {
        DemoOrder order = getOrder(review.orderId);
        if (order == null || order.status != DemoOrder.Status.COMPLETED)
            throw new IllegalStateException("Chỉ có thể đánh giá đơn đã hoàn thành");
        if (review.stars < 1 || review.stars > 5) throw new IllegalArgumentException("Số sao không hợp lệ");
        if (hasReview(review.orderId)) throw new IllegalStateException("Đơn này đã được đánh giá");
        JSONArray data = load(KEY_REVIEWS);
        try { data.put(review.toJson()); }
        catch (JSONException e) { throw new IllegalStateException("Không thể lưu đánh giá", e); }
        save(KEY_REVIEWS, data);
    }
    private synchronized void seedHistoryForDemo() {
        List<DemoOrder> existing = getOrders();
        {
            long baseTime = 1792393200000L; // Demo ngày 19/10/2026
            if (getOrder("TS1021") == null) existing.add(new DemoOrder("TS1021", baseTime,
                    Collections.singletonList(new CartLine("matcha-04", "Trà sữa matcha", 91_000L,
                            CartLine.Size.S, CartLine.Level.P50, CartLine.Level.P50,
                            Collections.emptyList(), 1)), 91_000L, 0L, "", DemoOrder.Status.COMPLETED));
            if (getOrder("TS1008") == null) existing.add(new DemoOrder("TS1008", baseTime - 86_400_000L,
                    Collections.singletonList(new CartLine("fruit-tea-02", "Trà đào cam sả", 42_000L,
                            CartLine.Size.S, CartLine.Level.P30, CartLine.Level.P50,
                            Collections.emptyList(), 2)), 84_000L, 0L, "", DemoOrder.Status.COMPLETED));
            if (getOrder("TS0996") == null) existing.add(new DemoOrder("TS0996", baseTime - 3 * 86_400_000L,
                    Collections.singletonList(new CartLine("milk-tea-01", "Trà sữa trân châu", 35_000L,
                            CartLine.Size.S, CartLine.Level.P50, CartLine.Level.P50,
                            Collections.emptyList(), 1)), 35_000L, 0L, "", DemoOrder.Status.CANCELLED));
            saveOrders(existing);
        }
    }

    @Override public synchronized void addDemoProductsForTesting() {
        addToCart(new CartLine("milk-tea-01", "Trà sữa trân châu", 32_000L,
                CartLine.Size.M, CartLine.Level.P50, CartLine.Level.P70,
                Arrays.asList(new CartLine.Topping("Trân châu đen", 5_000L)), 2));
        addToCart(new CartLine("fruit-tea-02", "Trà đào cam sả", 35_000L,
                CartLine.Size.S, CartLine.Level.P30, CartLine.Level.P50,
                Collections.emptyList(), 1));
        addToCart(new CartLine("coffee-03", "Cà phê muối", 39_000L,
                CartLine.Size.M, CartLine.Level.P50, CartLine.Level.P50,
                Collections.emptyList(), 1));
    }
}
