package com.uitdrinkapp.app.data.model;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Notification {
    public enum RecipientType { BUYER, SELLER }
    public enum Category { ORDER, SYSTEM }
    public enum Type { ORDER_CREATED, ORDER_STATUS_CHANGED, REVIEW_RECEIVED, VOUCHER_USAGE_LIMIT, SALES_REPORT, GENERAL }
    public enum ReferenceType { NONE, ORDER, REVIEW, VOUCHER, REPORT }

    public final String notificationId;
    public final RecipientType recipientType;
    public final String recipientId;
    public final Category category;
    public final Type type;
    public final String title;
    public final String body;
    public final long createdAt;
    public final boolean isRead;
    public final ReferenceType referenceType;
    public final String referenceId;
    public final Map<String, String> data;

    public Notification(String notificationId, RecipientType recipientType, String recipientId,
                        Category category, Type type, String title, String body, long createdAt,
                        boolean isRead, ReferenceType referenceType, String referenceId,
                        Map<String, String> data) {
        this.notificationId = valueOrEmpty(notificationId);
        this.recipientType = recipientType == null ? RecipientType.BUYER : recipientType;
        this.recipientId = valueOrEmpty(recipientId);
        this.category = category == null ? Category.SYSTEM : category;
        this.type = type == null ? Type.GENERAL : type;
        this.title = valueOrEmpty(title);
        this.body = valueOrEmpty(body);
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.referenceType = referenceType == null ? ReferenceType.NONE : referenceType;
        this.referenceId = valueOrEmpty(referenceId);
        Map<String, String> safeData = data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data);
        this.data = Collections.unmodifiableMap(safeData);
    }

    public Notification asRead() {
        if (isRead) return this;
        return new Notification(notificationId, recipientType, recipientId, category, type,
                title, body, createdAt, true, referenceType, referenceId, data);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject value = new JSONObject();
        value.put("notificationId", notificationId);
        value.put("recipientType", recipientType.name());
        value.put("recipientId", recipientId);
        value.put("category", category.name());
        value.put("type", type.name());
        value.put("title", title);
        value.put("body", body);
        value.put("createdAt", createdAt);
        value.put("isRead", isRead);
        value.put("referenceType", referenceType.name());
        value.put("referenceId", referenceId);
        JSONObject dataValue = new JSONObject();
        for (Map.Entry<String, String> entry : data.entrySet()) dataValue.put(entry.getKey(), entry.getValue());
        value.put("data", dataValue);
        return value;
    }

    public static Notification fromJson(JSONObject value) {
        JSONObject dataValue = value.optJSONObject("data");
        Map<String, String> data = new LinkedHashMap<>();
        if (dataValue != null) {
            java.util.Iterator<String> keys = dataValue.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                data.put(key, dataValue.optString(key, ""));
            }
        }
        return new Notification(value.optString("notificationId"),
                enumValue(RecipientType.class, value.optString("recipientType"), RecipientType.BUYER),
                value.optString("recipientId"),
                enumValue(Category.class, value.optString("category"), Category.SYSTEM),
                enumValue(Type.class, value.optString("type"), Type.GENERAL),
                value.optString("title"), value.optString("body"), value.optLong("createdAt"),
                value.optBoolean("isRead"),
                enumValue(ReferenceType.class, value.optString("referenceType"), ReferenceType.NONE),
                value.optString("referenceId"), data);
    }

    private static String valueOrEmpty(String value) { return value == null ? "" : value; }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value, T fallback) {
        try { return Enum.valueOf(type, value); }
        catch (IllegalArgumentException ignored) { return fallback; }
    }
}
