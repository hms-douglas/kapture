
package dev.dect.kapture.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSharedPreferences;

@SuppressLint("LaunchActivityFromNotification")
public class ShortcutOverlayNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    public ShortcutOverlayNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.Notification.Channel.OVERLAY, CTX.getString(R.string.notification_channel_name_overlay), NotificationManager.IMPORTANCE_LOW));
    }

    public Notification create() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.SHORTCUT);

        final NotificationCompat.Builder notificationCompact = new NotificationCompat.Builder(CTX, Constants.Notification.Channel.OVERLAY);

        notificationCompact.setSmallIcon(R.mipmap.ic_launcher);
        notificationCompact.setPriority(NotificationManager.IMPORTANCE_LOW);
        notificationCompact.setSilent(true);
        notificationCompact.setContentText(CTX.getString(R.string.notification_channel_name_overlay));

        return notificationCompact.build();
    }

    public void destroy() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.SHORTCUT);
    }
}

