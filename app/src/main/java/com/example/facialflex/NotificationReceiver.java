// NotificationReceiver.java
package com.example.facialflex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Call the sendNotification method to display the notification
        String title = "Daily Exercise Reminder";
        String message = "Don't forget to do your daily facial exercises!";
        NotificationHelper.sendNotification(context, title, message);
    }
}
