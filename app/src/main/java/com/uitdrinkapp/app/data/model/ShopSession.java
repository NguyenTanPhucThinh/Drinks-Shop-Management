package com.uitdrinkapp.app.data.model;

public final class ShopSession {
    private final String shopId;
    private final String tableNumber;

    public ShopSession(String shopId, String tableNumber) {
        this.shopId = shopId;
        this.tableNumber = tableNumber;
    }
    public String getShopId() { return shopId; }
    public String getTableNumber() { return tableNumber; }
}
