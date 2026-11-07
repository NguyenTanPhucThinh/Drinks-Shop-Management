package com.uitdrinkapp.app.data.model;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import static org.junit.Assert.*;

public class ProductCatalogModelTest {
    @Test public void legacyConstructorKeepsOldSizeSurcharges() {
        Product item = new Product("tea", "Tea", 35_000L, "", "", "tea");
        assertEquals(35_000L, item.getPriceFor(Size.S));
        assertEquals(40_000L, item.getPriceFor(Size.M));
        assertEquals(45_000L, item.getPriceFor(Size.L));
        assertEquals(35_000L, item.getPrice());
        assertTrue(item.isAvailable());
        assertTrue(item.isVisible());
    }
    @Test public void explicitSellerPriceOverridesOnlyConfiguredSize() {
        Map<Size, Long> prices = new EnumMap<>(Size.class);
        prices.put(Size.S, 39_000L);
        prices.put(Size.M, 45_000L);
        Product item = new Product("tea", "Tea", 39_000L, "", "", "tea",
                prices, Arrays.asList(Temperature.ICED),
                Collections.singletonList(SugarLevel.P50),
                Collections.singletonList("black_pearl"), true, false);
        assertEquals(39_000L, item.getPrice());
        assertEquals(45_000L, item.getPriceForSize(Size.M));
        assertEquals(49_000L, item.getPriceFor(Size.L));
        assertEquals(1, item.getTemperatures().size());
        assertFalse(item.isVisible());
    }
    @Test public void oldExplicitPriceConstructorRemainsSupported() {
        Product item = new Product("tea", "Tea", 30_000L, "", "", "tea",
                31_000L, 37_000L, 44_000L, false);
        assertEquals(31_000L, item.getPrice());
        assertEquals(37_000L, item.getPriceM());
        assertEquals(44_000L, item.getPriceL());
        assertFalse(item.isAvailable());
    }
    @Test public void toppingIsSeparateAndLegacyAvailable() {
        assertTrue(new Topping("a", "A", 5_000L).isAvailable());
        assertFalse(new Topping("b", "B", 7_000L, false).isAvailable());
    }
}
