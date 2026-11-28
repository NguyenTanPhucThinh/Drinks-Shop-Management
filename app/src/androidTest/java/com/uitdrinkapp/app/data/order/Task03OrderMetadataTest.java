package com.uitdrinkapp.app.data.order;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.uitdrinkapp.app.data.model.*;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Collections;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class Task03OrderMetadataTest {
    private CartLine line(Temperature t, FulfillmentMethod f) {
        return new CartLine("p1", "Tea", 30000L, CartLine.Size.M, CartLine.Level.P50,
                CartLine.Level.P50, Collections.emptyList(), 2, 42000L, t, f);
    }

    @Test public void cartSnapshotAndMerge() throws Exception {
        CartLine a = line(Temperature.ICED, FulfillmentMethod.TAKEAWAY);
        CartLine b = CartLine.fromJson(a.toJson());
        assertTrue(a.sameConfiguration(b));
        assertTrue(a.sameConfiguration(a.copy()));
        assertFalse(a.sameConfiguration(line(Temperature.ICED, FulfillmentMethod.AT_COUNTER)));
        assertFalse(a.sameConfiguration(line(Temperature.HOT, FulfillmentMethod.TAKEAWAY)));
        assertEquals(84000L, b.totalPrice());
        JSONObject old = a.toJson();
        old.remove("temperature"); old.remove("fulfillmentMethod");
        CartLine legacy = CartLine.fromJson(old);
        assertNull(legacy.temperature); assertNull(legacy.fulfillmentMethod);
        assertEquals(84000L, legacy.totalPrice());
    }

    @Test public void orderJsonAndCopyKeepMetadata() throws Exception {
        OrderItem item = new OrderItem("l", "p", "Tea", 30000L, "M", 50, 0,
                Collections.emptyList(), 2, 42000L, Temperature.HOT, FulfillmentMethod.TAKEAWAY);
        Order order = new Order("T1", 1L, "5", Collections.singletonList(item),
                84000L, 4000L, "", OrderStatus.PENDING, "Buyer", PaymentMethod.CASH,
                true, "0900000000", "Less sweet");
        Order restored = Order.fromJson(order.toJson()).withCustomerName("Buyer 2")
                .withCreatedAt(2L).withDemo(false);
        assertEquals("0900000000", restored.customerPhone);
        assertEquals("Less sweet", restored.note);
        assertEquals(Temperature.HOT, restored.items.get(0).temperature);
        assertEquals(FulfillmentMethod.TAKEAWAY, restored.items.get(0).fulfillmentMethod);
        assertEquals(42000L, restored.items.get(0).unitPrice());
        assertEquals(80000L, restored.totalAmount());
        JSONObject legacy = order.toJson();
        legacy.remove("customerPhone"); legacy.remove("note");
        JSONObject legacyItem = legacy.getJSONArray("items").getJSONObject(0);
        legacyItem.remove("temperature"); legacyItem.remove("fulfillmentMethod");
        Order old = Order.fromJson(legacy);
        assertEquals("", old.customerPhone); assertEquals("", old.note);
        assertNull(old.items.get(0).temperature); assertNull(old.items.get(0).fulfillmentMethod);
        assertEquals(80000L, old.totalAmount());
    }
}
