package com.uitdrinkapp.app.data.order;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public final class CartLineSizePriceTest {
    private CartLine line(long snapshot) {
        return new CartLine("tea", "Trà sữa", 39000L, CartLine.Size.M,
                CartLine.Level.P50, CartLine.Level.P50,
                Arrays.asList(new CartLine.Topping("Pearl", 5000L)), 2, snapshot);
    }
    @Test public void newCartKeepsSellerPriceSnapshot() {
        assertEquals(50000L, line(45000L).unitPrice());
        assertEquals(100000L, line(45000L).totalPrice());
        assertEquals(45000L, line(45000L).copy().sizePrice);
        assertFalse(line(45000L).sameConfiguration(line(50000L)));
    }
    @Test public void oldCartUsesLegacySurcharge() {
        CartLine old = new CartLine("tea", "Trà sữa", 39000L, CartLine.Size.M,
                CartLine.Level.P50, CartLine.Level.P50,
                Collections.emptyList(), 1);
        assertFalse(old.hasSizePriceSnapshot());
        assertEquals(44000L, old.unitPrice());
    }
}
