package com.uitdrinkapp.app.data.repository;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.model.Size;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** For the later instrumentation-test round; does not change the app's existing tests. */
@RunWith(AndroidJUnit4.class)
public class LocalProductRepositoryTest {
    private Context context;
    @Before public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences("seller_product_catalog_v1", Context.MODE_PRIVATE)
                .edit().clear().commit();
    }
    @Test public void sellerChangesSurviveNewRepositoryInstance() {
        LocalProductRepository first = new LocalProductRepository(context);
        Map<Size, Long> prices = new EnumMap<>(Size.class);
        prices.put(Size.S, 39_000L); prices.put(Size.M, 45_000L); prices.put(Size.L, 50_000L);
        Product item = new Product("new_tea", "New Tea", 39_000L, "", "", "tea",
                prices, null, null, Collections.singletonList("black_pearl"), true, false);
        first.saveProduct(item);
        LocalProductRepository second = new LocalProductRepository(context);
        assertNotNull(second.getProductForSeller("new_tea"));
        assertNull(second.getProductById("new_tea")); // hidden from Buyer
        assertEquals(45_000L, second.getProductForSeller("new_tea").getPriceFor(Size.M));
        assertTrue(second.deleteProduct("new_tea"));
        assertNull(new LocalProductRepository(context).getProductForSeller("new_tea"));
    }
}
