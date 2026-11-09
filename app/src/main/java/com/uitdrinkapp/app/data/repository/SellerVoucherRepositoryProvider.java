package com.uitdrinkapp.app.data.repository;

import android.content.Context;

public final class SellerVoucherRepositoryProvider {
    private SellerVoucherRepositoryProvider() {}
    public static SellerVoucherRepository get(Context context) {
        return LocalSellerVoucherRepository.getInstance(context);
    }
}
