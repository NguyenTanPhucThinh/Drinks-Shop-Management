package com.uitdrinkapp.app.ui.customer.order.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DemoReview {
    public final String orderId;
    public final int stars;
    public final List<String> tags;
    public final String note;
    public final String imageUri;
    public final long createdAt;

    public DemoReview(String orderId, int stars, List<String> tags, String note, String imageUri) {
        this(orderId, stars, tags, note, imageUri, System.currentTimeMillis());
    }
    private DemoReview(String orderId, int stars, List<String> tags, String note,
                       String imageUri, long createdAt) {
        this.orderId = orderId;
        this.stars = stars;
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
        this.note = note;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }
    JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("orderId", orderId); o.put("stars", stars);
        o.put("tags", new JSONArray(tags)); o.put("note", note);
        o.put("imageUri", imageUri); o.put("createdAt", createdAt);
        return o;
    }
    static DemoReview fromJson(JSONObject o) {
        List<String> tags = new ArrayList<>();
        JSONArray a = o.optJSONArray("tags");
        if (a != null) for (int i = 0; i < a.length(); i++) tags.add(a.optString(i));
        return new DemoReview(o.optString("orderId"), o.optInt("stars"), tags,
                o.optString("note"), o.optString("imageUri"), o.optLong("createdAt"));
    }
}
