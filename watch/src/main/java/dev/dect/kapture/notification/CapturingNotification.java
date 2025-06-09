package dev.dect.kapture.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import dev.dect.kapture.data.Constants;

@SuppressLint("LaunchActivityFromNotification")
public class CapturingNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    public CapturingNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.Notification.Channel.CAPTURING, "blah", NotificationManager.IMPORTANCE_LOW));
    }

    public Notification create() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.CAPTURING);

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(CTX, Constants.Notification.Channel.CAPTURING);

        notificationBuilder.setPriority(NotificationManager.IMPORTANCE_LOW);
        notificationBuilder.setColorized(true);
        notificationBuilder.setOngoing(true);

        return notificationBuilder.build();
    }

    public void destroy() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.CAPTURING);
    }
}


