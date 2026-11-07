package com.uitdrinkapp.app.data.repository;

import com.uitdrinkapp.app.data.model.Notification;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FakeNotificationRepository implements NotificationRepository {
    public static final String DEMO_SELLER_ID = "seller-demo";
    public static final String DEMO_BUYER_ID = "buyer-demo";
    private final List<Notification> notifications = new ArrayList<>();

    public FakeNotificationRepository() {
        long now = System.currentTimeMillis();
        add(Notification.RecipientType.SELLER, DEMO_SELLER_ID, Notification.Category.ORDER,
                Notification.Type.ORDER_CREATED, "Có đơn mới #TS1025", "Bàn 02 · 2 món · 91.000đ",
                now - 60_000L, false, Notification.ReferenceType.ORDER, "TS1025",
                data("tableNumber", "02", "itemCount", "2", "totalAmount", "91000"));
        add(Notification.RecipientType.SELLER, DEMO_SELLER_ID, Notification.Category.SYSTEM,
                Notification.Type.REVIEW_RECEIVED, "Khách đánh giá 5 sao", "\"Trà sữa matcha rất ngon!\"",
                now - 3_600_000L, true, Notification.ReferenceType.REVIEW, "RV1021",
                data("rating", "5"));
        add(Notification.RecipientType.SELLER, DEMO_SELLER_ID, Notification.Category.SYSTEM,
                Notification.Type.VOUCHER_USAGE_LIMIT, "Voucher GIAM10 sắp hết lượt", "Đã dùng 92/100",
                now - 86_400_000L, true, Notification.ReferenceType.VOUCHER, "GIAM10",
                data("usedCount", "92", "usageLimit", "100"));
        add(Notification.RecipientType.SELLER, DEMO_SELLER_ID, Notification.Category.SYSTEM,
                Notification.Type.SALES_REPORT, "Báo cáo doanh thu hôm qua", "1.480.000đ · 34 đơn",
                now - 86_400_000L, true, Notification.ReferenceType.REPORT, "sales-yesterday",
                data("revenue", "1480000", "orderCount", "34"));
        add(Notification.RecipientType.BUYER, DEMO_BUYER_ID, Notification.Category.ORDER,
                Notification.Type.ORDER_STATUS_CHANGED, "Đơn #TS1024 đang được pha chế",
                "Dự kiến giao sau ~25 phút", now - 15 * 60_000L, false,
                Notification.ReferenceType.ORDER, "TS1024", data("status", "PREPARING"));
    }

    private void add(Notification.RecipientType recipientType, String recipientId,
                      Notification.Category category, Notification.Type type,
                      String title, String body, long createdAt, boolean isRead,
                      Notification.ReferenceType referenceType, String referenceId,
                      Map<String, String> data) {
        notifications.add(new Notification(referenceId + "-" + type.name(), recipientType,
                recipientId, category, type, title, body, createdAt, isRead,
                referenceType, referenceId, data));
    }

    private static Map<String, String> data(String... keyValues) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) values.put(keyValues[i], keyValues[i + 1]);
        return values;
    }

    @Override public synchronized List<Notification> getNotifications(
            Notification.RecipientType recipientType, String recipientId) {
        List<Notification> result = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.recipientType == recipientType
                    && notification.recipientId.equals(recipientId == null ? "" : recipientId)) {
                result.add(notification);
            }
        }
        result.sort(Comparator.comparingLong((Notification value) -> value.createdAt).reversed());
        return Collections.unmodifiableList(result);
    }

    @Override public synchronized void markAsRead(String notificationId) {
        for (int i = 0; i < notifications.size(); i++) {
            Notification item = notifications.get(i);
            if (item.notificationId.equals(notificationId)) {
                notifications.set(i, item.asRead());
                return;
            }
        }
    }

    @Override public synchronized void markAllAsRead(
            Notification.RecipientType recipientType, String recipientId) {
        for (int i = 0; i < notifications.size(); i++) {
            Notification item = notifications.get(i);
            if (item.recipientType == recipientType
                    && item.recipientId.equals(recipientId == null ? "" : recipientId)) {
                notifications.set(i, item.asRead());
            }
        }
    }

    @Override public synchronized int getUnreadCount(
            Notification.RecipientType recipientType, String recipientId) {
        int count = 0;
        for (Notification item : notifications) {
            if (item.recipientType == recipientType && !item.isRead
                    && item.recipientId.equals(recipientId == null ? "" : recipientId)) count++;
        }
        return count;
    }
}
