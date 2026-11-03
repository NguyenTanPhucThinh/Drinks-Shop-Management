package com.uitdrinkapp.app.ui.customer.order.data;

import android.content.Context;

/** Chuyển implementation sang Repository chung ở đây khi Owner Core bàn giao. */
public final class BuyerOrderRepositoryProvider {
    private BuyerOrderRepositoryProvider() { }
    public static BuyerOrderRepository get(Context context) {
        return LocalBuyerOrderRepository.getInstance(context.getApplicationContext());
    }
}
