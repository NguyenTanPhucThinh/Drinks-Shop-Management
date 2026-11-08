package com.uitdrinkapp.app.data.order;

import android.content.Context;

/** Returns the very same instance and backing store as the existing Buyer flow. */
public final class OrderRepositoryProvider {
    private OrderRepositoryProvider() { }

    public static OrderRepository get(Context context) {
        return BuyerOrderRepositoryProvider.get(context);
    }
}
