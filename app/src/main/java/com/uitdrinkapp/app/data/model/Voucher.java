package com.uitdrinkapp.app.data.model;

/** Display-only voucher contract for the mock UI; no checkout discount rules yet. */
public final class Voucher {
    private final String id;
    private final String title;
    private final String description;
    private final String expiryLabel;
    public Voucher(String id, String title, String description, String expiryLabel) {
        this.id = id; this.title = title; this.description = description; this.expiryLabel = expiryLabel;
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getExpiryLabel() { return expiryLabel; }
}
