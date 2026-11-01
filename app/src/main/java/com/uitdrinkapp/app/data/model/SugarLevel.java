package com.uitdrinkapp.app.data.model;

public enum SugarLevel {
    P0(0), P30(30), P50(50), P70(70), P100(100);
    private final int percent;
    SugarLevel(int percent) { this.percent = percent; }
    public int getPercent() { return percent; }
}
