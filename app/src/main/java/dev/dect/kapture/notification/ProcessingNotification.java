
package dev.dect.kapture.notification;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.utils.Utils;

@SuppressLint("LaunchActivityFromNotification")
public class ProcessingNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    public ProcessingNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_PROCESSING, CTX.getString(R.string.notification_channel_name_processing), NotificationManager.IMPORTANCE_LOW));
    }

    public void createAndShow() {
        NOTIFICATION_MANAGER.cancelAll();

        if(!CTX.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE).getBoolean(Constants.SP_KEY_IS_TO_SHOW_NOTIFICATION_PROCESSING, DefaultSettings.IS_TO_SHOW_NOTIFICATION_PROCESSING)) {
            return;
        }

        final NotificationCompat.Builder notificationCompact = new NotificationCompat.Builder(CTX, Constants.NOTIFICATION_CHANNEL_ID_PROCESSING);

        notificationCompact.setSmallIcon(R.drawable.icon_notification_off);
        notificationCompact.setPriority(NotificationManager.IMPORTANCE_LOW);
        notificationCompact.setColorized(true);
        notificationCompact.setColor(CTX.getColor(R.color.notification_background));
        notificationCompact.setShowWhen(true);
        notificationCompact.setWhen(new Date().getTime());
        notificationCompact.setOngoing(true);
        notificationCompact.setSilent(true);
        notificationCompact.setSubText(CTX.getString(R.string.notification_processing));
        notificationCompact.setContentText(CTX.getString(R.string.notification_notification_processing_message));
        notificationCompact.setLights(CTX.getColor(R.color.notification_background), 1000, 1000);
        notificationCompact.setProgress(0, 0, true);

        NOTIFICATION_MANAGER.notify(Utils.generateTimeId(), notificationCompact.build());
    }
}

