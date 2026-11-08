package com.uitdrinkapp.app.ui.seller.orders;

import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.order.OrderTransitions;

/** Filter/action policy for Seller screen 15, independent of Android widgets. */
public final class SellerOrderUiPolicy {
    public enum Filter { ALL, NEW, PREPARING }

    private SellerOrderUiPolicy() { }

    public static boolean matches(OrderStatus status, Filter filter) {
        if (filter == null || filter == Filter.ALL) return true;
        if (filter == Filter.NEW) return status == OrderStatus.PENDING;
        return status == OrderStatus.DELIVERING || status == OrderStatus.ACCEPTED
                || status == OrderStatus.PREPARING || status == OrderStatus.READY;
    }

    /** Maps the agreed 4-step Seller workflow while supporting PR #12's extended statuses. */
    public static OrderStatus primaryNext(OrderStatus current) {
        if (current == null) return null;
        OrderStatus next;
        switch (current) {
            case PENDING: next = OrderStatus.DELIVERING; break;
            case DELIVERING: next = OrderStatus.COMPLETED; break;
            case ACCEPTED: next = OrderStatus.PREPARING; break;
            case PREPARING: next = OrderStatus.READY; break;
            case READY: next = OrderStatus.COMPLETED; break;
            default: return null;
        }
        return OrderTransitions.canSellerTransition(current, next) ? next : null;
    }

    /** Seller rejection is stored as CANCELLED per team four-state decision. */
    public static OrderStatus secondaryNext(OrderStatus current) {
        if (current == OrderStatus.PENDING || current == OrderStatus.DELIVERING) {
            return OrderTransitions.canSellerTransition(current, OrderStatus.CANCELLED)
                    ? OrderStatus.CANCELLED : null;
        }
        return null;
    }
}
