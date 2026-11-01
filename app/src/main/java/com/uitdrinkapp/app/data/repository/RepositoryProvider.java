package com.uitdrinkapp.app.data.repository;
import android.content.Context;

public final class RepositoryProvider {
    private static RepositoryProvider instance;
    private final ProductRepository products = new FakeProductRepository();
    private final VoucherRepository vouchers = new FakeVoucherRepository();
    private final HomeRepository home = new FakeHomeRepository();
    private final ShopRepository shops = new FakeShopRepository();
    private final ShopSessionRepository sessions;
    private RepositoryProvider(Context context) { sessions = new PreferencesShopSessionRepository(context, shops); }
    public static synchronized RepositoryProvider get(Context context) {
        if (instance == null) instance = new RepositoryProvider(context.getApplicationContext());
        return instance;
    }
    public ProductRepository products() { return products; }
    public VoucherRepository vouchers() { return vouchers; }
    public HomeRepository home() { return home; }
    public ShopRepository shops() { return shops; }
    public ShopSessionRepository sessions() { return sessions; }
}
