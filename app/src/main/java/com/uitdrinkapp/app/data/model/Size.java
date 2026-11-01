package com.uitdrinkapp.app.data.model;

public enum Size {
    S(0L), M(5000L), L(10000L);
    private final long extraPrice;
    Size(long extraPrice) { this.extraPrice = extraPrice; }
    public long getExtraPrice() { return extraPrice; }
}
