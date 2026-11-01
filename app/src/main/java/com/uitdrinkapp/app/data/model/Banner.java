package com.uitdrinkapp.app.data.model;

public final class Banner {
    private final String imageKey;
    private final String title;
    private final String subtitle;

    public Banner(String imageKey, String title, String subtitle) {
        this.imageKey = imageKey;
        this.title = title;
        this.subtitle = subtitle;
    }
    public String getImageKey() { return imageKey; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
}
