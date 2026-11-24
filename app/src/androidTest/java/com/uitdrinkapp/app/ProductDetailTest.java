package com.uitdrinkapp.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.uitdrinkapp.app.data.model.FulfillmentMethod;
import com.uitdrinkapp.app.data.model.Temperature;
import com.uitdrinkapp.app.data.order.BuyerOrderRepository;
import com.uitdrinkapp.app.data.order.BuyerOrderRepositoryProvider;
import com.uitdrinkapp.app.data.order.CartLine;
import com.uitdrinkapp.app.data.repository.RepositoryProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ProductDetailTest {
    private BuyerOrderRepository cart;

    @Before public void loginBuyer() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertNotNull("Buyer demo login failed", RepositoryProvider.get(context)
                .auth().login("demo@uit.edu.vn", "123456"));
        cart = BuyerOrderRepositoryProvider.get(context);
    }

    @Test public void homeProductOpensDetailAndOptionsSurviveRecreation() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("Trà sữa trân châu")).perform(scrollTo(), click());
            onView(withId(R.id.tv_detail_name)).check(matches(withText("Trà sữa trân châu")));
            onView(withId(R.id.btn_detail_buy)).check(matches(withText("Đặt ngay · 40.000đ")));
            onView(withId(R.id.btn_quantity_minus)).check(matches(not(isEnabled())));
            scenario.onActivity(activity -> assertEquals(View.GONE, activity.findViewById(R.id.ll_bottom_nav).getVisibility()));
            capture();
            onView(withId(R.id.chip_size_m)).perform(scrollTo(), click());
            onView(withText("Pudding trứng")).perform(scrollTo(), click());
            onView(withId(R.id.btn_quantity_plus)).perform(scrollTo(), click());
            onView(withId(R.id.btn_detail_buy)).check(matches(withText("Đặt ngay · 104.000đ")));
            onView(withId(R.id.chip_hot)).perform(scrollTo(), click());
            onView(withId(R.id.chip_sugar_30)).perform(scrollTo(), click());
            onView(withId(R.id.chip_takeaway)).perform(scrollTo(), click());
            scenario.recreate();
            onView(withId(R.id.btn_detail_buy)).check(matches(withText("Đặt ngay · 104.000đ")));
            onView(withText("Pudding trứng")).check(matches(isChecked()));
            onView(withText("Trân châu đen")).check(matches(isChecked()));
            onView(withId(R.id.chip_size_m)).check(matches(isChecked()));
            onView(withId(R.id.chip_hot)).check(matches(isChecked()));
            onView(withId(R.id.chip_sugar_30)).check(matches(isChecked()));
            onView(withId(R.id.chip_takeaway)).check(matches(isChecked()));

            // The success path pops the detail fragment and dismisses its Snackbar.
            // Verify the persistent cart and the automatic return to Home instead.
            List<CartLine> before = cart.getCart();
            int beforeQuantity = matchingQuantity(before);
            long beforeSubtotal = cart.getSubtotal();
            Map<String, Integer> originalQuantities = new HashMap<>();
            for (CartLine line : before) originalQuantities.put(line.lineId, line.quantity);
            try {
                onView(withId(R.id.btn_detail_add_cart)).perform(click());
                onView(withId(R.id.vp_banners)).check(matches(isDisplayed()));
                List<CartLine> after = cart.getCart();
                assertEquals("Two matching items must be persisted", beforeQuantity + 2,
                        matchingQuantity(after));
                assertEquals("Cart subtotal must increase by 104000 VND",
                        beforeSubtotal + 104_000L, cart.getSubtotal());
            } finally {
                // Undo only the matching cart additions made by this test.
                // Do not clear the user's existing cart.
                for (CartLine line : cart.getCart()) {
                    if (!matchesSelection(line)) continue;
                    Integer original = originalQuantities.get(line.lineId);
                    if (original == null) cart.removeFromCart(line.lineId);
                    else if (line.quantity > original) cart.updateQuantity(line.lineId, original);
                }
            }
        }
    }

    private static int matchingQuantity(List<CartLine> lines) {
        int quantity = 0;
        for (CartLine line : lines) if (matchesSelection(line)) quantity += line.quantity;
        return quantity;
    }

    private static boolean matchesSelection(CartLine line) {
        if (!"milk_tea".equals(line.productId)
                || line.size != CartLine.Size.M
                || line.sugar != CartLine.Level.P30
                || line.temperature != Temperature.HOT
                || line.ice != CartLine.Level.P0
                || line.fulfillmentMethod != FulfillmentMethod.TAKEAWAY
                || line.unitPrice() != 52_000L
                || line.toppings.size() != 2) return false;
        boolean pearl = false, pudding = false;
        for (CartLine.Topping topping : line.toppings) {
            if ("Trân châu đen".equals(topping.name) && topping.price == 5_000L) pearl = true;
            if ("Pudding trứng".equals(topping.name) && topping.price == 7_000L) pudding = true;
        }
        return pearl && pudding;
    }

    private void capture() throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        File folder = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null), "screenshots");
        if (!folder.exists() && !folder.mkdirs()) throw new java.io.IOException("Cannot create screenshot directory");
        try (FileOutputStream file = new FileOutputStream(new File(folder, "product-detail.png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, file);
        } finally {
            bitmap.recycle();
        }
    }
}
