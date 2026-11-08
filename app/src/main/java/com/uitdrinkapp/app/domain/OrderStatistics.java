package com.uitdrinkapp.app.domain;

import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderStatus;
import java.util.Calendar;
import java.util.List;

/** Unified local-demo metrics; revenue includes only COMPLETED orders. */
public final class OrderStatistics {
    public final int totalCount;
    public final int pendingCount;
    public final int preparingCount;
    public final int completedCount;
    public final int cancelledCount;
    public final int otherCount;
    public final long completedRevenue;
    public final long todayRevenue;

    private OrderStatistics(int total, int pending, int preparing, int completed,
            int cancelled, int other, long revenue, long today) {
        totalCount = total;
        pendingCount = pending;
        preparingCount = preparing;
        completedCount = completed;
        cancelledCount = cancelled;
        otherCount = other;
        completedRevenue = revenue;
        todayRevenue = today;
    }

    public static OrderStatistics fromOrders(List<Order> orders, long nowMillis) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(nowMillis);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        long todayStart = start.getTimeInMillis();
        start.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrowStart = start.getTimeInMillis();
        int total = 0, pending = 0, preparing = 0, completed = 0, cancelled = 0, other = 0;
        long revenue = 0, today = 0;
        if (orders != null) for (Order order : orders) {
            if (order == null || order.status == null) continue;
            total++;
            OrderStatus status = order.status;
            switch (status) {
                case PENDING: pending++; break;
                case DELIVERING:
                case PREPARING: preparing++; break;
                case COMPLETED:
                    completed++;
                    revenue += order.totalAmount();
                    // Provisional metric: completedAt not recorded in shared Order yet.
                    if (order.createdAt >= todayStart && order.createdAt < tomorrowStart) {
                        today += order.totalAmount();
                    }
                    break;
                case CANCELLED:
                case REJECTED: cancelled++; break;
                default: other++; break;
            }
        }
        return new OrderStatistics(total, pending, preparing, completed, cancelled,
                other, revenue, today);
    }
}
