package com.uitdrinkapp.app.ui.customer.order.data;

import java.util.List;

/** Giao diện tạm trong phạm vi Thiên, không chỉnh data.repository dùng chung. */
public interface BuyerOrderRepository {
    List<CartLine> getCart();
    void addToCart(CartLine line);
    void updateQuantity(String lineId, int quantity);
    void removeFromCart(String lineId);
    long getSubtotal();
    long calculateDiscount(String voucherCode, long subtotal);
    DemoOrder placeOrder(String voucherCode);
    DemoOrder getOrder(String orderId);
    List<DemoOrder> getOrders();
    boolean cancelOrder(String orderId);
    boolean simulateComplete(String orderId);
    boolean hasReview(String orderId);
    void submitReview(DemoReview review);
    void addDemoProductsForTesting();
}
