package com.uitdrinkapp.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.uitdrinkapp.app.data.mock.ShopMockData;
import com.uitdrinkapp.app.data.model.Shop;

public final class FakeShopRepository implements ShopRepository {
    private static final String PREFS = "vy_shop_store";
    private static final String KEY_NAME = "shop_name";
    private static final String KEY_ADDRESS = "shop_address";
    private static final String KEY_HOURS = "shop_hours";
    private final SharedPreferences prefs;

    public FakeShopRepository() {
        this.prefs = null;
    }

    public FakeShopRepository(Context context) {
        this.prefs = context != null ? context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE) : null;
    }

    private Shop getShop() {
        String name = prefs != null ? prefs.getString(KEY_NAME, ShopMockData.SHOP.getName()) : ShopMockData.SHOP.getName();
        String address = prefs != null ? prefs.getString(KEY_ADDRESS, "12 Võ Văn Ngân, Thủ Đức, TP.HCM") : "12 Võ Văn Ngân, Thủ Đức, TP.HCM";
        String hours = prefs != null ? prefs.getString(KEY_HOURS, "07:00 - 22:00") : "07:00 - 22:00";
        return new Shop(ShopMockData.SHOP.getId(), ShopMockData.SHOP.getCode(), name, address, hours);
    }

    @Override
    public Shop findByCode(String code) {
        return ShopMockData.SHOP.getCode().equals(code) ? getShop() : null;
    }

    @Override
    public Shop findById(String id) {
        return ShopMockData.SHOP.getId().equals(id) ? getShop() : null;
    }

    @Override
    public String getDemoCode() {
        return ShopMockData.DEMO_SHOP_CODE;
    }

    @Override
    public String getDemoTable() {
        return ShopMockData.DEMO_TABLE;
    }

    @Override
    public void updateShopInfo(String name, String address, String hours) {
        if (prefs != null) {
            prefs.edit()
                    .putString(KEY_NAME, name.trim())
                    .putString(KEY_ADDRESS, address.trim())
                    .putString(KEY_HOURS, hours.trim())
                    .apply();
        }
    }
}
