package com.uitdrinkapp.app.data.order;

import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.Review;
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
    Order placeOrder(String voucherCode);
    Order getOrder(String orderId);
    List<Order> getOrders();
    boolean cancelOrder(String orderId);
    boolean simulateComplete(String orderId);
    /** Chỉ repository demo được phép đặt lại trạng thái; remote mặc định không hỗ trợ. */
    default boolean resetDemoOrder(String orderId) { return false; }
    /** Dành cho kết nối trạng thái từ người bán/API ở giai đoạn sau. */
    boolean updateStatus(String orderId, OrderStatus nextStatus);
    boolean hasReview(String orderId);
    Review getReview(String orderId);
    void submitReview(Review review);
    void addDemoProductsForTesting();
}
