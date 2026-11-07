package com.uitdrinkapp.app.data.repository;

import com.uitdrinkapp.app.data.model.Notification;
import java.util.List;

public interface NotificationRepository {
    List<Notification> getNotifications(Notification.RecipientType recipientType, String recipientId);
    void markAsRead(String notificationId);
    void markAllAsRead(Notification.RecipientType recipientType, String recipientId);
    int getUnreadCount(Notification.RecipientType recipientType, String recipientId);
}
