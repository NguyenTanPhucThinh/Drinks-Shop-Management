package com.uitdrinkapp.app.data.model;

/** Metadata only. Buyer radio-button mapping has not been agreed by the team. */
public enum PaymentMethod {
    UNSPECIFIED, CASH, TRANSFER, EWALLET;

    public static PaymentMethod fromStorage(String raw) {
        try { return valueOf(raw == null ? "UNSPECIFIED" : raw); }
        catch (IllegalArgumentException ignored) { return UNSPECIFIED; }
    }
}
