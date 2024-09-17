package dev.dect.kapture.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.Utils;

@SuppressLint("LaunchActivityFromNotification")
public class RecordingNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    private final BroadcastReceiver NOTIFICATION_RECEIVER;

    private final int ID;

    public RecordingNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        this.NOTIFICATION_RECEIVER = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent i) {
            if(Objects.requireNonNull(i.getAction()).equals(Constants.NOTIFICATION_CAPTURING_ACTION_STOP)) {
                CapturingService.requestStopRecording();
            }
            }
        };

        this.ID = Utils.generateTimeId();

        NOTIFICATION_MANAGER.cancelAll();

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_CAPTURING, CTX.getString(R.string.notification_channel_name_capturing), NotificationManager.IMPORTANCE_LOW));
    }

    public int getId() {
        return ID;
    }

    public Notification create() {
        final NotificationCompat.Builder notificationCompact = new NotificationCompat.Builder(CTX, Constants.NOTIFICATION_CHANNEL_ID_CAPTURING);

        notificationCompact.setSmallIcon(R.drawable.animation_notification_icon);
        notificationCompact.setPriority(NotificationManager.IMPORTANCE_LOW);
        notificationCompact.setColorized(true);
        notificationCompact.setColor(CTX.getColor(R.color.notification_background));
        notificationCompact.setOngoing(true);
        notificationCompact.setShowWhen(true);
        notificationCompact.setWhen(new Date().getTime());
        notificationCompact.setUsesChronometer(true);
        notificationCompact.setSubText(CTX.getString(R.string.notification_recording));
        notificationCompact.setContentText(CTX.getString(R.string.notification_tap_stop));
        notificationCompact.setLights(CTX.getColor(R.color.notification_background), 1000, 1000);
        notificationCompact.setTicker("");

        CTX.registerReceiver(NOTIFICATION_RECEIVER, new IntentFilter(Constants.NOTIFICATION_CAPTURING_ACTION_STOP), Context.RECEIVER_NOT_EXPORTED);

        notificationCompact.setContentIntent(
            PendingIntent.getBroadcast(
                CTX,
                ID,
                new Intent(Constants.NOTIFICATION_CAPTURING_ACTION_STOP),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
            )
        );

        return notificationCompact.build();
    }

    public void destroy() {
        NOTIFICATION_MANAGER.cancel(ID);

        try {
            CTX.unregisterReceiver(NOTIFICATION_RECEIVER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


