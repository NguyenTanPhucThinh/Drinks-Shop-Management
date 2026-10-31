package com.uitdrinkapp.app.data.model;
public final class Topping {
    private final String id;
    private final String name;
    private final long price;
    public Topping(String id, String name, long price) { this.id = id; this.name = name; this.price = price; }
    public String getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
}
