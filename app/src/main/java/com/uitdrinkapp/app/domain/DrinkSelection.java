package com.uitdrinkapp.app.domain;

import com.uitdrinkapp.app.data.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Single pricing source for the detail screen and future cart integration. */
public final class DrinkSelection {
    public static final int MAX_TOPPINGS = 3;
    private final Product product;
    private int quantity = 1;
    private Size size = Size.S;
    private SugarLevel sugar = SugarLevel.P50;
    private Temperature temperature = Temperature.ICED;
    private FulfillmentMethod fulfillment = FulfillmentMethod.AT_COUNTER;
    private final List<Topping> toppings = new ArrayList<>();
    public DrinkSelection(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(1, Math.min(99, quantity)); }
    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }
    public SugarLevel getSugar() { return sugar; }
    public void setSugar(SugarLevel sugar) { this.sugar = sugar; }
    public Temperature getTemperature() { return temperature; }
    public void setTemperature(Temperature temperature) { this.temperature = temperature; }
    public FulfillmentMethod getFulfillment() { return fulfillment; }
    public void setFulfillment(FulfillmentMethod fulfillment) { this.fulfillment = fulfillment; }
    public List<Topping> getToppings() { return Collections.unmodifiableList(toppings); }
    public boolean hasTopping(String id) {
        for (Topping topping : toppings) if (topping.getId().equals(id)) return true;
        return false;
    }
    public boolean setTopping(Topping topping, boolean checked) {
        if (!checked) { toppings.removeIf(item -> item.getId().equals(topping.getId())); return true; }
        if (hasTopping(topping.getId())) return true;
        if (toppings.size() == MAX_TOPPINGS) return false;
        toppings.add(topping);
        return true;
    }
    public long getTotalPrice() {
        long unitPrice = product.getPrice() + size.getExtraPrice();
        for (Topping topping : toppings) unitPrice += topping.getPrice();
        return unitPrice * quantity;
    }
}
