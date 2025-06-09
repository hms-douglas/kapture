
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
import dev.dect.kapture.data.KSharedPreferences;

@SuppressLint("LaunchActivityFromNotification")
public class ReceivingNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    private NotificationCompat.Builder NOTIFICATION_BUILDER;

    public ReceivingNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.Notification.Channel.RECEIVING, CTX.getString(R.string.notification_channel_name_receiving), NotificationManager.IMPORTANCE_DEFAULT));
    }

    public void createAndShow() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.PROCESSING);

        if(!KSharedPreferences.getAppSp(CTX).getBoolean(Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_PROCESSING, DefaultSettings.IS_TO_SHOW_NOTIFICATION_PROCESSING)) {
            return;
        }

        NOTIFICATION_BUILDER = new NotificationCompat.Builder(CTX, Constants.Notification.Channel.PROCESSING);

        NOTIFICATION_BUILDER.setSmallIcon(R.mipmap.ic_launcher);
        NOTIFICATION_BUILDER.setPriority(NotificationManager.IMPORTANCE_DEFAULT);
        NOTIFICATION_BUILDER.setColorized(true);
        NOTIFICATION_BUILDER.setColor(CTX.getColor(R.color.notification_background));
        NOTIFICATION_BUILDER.setShowWhen(true);
        NOTIFICATION_BUILDER.setWhen(new Date().getTime());
        NOTIFICATION_BUILDER.setOngoing(true);
        NOTIFICATION_BUILDER.setSubText(CTX.getString(R.string.notification_receiving));
        NOTIFICATION_BUILDER.setContentText(CTX.getString(R.string.notification_receiving_message));
        NOTIFICATION_BUILDER.setLights(CTX.getColor(R.color.notification_background), 1000, 1000);
        NOTIFICATION_BUILDER.setProgress(0, 0, true);

        NOTIFICATION_MANAGER.notify(Constants.Notification.Id.RECEIVING, NOTIFICATION_BUILDER.build());
    }

    public void notifyReceived(int amount) {
        NOTIFICATION_BUILDER.setOngoing(false);

        NOTIFICATION_BUILDER.setSubText(CTX.getString(R.string.notification_receiving_success));
        NOTIFICATION_BUILDER.setProgress(0, 0, false);

        if(amount == 1) {
            NOTIFICATION_BUILDER.setContentText(CTX.getString(R.string.notification_receiving_success_message_singular));
        } else {
            NOTIFICATION_BUILDER.setContentText(
                CTX.getString(R.string.notification_receiving_success_message_plural).replaceFirst("%d", String.valueOf(amount))
            );
        }

        NOTIFICATION_MANAGER.notify(Constants.Notification.Id.RECEIVING, NOTIFICATION_BUILDER.build());
    }

    public void notifyError() {
        NOTIFICATION_BUILDER.setOngoing(false);

        NOTIFICATION_BUILDER.setSubText(CTX.getString(R.string.notification_receiving_error));
        NOTIFICATION_BUILDER.setContentText(CTX.getString(R.string.notification_receiving_error_message));
        NOTIFICATION_BUILDER.setProgress(0, 0, false);

        NOTIFICATION_MANAGER.notify(Constants.Notification.Id.RECEIVING, NOTIFICATION_BUILDER.build());
    }

    public void destroy() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.PROCESSING);
    }
}

