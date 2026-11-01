package com.uitdrinkapp.app.ui.customer.order.data;

import java.util.List;

/** Giao diện tạm trong phạm vi Thiên, không chỉnh data.repository dùng chung. */
public interface BuyerOrderRepository {
    List<CartLine> getCart();
    /** Chỉ các món có trạng thái hiển thị; hiện dùng catalog mock thay DB. */
    List<CartLine> getAvailableProducts();
    void clearCart();
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
    /** Dành cho kết nối trạng thái từ người bán/API ở giai đoạn sau. */
    boolean updateStatus(String orderId, DemoOrder.Status nextStatus);
    boolean hasReview(String orderId);
    DemoReview getReview(String orderId);
    void submitReview(DemoReview review);
    void addDemoProductsForTesting();
}
