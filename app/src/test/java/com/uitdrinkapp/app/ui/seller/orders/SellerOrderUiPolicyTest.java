package com.uitdrinkapp.app.ui.seller.orders;

import com.uitdrinkapp.app.data.model.OrderStatus;
import org.junit.Test;
import static org.junit.Assert.*;

public final class SellerOrderUiPolicyTest {
    @Test public void expectedFilterMembership() {
        assertTrue(SellerOrderUiPolicy.matches(OrderStatus.PENDING, SellerOrderUiPolicy.Filter.NEW));
        assertFalse(SellerOrderUiPolicy.matches(OrderStatus.COMPLETED, SellerOrderUiPolicy.Filter.NEW));
        for (OrderStatus status : new OrderStatus[]{OrderStatus.DELIVERING, OrderStatus.ACCEPTED,
                OrderStatus.PREPARING, OrderStatus.READY}) {
            assertTrue(SellerOrderUiPolicy.matches(status, SellerOrderUiPolicy.Filter.PREPARING));
        }
        assertFalse(SellerOrderUiPolicy.matches(OrderStatus.CANCELLED, SellerOrderUiPolicy.Filter.PREPARING));
        assertTrue(SellerOrderUiPolicy.matches(OrderStatus.COMPLETED, SellerOrderUiPolicy.Filter.ALL));
    }

    @Test public void pendingAndPreparingActionsMatchTeamDecision() {
        assertEquals(OrderStatus.DELIVERING, SellerOrderUiPolicy.primaryNext(OrderStatus.PENDING));
        assertEquals(OrderStatus.CANCELLED, SellerOrderUiPolicy.secondaryNext(OrderStatus.PENDING));
        assertEquals(OrderStatus.COMPLETED, SellerOrderUiPolicy.primaryNext(OrderStatus.DELIVERING));
        assertEquals(OrderStatus.CANCELLED, SellerOrderUiPolicy.secondaryNext(OrderStatus.DELIVERING));
        for (OrderStatus terminal : new OrderStatus[]{OrderStatus.COMPLETED,
                OrderStatus.CANCELLED, OrderStatus.REJECTED}) {
            assertNull(SellerOrderUiPolicy.primaryNext(terminal));
            assertNull(SellerOrderUiPolicy.secondaryNext(terminal));
        }
    }

    @Test public void pr12IntermediateStatusesDoNotGetStuck() {
        assertEquals(OrderStatus.PREPARING, SellerOrderUiPolicy.primaryNext(OrderStatus.ACCEPTED));
        assertEquals(OrderStatus.READY, SellerOrderUiPolicy.primaryNext(OrderStatus.PREPARING));
        assertEquals(OrderStatus.COMPLETED, SellerOrderUiPolicy.primaryNext(OrderStatus.READY));
    }
}
