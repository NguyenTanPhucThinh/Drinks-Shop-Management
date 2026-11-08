package com.uitdrinkapp.app.data.order;

import com.uitdrinkapp.app.data.model.OrderStatus;

/** Canonical Task02 Seller flow + backwards-compatible PR #12 transitions. */
public final class OrderTransitions {
    private OrderTransitions() { }

    public static boolean canBuyerCancel(OrderStatus current) {
        return current == OrderStatus.PENDING;
    }

    public static boolean canSellerTransition(OrderStatus current, OrderStatus next) {
        if (current == null || next == null || current == next) return false;
        switch (current) {
            case PENDING:
                // Task02: DELIVERING/CANCELLED; PR #12 also introduced ACCEPTED/REJECTED.
                return next == OrderStatus.DELIVERING || next == OrderStatus.CANCELLED
                        || next == OrderStatus.ACCEPTED || next == OrderStatus.REJECTED;
            case DELIVERING:
                // New Task02 requirement: Seller may cancel while preparing.
                return next == OrderStatus.COMPLETED || next == OrderStatus.CANCELLED;
            case ACCEPTED:
                return next == OrderStatus.PREPARING;
            case PREPARING:
                return next == OrderStatus.READY;
            case READY:
                return next == OrderStatus.DELIVERING || next == OrderStatus.COMPLETED;
            default:
                return false;
        }
    }
}
