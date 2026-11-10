package com.uitdrinkapp.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.uitdrinkapp.app.data.model.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;

public final class LocalAuthRepository implements AuthRepository {
    // Đổi v1 -> v2 để máy đã chạy bản cũ được seed lại (có thêm tài khoản Seller)
    private static final String PREFS = "vy_auth_store_v2";
    private static final String KEY_USERS = "users";
    private static final String KEY_CURRENT = "current_email";
    private final SharedPreferences prefs;

    public LocalAuthRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.contains(KEY_USERS)) {
            JSONArray seed = new JSONArray();
            // id "buyer-demo" khớp FakeNotificationRepository.DEMO_BUYER_ID
            seed.put(toJson("buyer-demo", "Khách Demo", "demo@uit.edu.vn", "0900000000", "123456", "BUYER"));
            // id "seller-demo" khớp FakeNotificationRepository.DEMO_SELLER_ID
            seed.put(toJson("seller-demo", "Chủ quán Demo", "seller@uit.edu.vn", "0911111111", "123456", "SELLER"));
            prefs.edit().putString(KEY_USERS, seed.toString()).apply();
        }
    }

    private JSONArray users() {
        try { return new JSONArray(prefs.getString(KEY_USERS, "[]")); }
        catch (JSONException e) { return new JSONArray(); }
    }

    private static JSONObject toJson(String id, String fullName, String email, String phone,
                                     String password, String role) {
        JSONObject value = new JSONObject();
        try {
            value.put("id", id);
            value.put("fullName", fullName);
            value.put("email", email);
            value.put("phone", phone);
            value.put("password", password);
            value.put("role", role);
        } catch (JSONException ignored) { }
        return value;
    }

    private static User toUser(JSONObject value) {
        return new User(value.optString("id"), value.optString("fullName"),
                value.optString("email"), value.optString("phone"),
                roleOf(value.optString("role", "BUYER")));
    }

    private static User.Role roleOf(String name) {
        try { return User.Role.valueOf(name); }
        catch (IllegalArgumentException ignored) { return User.Role.BUYER; }
    }

    @Override public User login(String email, String password) {
        JSONArray all = users();
        for (int i = 0; i < all.length(); i++) {
            JSONObject value = all.optJSONObject(i);
            if (value != null && value.optString("email").equalsIgnoreCase(email.trim())
                    && value.optString("password").equals(password)) {
                prefs.edit().putString(KEY_CURRENT, value.optString("email")).apply();
                return toUser(value);
            }
        }
        return null;
    }

    @Override public boolean register(String fullName, String email, String phone, String password) {
        JSONArray all = users();
        for (int i = 0; i < all.length(); i++) {
            JSONObject value = all.optJSONObject(i);
            if (value != null && value.optString("email").equalsIgnoreCase(email.trim())) return false;
        }
        // Đăng ký luôn tạo tài khoản người mua
        all.put(toJson("buyer-" + UUID.randomUUID(), fullName.trim(), email.trim(), phone.trim(), password, "BUYER"));
        prefs.edit().putString(KEY_USERS, all.toString()).putString(KEY_CURRENT, email.trim()).apply();
        return true;
    }

    @Override public boolean isLoggedIn() { return getCurrentUser() != null; }

    @Override public User getCurrentUser() {
        String email = prefs.getString(KEY_CURRENT, "");
        JSONArray all = users();
        for (int i = 0; i < all.length(); i++) {
            JSONObject value = all.optJSONObject(i);
            if (value != null && value.optString("email").equalsIgnoreCase(email)) return toUser(value);
        }
        return null;
    }

    @Override public User updateProfile(String fullName, String phone) {
        String email = prefs.getString(KEY_CURRENT, "");
        JSONArray all = users();
        for (int i = 0; i < all.length(); i++) {
            JSONObject value = all.optJSONObject(i);
            if (value != null && value.optString("email").equalsIgnoreCase(email)) {
                try {
                    value.put("fullName", fullName.trim());
                    value.put("phone", phone.trim());
                } catch (JSONException ignored) { }
                prefs.edit().putString(KEY_USERS, all.toString()).apply();
                return toUser(value);
            }
        }
        return null;
    }

    @Override public void logout() { prefs.edit().remove(KEY_CURRENT).apply(); }
}