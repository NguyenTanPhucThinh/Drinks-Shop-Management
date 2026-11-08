package com.uitdrinkapp.app.ui.seller.product;

import com.uitdrinkapp.app.data.model.Temperature;
import com.uitdrinkapp.app.data.model.SugarLevel;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProductEditorFormPolicyTest {
    private ProductEditorFormPolicy.Result valid(String name, String s, String m, String l,
            boolean all, java.util.List<String> toppings) {
        return ProductEditorFormPolicy.validate(name, "tea", s, m, l,
                Arrays.asList(Temperature.HOT), Arrays.asList(SugarLevel.P50), all, toppings);
    }
    @Test public void validPricesAndNameAreNormalized() {
        ProductEditorFormPolicy.Result result = valid("  Trà sữa  ", "39000", "45000", "50000", true, Collections.emptyList());
        assertTrue(result.isValid()); assertEquals("Trà sữa", result.name);
        assertEquals(45000L, result.priceM);
    }
    @Test public void missingNameRejected() {
        assertEquals(ProductEditorFormPolicy.Issue.NAME,
                valid("  ", "39000", "45000", "50000", true, Collections.emptyList()).issue);
    }
    @Test public void invalidAndOverflowingPriceRejected() {
        assertEquals(ProductEditorFormPolicy.Issue.PRICE,
                valid("Món", "39000", "-1", "50000", true, Collections.emptyList()).issue);
        assertEquals(ProductEditorFormPolicy.Issue.PRICE,
                valid("Món", "39000", "1000000000", "50000", true, Collections.emptyList()).issue);
    }
    @Test public void noTemperatureAndSugarRejected() {
        assertEquals(ProductEditorFormPolicy.Issue.TEMPERATURE,
                ProductEditorFormPolicy.validate("Món", "tea", "1", "2", "3", Collections.emptyList(),
                        Arrays.asList(SugarLevel.P0),true,Collections.emptyList()).issue);
        assertEquals(ProductEditorFormPolicy.Issue.SUGAR,
                ProductEditorFormPolicy.validate("Món", "tea", "1", "2", "3", Arrays.asList(Temperature.HOT),
                        Collections.emptyList(),true,Collections.emptyList()).issue);
    }
    @Test public void explicitToppingRequiresAtLeastOneChoice() {
        assertEquals(ProductEditorFormPolicy.Issue.TOPPING,
                valid("Món", "39000", "45000", "50000", false, Collections.emptyList()).issue);
        assertTrue(valid("Món", "39000", "45000", "50000", false,
                Arrays.asList("black_pearl")).isValid());
    }
}
