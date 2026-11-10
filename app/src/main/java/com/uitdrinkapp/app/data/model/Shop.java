package com.uitdrinkapp.app.data.model;

public final class Shop {
    private final String id;
    private final String code;
    private final String name;
    private final String address;
    private final String hours;

    public Shop(String id, String code, String name) {
        this(id, code, name, "12 Võ Văn Ngân, Thủ Đức, TP.HCM", "07:00 - 22:00");
    }

    public Shop(String id, String code, String name, String address, String hours) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        this.hours = hours;
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getHours() { return hours; }
}
