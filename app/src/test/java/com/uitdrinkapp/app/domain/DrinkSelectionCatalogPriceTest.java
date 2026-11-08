package com.uitdrinkapp.app.domain;

import static org.junit.Assert.assertEquals;
import com.uitdrinkapp.app.data.model.Product;
import com.uitdrinkapp.app.data.model.Size;
import com.uitdrinkapp.app.data.model.Topping;
import java.util.Arrays;
import org.junit.Test;

public final class DrinkSelectionCatalogPriceTest {
    @Test public void usesSellerSizePricesAndToppings() {
        Product p = new Product("tea", "Tea", 39000L, "", "", "tea",
                39000L, 45000L, 50000L, true);
        DrinkSelection selection = new DrinkSelection(p);
        selection.setSize(Size.M);
        selection.setTopping(new Topping("pearl", "Pearl", 5000L), true);
        selection.setTopping(new Topping("pudding", "Pudding", 7000L), true);
        selection.setQuantity(2);
        assertEquals(114000L, selection.getTotalPrice());
    }
    @Test public void legacyProductFallsBackToSurcharge() {
        Product p = new Product("tea", "Tea", 35000L, "", "", "tea");
        DrinkSelection selection = new DrinkSelection(p);
        selection.setSize(Size.L);
        assertEquals(45000L, selection.getTotalPrice());
    }
}
