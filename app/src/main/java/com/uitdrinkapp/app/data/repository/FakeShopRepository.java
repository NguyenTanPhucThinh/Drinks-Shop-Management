package com.uitdrinkapp.app.data.repository;
import com.uitdrinkapp.app.data.mock.ShopMockData;
import com.uitdrinkapp.app.data.model.Shop;
public final class FakeShopRepository implements ShopRepository {
    public Shop findByCode(String code) { return ShopMockData.SHOP.getCode().equals(code) ? ShopMockData.SHOP : null; }
    public Shop findById(String id) { return ShopMockData.SHOP.getId().equals(id) ? ShopMockData.SHOP : null; }
    public String getDemoCode() { return ShopMockData.DEMO_SHOP_CODE; }
    public String getDemoTable() { return ShopMockData.DEMO_TABLE; }
}
