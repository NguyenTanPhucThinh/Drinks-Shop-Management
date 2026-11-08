package com.uitdrinkapp.app.data.order;

import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.Review;
import com.uitdrinkapp.app.domain.OrderStatistics;
import java.util.List;

/** Shared local order contract for Buyer/Seller. Not a backend/authorization boundary. */
public interface OrderRepository {
    List<Order> getOrders();
    Order getOrder(String orderId);
    /** Caller must enforce its role; transition validity is checked by repository. */
    boolean updateStatus(String orderId, OrderStatus nextStatus);
    boolean hasReview(String orderId);
    Review getReview(String orderId);

    default OrderStatistics getStatistics() {
        return OrderStatistics.fromOrders(getOrders(), System.currentTimeMillis());
    }
}
