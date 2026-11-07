package com.uitdrinkapp.app.data.model;

public final class Product {
    private final String id;
    private final String name;
    private final long price;
    private final String imageUrl;
    private final String description;
    private final String categoryId;
    private final long priceS;
    private final long priceM;
    private final long priceL;
    private final boolean inStock;

    public Product(String id, String name, long price, String imageUrl, String description, String categoryId) {
        this(id, name, price, imageUrl, description, categoryId,
                price, price + Size.M.getExtraPrice(), price + Size.L.getExtraPrice(), true);
    }

    public Product(String id, String name, long price, String imageUrl, String description,
                   String categoryId, long priceS, long priceM, long priceL, boolean inStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.categoryId = categoryId;
        this.priceS = Math.max(0L, priceS);
        this.priceM = Math.max(0L, priceM);
        this.priceL = Math.max(0L, priceL);
        this.inStock = inStock;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public long getPriceS() { return priceS; }
    public long getPriceM() { return priceM; }
    public long getPriceL() { return priceL; }
    public long getPriceForSize(Size size) {
        if (size == null) return priceS;
        switch (size) {
            case M: return priceM;
            case L: return priceL;
            case S:
            default: return priceS;
        }
    }
    public boolean isInStock() { return inStock; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String getCategoryId() { return categoryId; }
}
