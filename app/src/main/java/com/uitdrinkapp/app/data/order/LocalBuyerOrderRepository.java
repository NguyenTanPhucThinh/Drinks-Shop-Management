package com.uitdrinkapp.app.data.order;

import android.content.Context;
import android.content.SharedPreferences;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderItem;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.PaymentMethod;
import com.uitdrinkapp.app.data.model.Review;
import com.uitdrinkapp.app.data.model.ShopSession;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import com.uitdrinkapp.app.data.repository.ShopSessionRepository;
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
    private static final String KEY_SELLER_SEEDED = "seller_seeded_task02_pr12";
    private static LocalBuyerOrderRepository instance;
    private final SharedPreferences prefs;
    private final ShopSessionRepository sessions;
    private final Context applicationContext;

    private LocalBuyerOrderRepository(Context context) {
        this(context, PREFS);
    }

    /** Package-private constructor for isolated Android instrumentation tests. */
    LocalBuyerOrderRepository(Context context, String prefsName) {
        applicationContext = context.getApplicationContext();
        prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        sessions = RepositoryProvider.get(context).sessions();
        if (!prefs.getBoolean(KEY_SEEDED, false)) {
            prefs.edit().putBoolean(KEY_SEEDED, true).apply();
            // Không tự động thêm sản phẩm demo vào giỏ hàng ban đầu nữa
        }
        if (!prefs.getBoolean(KEY_HISTORY_SEEDED, false)) {
            seedHistoryForDemo();
            prefs.edit().putBoolean(KEY_HISTORY_SEEDED, true).apply();
        }
        if (!prefs.getBoolean(KEY_SELLER_SEEDED, false)) {
            seedSellerOrdersForDemo();
            prefs.edit().putBoolean(KEY_SELLER_SEEDED, true).apply();
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
    private void saveOrders(List<Order> orders) {
        JSONArray a = new JSONArray();
        for (Order order : orders) try { a.put(order.toJson()); } catch (JSONException ignored) { }
        save(KEY_ORDERS, a);
    }

    private static List<OrderItem> snapshotOrderItems(List<CartLine> cart) {
        List<OrderItem> items = new ArrayList<>();
        for (CartLine line : cart) {
            List<OrderItem.Topping> toppings = new ArrayList<>();
            for (CartLine.Topping topping : line.toppings) {
                toppings.add(new OrderItem.Topping(topping.name, topping.price));
            }
            items.add(new OrderItem(line.lineId, line.productId, line.productName, line.basePrice,
                    line.size.name(), line.sugar.percent, line.ice.percent, toppings,
                    line.quantity, line.unitPrice()));
        }
        return items;
    }

    private String currentTableNumber() {
        ShopSession session = sessions.getSession();
        return session == null ? "" : session.getTableNumber();
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
        List<CartLine> result = new ArrayList<>();
        for (Product p : RepositoryProvider.get(applicationContext).products().getProducts()) {
            if (!p.isAvailable() || p.getTemperatures().isEmpty()
                    || p.getSugarLevels().isEmpty()) continue;
            CartLine.Level sugar = CartLine.Level.valueOf(p.getSugarLevels().get(0).name());
            CartLine.Level ice = (p.getTemperatures().size() == 1
                    && p.getTemperatures().get(0) == com.uitdrinkapp.app.data.model.Temperature.HOT)
                    ? CartLine.Level.P0 : CartLine.Level.P50;
            result.add(new CartLine(p.getId(), p.getName(), p.getPrice(),
                    CartLine.Size.S, sugar, ice,
                    Collections.emptyList(), 1, p.getPriceS()));
        }
        return result;
    }
    @Override public synchronized void clearCart() { saveCart(Collections.emptyList()); }

    @Override public synchronized void addToCart(CartLine line) {
        validateCurrentProduct(line);
        List<CartLine> cart = getCart();
        for (CartLine existing : cart) {
            if (existing.sameConfiguration(line)) {
                existing.quantity += line.quantity;
                saveCart(cart); return;
            }
        }
        cart.add(line.copy()); saveCart(cart);
    }
    /** A new cart snapshot must still be sellable; legacy imported/demo lines remain readable. */
    private void validateCurrentProduct(CartLine line) {
        if (line == null) throw new IllegalArgumentException("Thiếu sản phẩm");
        if (!line.hasSizePriceSnapshot()) return;
        Product product = RepositoryProvider.get(applicationContext).products()
                .getProductForSeller(line.productId);
        if (product == null || !product.isVisible() || !product.isAvailable()
                || product.getTemperatures().isEmpty() || product.getSugarLevels().isEmpty())
            throw new IllegalStateException("Có món đã hết hàng hoặc bị ẩn. Vui lòng xóa món đó khỏi giỏ.");
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
    @Override public synchronized Order placeOrder(String voucherCode) {
        return placeOrder(voucherCode, currentTableNumber(), "Khách",
                PaymentMethod.UNSPECIFIED);
    }

    @Override public synchronized Order placeOrder(String voucherCode, String tableNumber,
            String customerName, PaymentMethod paymentMethod) {
        List<CartLine> cart = getCart();
        if (cart.isEmpty()) throw new IllegalStateException("Giỏ hàng đang trống");
        for (CartLine line : cart) validateCurrentProduct(line);
        long subtotal = getSubtotal();
        long discount = calculateDiscount(voucherCode, subtotal);
        String id = "DH" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        Order order = new Order(id, System.currentTimeMillis(), tableNumber,
                snapshotOrderItems(cart), subtotal, discount,
                voucherCode == null ? "" : voucherCode, OrderStatus.PENDING,
                customerName, paymentMethod, true);
        List<Order> orders = getOrders();
        orders.add(0, order);
        saveOrders(orders);
        saveCart(Collections.emptyList());
        return order;
    }
    @Override public synchronized List<Order> getOrders() {
        List<Order> orders = new ArrayList<>();
        JSONArray data = load(KEY_ORDERS);
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) orders.add(Order.fromJson(o));
        }
        return orders;
    }
    @Override public synchronized Order getOrder(String orderId) {
        for (Order order : getOrders()) if (order.orderId.equals(orderId)) return order;
        return null;
    }
    @Override public synchronized boolean cancelOrder(String orderId) {
        List<Order> orders = getOrders();
        for (Order order : orders) if (order.orderId.equals(orderId)) {
            if (!OrderTransitions.canBuyerCancel(order.status)) return false;
            order.status = OrderStatus.CANCELLED; saveOrders(orders); return true;
        }
        return false;
    }
    @Override public synchronized boolean simulateComplete(String orderId) {
        List<Order> orders = getOrders();
        for (Order order : orders) if (order.orderId.equals(orderId)) {
            if (order.status == OrderStatus.CANCELLED || order.status == OrderStatus.REJECTED
                    || order.status == OrderStatus.COMPLETED) return false;
            order.status = OrderStatus.COMPLETED; saveOrders(orders); return true;
        }
        return false;
    }
    /** Thao tác demo độc lập: không ghi vào đơn của người bán hoặc backend. */
    @Override public synchronized boolean resetDemoOrder(String orderId) {
        if (orderId == null) return false;
        List<Order> orders = getOrders();
        for (Order order : orders) {
            if (!order.orderId.equals(orderId)) continue;
            if (!order.isDemo || order.status == OrderStatus.PENDING) return false;
            order.status = OrderStatus.PENDING;
            saveOrders(orders);
            return true;
        }
        return false;
    }
    @Override public synchronized Review getReview(String orderId) {
        if (orderId == null) return null;
        JSONArray all = load(KEY_REVIEWS);
        for (int i = 0; i < all.length(); i++) {
            JSONObject value = all.optJSONObject(i);
            if (value != null && orderId.equals(value.optString("orderId"))) {
                Review review = Review.fromJson(value);
                if (review.productName.isEmpty()) {
                    Order order = getOrder(orderId);
                    if (order != null && !order.items.isEmpty()) {
                        OrderItem item = order.items.get(0);
                        return review.withProduct(item.productId, item.productName);
                    }
                }
                return review;
            }
        }
        return null;
    }
    @Override public synchronized boolean updateStatus(String orderId, OrderStatus nextStatus) {
        if (orderId == null || nextStatus == null) return false;
        List<Order> orders = getOrders();
        for (Order order : orders) {
            if (!order.orderId.equals(orderId)) continue;
            if (order.status == OrderStatus.COMPLETED || order.status == OrderStatus.CANCELLED
                    || order.status == OrderStatus.REJECTED)
                return false;
            boolean allowed = OrderTransitions.canSellerTransition(order.status, nextStatus);
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
    @Override public synchronized void submitReview(Review review) {
        Order order = getOrder(review.orderId);
        if (order == null || order.status != OrderStatus.COMPLETED)
            throw new IllegalStateException("Chỉ có thể đánh giá đơn đã hoàn thành");
        if (review.rating < 1 || review.rating > 5) throw new IllegalArgumentException("Số sao không hợp lệ");
        if (hasReview(review.orderId)) throw new IllegalStateException("Đơn này đã được đánh giá");
        JSONArray data = load(KEY_REVIEWS);
        try { data.put(review.toJson()); }
        catch (JSONException e) { throw new IllegalStateException("Không thể lưu đánh giá", e); }
        save(KEY_REVIEWS, data);
    }
    private synchronized void seedHistoryForDemo() {
        List<Order> existing = getOrders();
        {
            long baseTime = 1792393200000L; // Demo ngày 19/10/2026
            if (getOrder("TS1021") == null) existing.add(new Order("TS1021", baseTime, "",
                    snapshotOrderItems(Collections.singletonList(new CartLine("matcha-04", "Trà sữa matcha", 91_000L,
                            CartLine.Size.S, CartLine.Level.P50, CartLine.Level.P50,
                            Collections.emptyList(), 1))), 91_000L, 0L, "", OrderStatus.COMPLETED, "Khách", PaymentMethod.UNSPECIFIED, true));
            if (getOrder("TS1008") == null) existing.add(new Order("TS1008", baseTime - 86_400_000L, "",
                    snapshotOrderItems(Collections.singletonList(new CartLine("fruit-tea-02", "Trà đào cam sả", 42_000L,
                            CartLine.Size.S, CartLine.Level.P30, CartLine.Level.P50,
                            Collections.emptyList(), 2))), 84_000L, 0L, "", OrderStatus.COMPLETED, "Khách", PaymentMethod.UNSPECIFIED, true));
            if (getOrder("TS0996") == null) existing.add(new Order("TS0996", baseTime - 3 * 86_400_000L, "",
                    snapshotOrderItems(Collections.singletonList(new CartLine("milk-tea-01", "Trà sữa trân châu", 35_000L,
                            CartLine.Size.S, CartLine.Level.P50, CartLine.Level.P50,
                            Collections.emptyList(), 1))), 35_000L, 0L, "", OrderStatus.CANCELLED, "Khách", PaymentMethod.UNSPECIFIED, true));
            saveOrders(existing);
        }
    }

    /** Additional Seller mock orders; never replace existing orders or reviews. */
    private synchronized void seedSellerOrdersForDemo() {
        List<Order> orders = getOrders();
        boolean added = false;
        long now = System.currentTimeMillis();
        if (getOrder("TS1025") == null) {
            orders.add(0, new Order("TS1025", now - 15 * 60_000L, "07",
                    snapshotOrderItems(Collections.singletonList(new CartLine("milk-tea-01",
                            "Trà sữa trân châu", 35_000L, CartLine.Size.S,
                            CartLine.Level.P50, CartLine.Level.P50,
                            Collections.emptyList(), 1))), 35_000L, 0L, "",
                    OrderStatus.PENDING, "Khách", PaymentMethod.UNSPECIFIED, true));
            added = true;
        }
        if (getOrder("TS1024") == null) {
            orders.add(0, new Order("TS1024", now - 45 * 60_000L, "05",
                    snapshotOrderItems(Collections.singletonList(new CartLine("fruit-tea-02",
                            "Trà đào cam sả", 42_000L, CartLine.Size.S,
                            CartLine.Level.P30, CartLine.Level.P50,
                            Collections.emptyList(), 2))), 84_000L, 0L, "",
                    OrderStatus.DELIVERING, "Khách", PaymentMethod.UNSPECIFIED, true));
            added = true;
        }
        if (added) saveOrders(orders);
        if (!hasReview("TS1008") && getOrder("TS1008") != null) {
            JSONArray all = load(KEY_REVIEWS);
            Review review = new Review("TS1008", "fruit-tea-02", "Trà đào cam sả",
                    "Khách", 5, Collections.singletonList("Ngon"),
                    "Đồ uống ngon", Collections.emptyList());
            try { all.put(review.toJson()); save(KEY_REVIEWS, all); }
            catch (JSONException ignored) { /* Fail closed: never delete earlier reviews. */ }
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
