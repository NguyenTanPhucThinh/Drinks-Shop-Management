package com.uitdrinkapp.app.data.repository;

import com.uitdrinkapp.app.data.model.User;

public interface AuthRepository {
    User login(String email, String password);   // null nếu sai
    boolean register(String fullName, String email, String phone, String password); // false nếu email đã có
    boolean isLoggedIn();
    User getCurrentUser();
    User updateProfile(String fullName, String phone);
    void logout();
}