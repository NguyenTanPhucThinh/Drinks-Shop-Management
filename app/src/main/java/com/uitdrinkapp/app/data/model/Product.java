package com.uitdrinkapp.app.data.model;

public final class Product {
    private final String id;
    private final String name;
    private final long price;
    private final String imageUrl;
    private final String description;
    private final String categoryId;

    public Product(String id, String name, long price, String imageUrl, String description, String categoryId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.categoryId = categoryId;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String getCategoryId() { return categoryId; }
}
