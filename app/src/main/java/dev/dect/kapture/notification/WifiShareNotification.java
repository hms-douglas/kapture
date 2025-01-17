
package dev.dect.kapture.notification;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;

@SuppressLint("LaunchActivityFromNotification")
public class WifiShareNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    public WifiShareNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.Notification.Channel.WIFI_SHARE, CTX.getString(R.string.notification_channel_name_wifi_share), NotificationManager.IMPORTANCE_LOW));
    }

    public void createAndShow() {
        final NotificationCompat.Builder notificationCompact = new NotificationCompat.Builder(CTX, Constants.Notification.Channel.WIFI_SHARE);

        notificationCompact.setSmallIcon(R.mipmap.ic_launcher);
        notificationCompact.setPriority(NotificationManager.IMPORTANCE_LOW);
        notificationCompact.setShowWhen(true);
        notificationCompact.setWhen(new Date().getTime());
        notificationCompact.setOngoing(true);
        notificationCompact.setSilent(true);
        notificationCompact.setContentText(CTX.getString(R.string.notification_wifi_share));
        notificationCompact.setLights(CTX.getColor(R.color.notification_background), 1000, 1000);

        NOTIFICATION_MANAGER.notify(Constants.Notification.Id.WIFI_SHARE, notificationCompact.build());
    }

    public void destroy() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.WIFI_SHARE);
    }
}

