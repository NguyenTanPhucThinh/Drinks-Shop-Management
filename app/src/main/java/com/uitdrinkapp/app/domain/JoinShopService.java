package com.uitdrinkapp.app.domain;

import com.uitdrinkapp.app.data.model.Shop;
import com.uitdrinkapp.app.data.model.ShopSession;
import com.uitdrinkapp.app.data.repository.ShopRepository;
import com.uitdrinkapp.app.data.repository.ShopSessionRepository;
import java.util.Locale;

/** Demo entry only: no form validation; unmatched codes use the demo shop. */
public final class JoinShopService {
    private final ShopRepository shops;
    private final ShopSessionRepository sessions;
    public JoinShopService(ShopRepository shops, ShopSessionRepository sessions) {
        this.shops = shops; this.sessions = sessions;
    }
    public void join(String codeInput, String tableInput) {
        String code = codeInput == null ? "" : codeInput.trim().toUpperCase(Locale.ROOT);
        Shop shop = shops.findByCode(code);
        if (shop == null) shop = shops.findByCode(shops.getDemoCode());
        sessions.saveSession(new ShopSession(shop.getId(), tableInput == null ? "" : tableInput.trim()));
    }
}
