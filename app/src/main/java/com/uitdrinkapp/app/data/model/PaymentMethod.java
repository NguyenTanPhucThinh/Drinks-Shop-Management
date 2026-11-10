package com.uitdrinkapp.app.data.model;

/** Payment methods selected in Buyer Checkout and persisted with each order. */
public enum PaymentMethod {
    UNSPECIFIED, CASH, TRANSFER, EWALLET;

    public static PaymentMethod fromStorage(String raw) {
        try { return valueOf(raw == null ? "UNSPECIFIED" : raw); }
        catch (IllegalArgumentException ignored) { return UNSPECIFIED; }
    }
}
