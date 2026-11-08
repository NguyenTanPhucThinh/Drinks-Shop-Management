package com.uitdrinkapp.app.data.order;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.uitdrinkapp.app.data.model.Order;
import com.uitdrinkapp.app.data.model.OrderStatus;
import com.uitdrinkapp.app.data.model.PaymentMethod;
import com.uitdrinkapp.app.data.model.Review;
import com.uitdrinkapp.app.domain.OrderStatistics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Collections;
import java.util.UUID;
import static org.junit.Assert.*;

/** Isolated instrumentation tests; never wipe the production SharedPreferences. */
@RunWith(AndroidJUnit4.class)
public final class SharedOrderRepositoryTest {
    private Context context;
    private String testPrefs;
    private LocalBuyerOrderRepository buyer;

    @Before public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testPrefs = "task02_test_" + UUID.randomUUID();
        buyer = new LocalBuyerOrderRepository(context, testPrefs);
    }

    @After public void cleanup() {
        context.getSharedPreferences(testPrefs, Context.MODE_PRIVATE).edit().clear().commit();
    }

    private CartLine drink() {
        return new CartLine("coffee-03", "Cà phê muối", 39_000L,
                CartLine.Size.S, CartLine.Level.P50, CartLine.Level.P50,
                Collections.emptyList(), 1);
    }

    @Test public void providerReturnsSameInstance() {
        assertSame(BuyerOrderRepositoryProvider.get(context), OrderRepositoryProvider.get(context));
    }

    @Test public void sellerReadsBuyerOrderAndCanCancelDuringPreparation() {
        buyer.addToCart(drink());
        Order order = buyer.placeOrder("");
        OrderRepository seller = buyer;
        assertNotNull(seller.getOrder(order.orderId));
        assertTrue(order.isDemo);
        assertEquals(PaymentMethod.UNSPECIFIED, order.paymentMethod);
        assertTrue(seller.updateStatus(order.orderId, OrderStatus.DELIVERING));
        assertFalse(buyer.cancelOrder(order.orderId));
        assertTrue(seller.updateStatus(order.orderId, OrderStatus.CANCELLED));
        assertEquals(OrderStatus.CANCELLED, buyer.getOrder(order.orderId).status);
        assertFalse(seller.updateStatus(order.orderId, OrderStatus.COMPLETED));
    }

    @Test public void completedOrderIsCountedAndCanBeReviewed() {
        buyer.addToCart(drink());
        Order order = buyer.placeOrder("");
        assertTrue(buyer.updateStatus(order.orderId, OrderStatus.DELIVERING));
        assertTrue(buyer.updateStatus(order.orderId, OrderStatus.COMPLETED));
        Review review = new Review(order.orderId, "coffee-03", "Cà phê muối", "Khách",
                5, Collections.singletonList("Ngon"), "Ổn", Collections.emptyList());
        buyer.submitReview(review);
        assertTrue(((OrderRepository) buyer).hasReview(order.orderId));
        assertEquals(5, ((OrderRepository) buyer).getReview(order.orderId).rating);
        OrderStatistics stats = ((OrderRepository) buyer).getStatistics();
        assertTrue(stats.completedRevenue >= 39_000L);
        assertTrue(stats.completedCount >= 1);
    }

    @Test public void legacyWithoutMetadataIsReadableButNotResettable() throws Exception {
        Order order = new Order("LEGACY", System.currentTimeMillis(), "07",
                Collections.emptyList(), 0L, 0L, "", OrderStatus.COMPLETED);
        JSONObject object = order.toJson();
        object.remove("customerName"); object.remove("paymentMethod"); object.remove("isDemo");
        JSONArray saved = new JSONArray(); saved.put(object);
        context.getSharedPreferences(testPrefs, Context.MODE_PRIVATE).edit()
                .putString("orders", saved.toString()).commit();
        Order restored = buyer.getOrder("LEGACY");
        assertNotNull(restored);
        assertFalse(restored.isDemo);
        assertEquals(PaymentMethod.UNSPECIFIED, restored.paymentMethod);
        assertFalse(buyer.resetDemoOrder("LEGACY"));
    }

    @Test public void demoSeedIsIdempotent() {
        LocalBuyerOrderRepository reloaded = new LocalBuyerOrderRepository(context, testPrefs);
        int countA = 0, countB = 0;
        for (Order order : reloaded.getOrders()) {
            if ("TS1025".equals(order.orderId)) countA++;
            if ("TS1024".equals(order.orderId)) countB++;
        }
        assertEquals(1, countA);
        assertEquals(1, countB);
        assertTrue(reloaded.hasReview("TS1008"));
    }
}
