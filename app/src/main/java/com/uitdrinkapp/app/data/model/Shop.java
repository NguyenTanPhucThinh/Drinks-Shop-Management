package com.uitdrinkapp.app.data.model;

public final class Shop {
    private final String id;
    private final String code;
    private final String name;

    public Shop(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
    public String getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
