package com.uitdrinkapp.app.ui.common;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.uitdrinkapp.app.R;
import com.uitdrinkapp.app.data.model.OrderStatus;

/** Device-local notifications for successful order actions. No account switching or push transport. */
public final class OrderSystemNotifications {
    private static final String TAG = "OrderSystemNotifications";
    private static final String SELLER_CHANNEL = "task03_seller_orders_v1";
    private static final String BUYER_CHANNEL = "task03_buyer_orders_v1";
    private static final String PERMISSION_PREFS = "task03_notification_preferences";
    private static final String ASKED = "permission_requested";
    private static final int REQUEST_PERMISSION = 7305;

    private OrderSystemNotifications() { }

    /** Called from relevant foreground screens; do not repeatedly prompt after denial. */
    public static void prepare(Activity activity) {
        try {
            ensureChannels(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                SharedPreferences prefs = activity.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE);
                if (!prefs.getBoolean(ASKED, false)) {
                    prefs.edit().putBoolean(ASKED, true).apply();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_PERMISSION);
                }
            }
        } catch (RuntimeException exception) {
            Log.w(TAG, "Unable to prepare notifications", exception);
        }
    }

    private static void ensureChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        createChannel(context, manager, SELLER_CHANNEL, R.string.task03_notif_channel_seller);
        createChannel(context, manager, BUYER_CHANNEL, R.string.task03_notif_channel_buyer);
    }

    private static void createChannel(Context context, NotificationManager manager, String id, int name) {
        // Android 8+ channels retain user-selected sound/importance; never reset them.
        if (manager.getNotificationChannel(id) != null) return;
        NotificationChannel channel = new NotificationChannel(id, context.getString(name),
                NotificationManager.IMPORTANCE_HIGH);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), attributes);
        channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
        manager.createNotificationChannel(channel);
    }

    public static void orderCreated(Context context, String orderId) {
        publish(context, SELLER_CHANNEL, orderId, "created", R.string.task03_notif_seller_title,
                R.string.task03_notif_order_created);
    }

    public static void buyerCancelled(Context context, String orderId) {
        publish(context, SELLER_CHANNEL, orderId, "buyer_cancelled", R.string.task03_notif_seller_title,
                R.string.task03_notif_buyer_cancelled);
    }

    public static void sellerStatusChanged(Context context, String orderId, OrderStatus status) {
        if (status == null) return;
        final int body;
        switch (status) {
            case ACCEPTED: body = R.string.task03_notif_accepted; break;
            case PREPARING:
            case DELIVERING: body = R.string.task03_notif_preparing; break;
            case READY: body = R.string.task03_notif_ready; break;
            case COMPLETED: body = R.string.task03_notif_completed; break;
            case REJECTED: body = R.string.task03_notif_rejected; break;
            case CANCELLED: body = R.string.task03_notif_seller_cancelled; break;
            default: return;
        }
        publish(context, BUYER_CHANNEL, orderId, "seller_" + status.name(),
                R.string.task03_notif_buyer_title, body);
    }

    private static void publish(Context context, String channel, String orderId, String event,
                                int titleId, int bodyId) {
        if (orderId == null || orderId.trim().isEmpty()) return;
        // Notification failures must not turn a successful order into an apparent failure.
        try {
            Context app = context.getApplicationContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) return;
            NotificationManager manager = app.getSystemService(NotificationManager.class);
            if (manager == null || !manager.areNotificationsEnabled()) return;
            ensureChannels(app);
            String body = app.getString(bodyId, orderId);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(app, channel)
                    .setSmallIcon(R.drawable.ic_cup)
                    .setContentTitle(app.getString(titleId))
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true);
            // Respect the existing login/role flow. Do not open a different account or bypass auth.
            Intent launch = app.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
            if (launch != null) {
                PendingIntent openApp = PendingIntent.getActivity(app, 0, launch,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                builder.setContentIntent(openApp);
            }
            // Stable tag per order/event: repeated posts update rather than stack duplicates.
            manager.notify(channel + ":" + orderId + ":" + event, 1, builder.build());
        } catch (RuntimeException exception) {
            Log.w(TAG, "Unable to publish order notification", exception);
        }
    }
}
