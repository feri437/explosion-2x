package com.gb90.smart2x;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class Notifier {
    public static final String CH_ID = "gb90_2x";

    public static void ensure(Context ctx){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(CH_ID, "GB90 2x Alerts", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Notification for entry 2");
            nm.createNotificationChannel(ch);
        }
    }

    public static void notifyEntry2(Context ctx, String title, String text){
        ensure(ctx);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CH_ID)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        nm.notify((int) System.currentTimeMillis(), b.build());
    }
}
