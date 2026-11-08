package com.uitdrinkapp.app.data.model;

public final class User {
    public final String id;
    public final String fullName;
    public final String email;
    public final String phone;

    public User(String id, String fullName, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }
}