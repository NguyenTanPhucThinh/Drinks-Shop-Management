package com.uitdrinkapp.app.ui.seller.orders;

import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.order.OrderTransitions;
import org.junit.Test;
import static org.junit.Assert.*;

/** Regression guard for the current short Seller UI and long legacy PR12 order statuses. */
public final class SellerOrderWorkflowCompatibilityTest {
    @Test public void shortSellerFlowRemainsSupported() {
        assertEquals(OrderStatus.DELIVERING, SellerOrderUiPolicy.primaryNext(OrderStatus.PENDING));
        assertEquals(OrderStatus.COMPLETED, SellerOrderUiPolicy.primaryNext(OrderStatus.DELIVERING));
        assertTrue(OrderTransitions.canSellerTransition(OrderStatus.PENDING, OrderStatus.CANCELLED));
        assertTrue(OrderTransitions.canSellerTransition(OrderStatus.DELIVERING, OrderStatus.CANCELLED));
        assertTrue(OrderTransitions.canBuyerCancel(OrderStatus.PENDING));
        assertFalse(OrderTransitions.canBuyerCancel(OrderStatus.DELIVERING));
    }

    @Test public void extendedPr12StatusesStillHaveACompletionPath() {
        assertTrue(OrderTransitions.canSellerTransition(OrderStatus.PENDING, OrderStatus.ACCEPTED));
        assertEquals(OrderStatus.PREPARING, SellerOrderUiPolicy.primaryNext(OrderStatus.ACCEPTED));
        assertEquals(OrderStatus.READY, SellerOrderUiPolicy.primaryNext(OrderStatus.PREPARING));
        assertTrue(OrderTransitions.canSellerTransition(OrderStatus.READY, OrderStatus.DELIVERING));
        assertEquals(OrderStatus.COMPLETED, SellerOrderUiPolicy.primaryNext(OrderStatus.READY));
    }

    @Test public void terminalStatusesNeverTransitionOrExposeSellerActions() {
        for (OrderStatus terminal : new OrderStatus[] {
                OrderStatus.COMPLETED, OrderStatus.REJECTED, OrderStatus.CANCELLED}) {
            assertNull(SellerOrderUiPolicy.primaryNext(terminal));
            assertNull(SellerOrderUiPolicy.secondaryNext(terminal));
            assertFalse(OrderTransitions.canSellerTransition(terminal, OrderStatus.PENDING));
        }
        assertFalse(OrderTransitions.canSellerTransition(OrderStatus.PENDING, OrderStatus.COMPLETED));
        assertFalse(OrderTransitions.canSellerTransition(OrderStatus.PENDING, OrderStatus.PENDING));
        assertFalse(OrderTransitions.canSellerTransition(OrderStatus.PREPARING, OrderStatus.CANCELLED));
    }
}
