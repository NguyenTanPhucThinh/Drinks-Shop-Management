package com.uitdrinkapp.app.data.model;

public final class User {

    public enum Role { BUYER, SELLER }

    public final String id;
    public final String fullName;
    public final String email;
    public final String phone;
    public final Role role;

    public User(String id, String fullName, String email, String phone, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role == null ? Role.BUYER : role;
    }

}