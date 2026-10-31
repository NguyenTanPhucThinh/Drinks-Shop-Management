package com.uitdrinkapp.app.domain;
import com.uitdrinkapp.app.data.model.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class DrinkSelectionTest {
    private DrinkSelection selection() {
        return new DrinkSelection(new Product("tea", "Tea", 35000L, "", "", "tea"));
    }
    @Test public void totalsUseSizeAndToppingsBeforeQuantity() {
        DrinkSelection selection = selection();
        selection.setTopping(new Topping("pearls", "Pearls", 5000L), true);
        assertEquals(40000L, selection.getTotalPrice());
        selection.setSize(Size.M);
        selection.setTopping(new Topping("pudding", "Pudding", 7000L), true);
        selection.setQuantity(2);
        assertEquals(104000L, selection.getTotalPrice());
        selection.setSugar(SugarLevel.P0);
        selection.setTemperature(Temperature.HOT);
        assertEquals(104000L, selection.getTotalPrice());
        selection.setTopping(new Topping("pearls", "Pearls", 5000L), false);
        assertEquals(94000L, selection.getTotalPrice());
    }
    @Test public void duplicateToppingIsNotChargedTwiceAndQuantityCannotBeZero() {
        DrinkSelection selection = selection();
        Topping topping = new Topping("pearls", "Pearls", 5000L);
        selection.setTopping(topping, true);
        selection.setTopping(topping, true);
        selection.setQuantity(0);
        assertEquals(1, selection.getQuantity());
        assertEquals(40000L, selection.getTotalPrice());
    }
}
