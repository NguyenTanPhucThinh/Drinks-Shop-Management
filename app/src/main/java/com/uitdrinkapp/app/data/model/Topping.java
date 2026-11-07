package com.uitdrinkapp.app.data.model;

/** Toppings remain separate from products; legacy constructor is retained. */
public final class Topping {
    private final String id;
    private final String name;
    private final long price;
    private final boolean available;
    public Topping(String id, String name, long price) { this(id, name, price, true); }
    public Topping(String id, String name, long price, boolean available) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
        this.price = Math.max(0L, price);
        this.available = available;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public boolean isAvailable() { return available; }
}
