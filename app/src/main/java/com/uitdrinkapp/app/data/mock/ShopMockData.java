package com.uitdrinkapp.app.data.mock;

import com.uitdrinkapp.app.data.model.Shop;

/** Mock shop used by the join-shop flow. */
public final class ShopMockData {
    private ShopMockData() { }
    public static final String DEMO_SHOP_CODE = "TS-0123";
    public static final String DEMO_TABLE = "05";
    public static final Shop SHOP = new Shop("abc", DEMO_SHOP_CODE, "Quán Trà Sữa ABC");
}
