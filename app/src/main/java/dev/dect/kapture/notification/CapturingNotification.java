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

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.Utils;

@SuppressLint("LaunchActivityFromNotification")
public class CapturingNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    private NotificationCompat.Builder NOTIFICATION_BUILDER;

    private final BroadcastReceiver NOTIFICATION_RECEIVER;

    private long TIME_WHEN = 0,
                 TIME_PASSED = 0;

    public CapturingNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        this.NOTIFICATION_RECEIVER = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent i) {
            switch(Objects.requireNonNull(i.getAction())) {
                case Constants.Notification.Action.STOP:
                    CapturingService.requestStopRecording();
                    break;

                case Constants.Notification.Action.PAUSE:
                    CapturingService.requestPauseRecording();
                    break;

                case Constants.Notification.Action.RESUME:
                    CapturingService.requestResumeRecording();
                    break;
            }
            }
        };

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.Notification.Channel.CAPTURING, CTX.getString(R.string.notification_channel_name_capturing), NotificationManager.IMPORTANCE_LOW));
    }

    public Notification create() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.CAPTURING);

        TIME_WHEN = System.currentTimeMillis();

        NOTIFICATION_BUILDER = new NotificationCompat.Builder(CTX, Constants.Notification.Channel.CAPTURING);

        NOTIFICATION_BUILDER.setSmallIcon(R.drawable.animation_notification_icon);
        NOTIFICATION_BUILDER.setPriority(NotificationManager.IMPORTANCE_LOW);
        NOTIFICATION_BUILDER.setColorized(true);
        NOTIFICATION_BUILDER.setColor(CTX.getColor(R.color.notification_background));
        NOTIFICATION_BUILDER.setOngoing(true);
        NOTIFICATION_BUILDER.setShowWhen(true);
        NOTIFICATION_BUILDER.setWhen(TIME_WHEN);
        NOTIFICATION_BUILDER.setUsesChronometer(true);
        NOTIFICATION_BUILDER.setContentText(CTX.getString(R.string.notification_recording_action));
        NOTIFICATION_BUILDER.setSubText(CTX.getString(R.string.notification_recording_recording));

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Constants.Notification.Action.STOP);
        intentFilter.addAction(Constants.Notification.Action.PAUSE);
        intentFilter.addAction(Constants.Notification.Action.RESUME);

        CTX.registerReceiver(NOTIFICATION_RECEIVER, intentFilter, Context.RECEIVER_NOT_EXPORTED);

        addActions();

        NOTIFICATION_BUILDER.setContentIntent(
            PendingIntent.getBroadcast(
                CTX,
                0,
                new Intent(Constants.Notification.Action.STOP),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
            )
        );

        return NOTIFICATION_BUILDER.build();
    }

    public void destroy() {
        NOTIFICATION_MANAGER.cancel(Constants.Notification.Id.CAPTURING);

        try {
            CTX.unregisterReceiver(NOTIFICATION_RECEIVER);
        } catch (Exception ignore) {}
    }

    public void refreshRecordingState() {
        if(CapturingService.isPaused()) {
            TIME_PASSED += System.currentTimeMillis() - TIME_WHEN;

            NOTIFICATION_BUILDER.setSubText(CTX.getString(R.string.notification_recording_paused) + "    " + Utils.Formatter.timeInMillis(TIME_PASSED));
            NOTIFICATION_BUILDER.setShowWhen(false);
            NOTIFICATION_BUILDER.setUsesChronometer(false);
            NOTIFICATION_BUILDER.setSmallIcon(R.drawable.icon_notification_paused);
        } else {
            TIME_WHEN = System.currentTimeMillis();

            NOTIFICATION_BUILDER.setWhen(TIME_WHEN - TIME_PASSED);
            NOTIFICATION_BUILDER.setSubText(CTX.getString(R.string.notification_recording_recording));
            NOTIFICATION_BUILDER.setShowWhen(true);
            NOTIFICATION_BUILDER.setUsesChronometer(true);
            NOTIFICATION_BUILDER.setSmallIcon(R.drawable.animation_notification_icon);
        }

        addActions();

        NOTIFICATION_MANAGER.notify(Constants.Notification.Id.CAPTURING, NOTIFICATION_BUILDER.build());
    }

    private void addActions() {
        NOTIFICATION_BUILDER.clearActions();

        NOTIFICATION_BUILDER.addAction(
            0,
            CTX.getString(R.string.notification_recording_stop),
            PendingIntent.getBroadcast(
                CTX,
                1,
                new Intent(Constants.Notification.Action.STOP),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
            )
        );

        if(CapturingService.isPaused()) {
            NOTIFICATION_BUILDER.addAction(
                0,
                CTX.getString(R.string.notification_recording_resume),
                PendingIntent.getBroadcast(
                    CTX,
                    2,
                    new Intent(Constants.Notification.Action.RESUME),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                )
            );
        } else {
            NOTIFICATION_BUILDER.addAction(
                0,
                CTX.getString(R.string.notification_recording_pause),
                PendingIntent.getBroadcast(
                    CTX,
                    3,
                    new Intent(Constants.Notification.Action.PAUSE),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                )
            );
        }
    }
}


