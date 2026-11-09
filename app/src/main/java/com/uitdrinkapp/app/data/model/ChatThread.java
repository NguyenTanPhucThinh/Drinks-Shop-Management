package com.uitdrinkapp.app.data.model;

public final class ChatThread {
    public final String id;
    public final String shopName;
    public final String lastMessage;
    public final long lastMessageAt;
    public final int unreadCount;

    public ChatThread(String id, String shopName, String lastMessage,
                      long lastMessageAt, int unreadCount) {
        this.id = id;
        this.shopName = shopName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }
}