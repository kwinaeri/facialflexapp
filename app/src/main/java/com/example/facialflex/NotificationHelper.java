package com.example.facialflex;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    // Notification Channel ID and settings
    private static final String CHANNEL_ID = "daily_notification_channel";
    private static final String CHANNEL_NAME = "Daily Notification";
    private static final String CHANNEL_DESCRIPTION = "Channel for daily exercise reminders";

    // Method to create the notification channel for Android O and above
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Method to create the notification builder
    public static NotificationCompat.Builder getNotificationBuilder(Context context, String title, String message) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) // Make sure you have a suitable icon in your res/drawable folder
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // The notification will automatically be dismissed when tapped
    }

    // Method to issue the notification
    public static void sendNotification(Context context, String title, String message) {
        // Ensure notification channel is created before sending notifications
        createNotificationChannel(context);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, getNotificationBuilder(context, title, message).build());
    }
}
