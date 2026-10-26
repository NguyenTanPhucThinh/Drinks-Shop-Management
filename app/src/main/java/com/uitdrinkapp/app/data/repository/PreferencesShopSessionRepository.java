package com.uitdrinkapp.app.data.repository;
import android.content.Context;
import android.content.SharedPreferences;
import com.uitdrinkapp.app.data.model.ShopSession;

public final class PreferencesShopSessionRepository implements ShopSessionRepository {
    private final SharedPreferences preferences;
    private final ShopRepository shops;
    public PreferencesShopSessionRepository(Context context, ShopRepository shops) {
        this.preferences = context.getApplicationContext().getSharedPreferences("shop_session", Context.MODE_PRIVATE);
        this.shops = shops;
    }
    public ShopSession getSession() {
        String id = preferences.getString("shop_id", null);
        if (id == null || shops.findById(id) == null) return null;
        return new ShopSession(id, preferences.getString("table_number", ""));
    }
    public void saveSession(ShopSession session) {
        preferences.edit().putString("shop_id", session.getShopId())
                .putString("table_number", session.getTableNumber()).apply();
    }
}
