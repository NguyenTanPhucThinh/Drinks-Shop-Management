package com.uitdrinkapp.app.data.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Review {
    public final String reviewId;
    public final String orderId;
    public final String productId;
    public final String productName;
    public final String customerName;
    public final int rating;
    public final List<String> tags;
    public final String comment;
    public final List<String> imageUris;
    public final long createdAt;

    public Review(String orderId, String productId, String productName, String customerName,
                  int rating, List<String> tags, String comment, List<String> imageUris) {
        this(UUID.randomUUID().toString(), orderId, productId, productName, customerName,
                rating, tags, comment, imageUris, System.currentTimeMillis());
    }

    private Review(String reviewId, String orderId, String productId, String productName,
                   String customerName, int rating, List<String> tags, String comment,
                   List<String> imageUris, long createdAt) {
        this.reviewId = reviewId == null || reviewId.isEmpty() ? UUID.randomUUID().toString() : reviewId;
        this.orderId = orderId == null ? "" : orderId;
        this.productId = productId == null ? "" : productId;
        this.productName = productName == null ? "" : productName;
        this.customerName = customerName == null ? "" : customerName;
        this.rating = Math.max(0, Math.min(5, rating));
        this.tags = Collections.unmodifiableList(tags == null ? new ArrayList<>() : new ArrayList<>(tags));
        this.comment = comment == null ? "" : comment;
        this.imageUris = Collections.unmodifiableList(
                imageUris == null ? new ArrayList<>() : new ArrayList<>(imageUris));
        this.createdAt = createdAt;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject value = new JSONObject();
        value.put("reviewId", reviewId);
        value.put("orderId", orderId);
        value.put("productId", productId);
        value.put("productName", productName);
        value.put("customerName", customerName);
        value.put("rating", rating);
        value.put("tags", new JSONArray(tags));
        value.put("comment", comment);
        value.put("imageUris", new JSONArray(imageUris));
        value.put("createdAt", createdAt);
        return value;
    }

    public Review withProduct(String id, String name) {
        return new Review(reviewId, orderId, id, name, customerName, rating, tags,
                comment, imageUris, createdAt);
    }

    public static Review fromJson(JSONObject value) {
        List<String> tags = readStrings(value.optJSONArray("tags"));
        List<String> imageUris = readStrings(value.optJSONArray("imageUris"));
        if (imageUris.isEmpty()) {
            String legacyImageUri = value.optString("imageUri", "");
            if (!legacyImageUri.isEmpty()) imageUris.add(legacyImageUri);
        }
        String orderId = value.optString("orderId");
        String reviewId = value.optString("reviewId", "review-" + orderId);
        return new Review(reviewId, orderId,
                value.optString("productId"), value.optString("productName"),
                value.optString("customerName"), value.optInt("rating", value.optInt("stars")),
                tags, value.optString("comment", value.optString("note")), imageUris,
                value.optLong("createdAt", System.currentTimeMillis()));
    }

    private static List<String> readStrings(JSONArray array) {
        List<String> values = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                String item = array.optString(i, "");
                if (!item.isEmpty()) values.add(item);
            }
        }
        return values;
    }
}
