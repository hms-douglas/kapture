
package dev.dect.kapture.notification;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;

import androidx.core.app.NotificationCompat;

import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.ActionActivity;
import dev.dect.kapture.activity.viewer.VideoActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.model.Kapture;

@SuppressLint("LaunchActivityFromNotification")
public class CapturedNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    public CapturedNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.Notification.Channel.CAPTURED, CTX.getString(R.string.notification_channel_name_captured), NotificationManager.IMPORTANCE_LOW));
    }

    public void createAndShow(Kapture kapture) {
        if(!KSharedPreferences.getAppSp(CTX).getBoolean(Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_CAPTURED, DefaultSettings.IS_TO_SHOW_NOTIFICATION_CAPTURED)) {
            return;
        }

        final int id = (int) kapture.getId();

        final NotificationCompat.Builder notificationCompact = new NotificationCompat.Builder(CTX, Constants.Notification.Channel.CAPTURED);

        notificationCompact.setSmallIcon(R.mipmap.ic_launcher);
        notificationCompact.setPriority(NotificationManager.IMPORTANCE_LOW);
        notificationCompact.setColorized(true);
        notificationCompact.setColor(CTX.getColor(R.color.notification_background));
        notificationCompact.setShowWhen(true);
        notificationCompact.setWhen(new Date().getTime());
        notificationCompact.setSubText(CTX.getString(R.string.notification_saved));
        notificationCompact.setContentText(CTX.getString(R.string.notification_tap_show));
        notificationCompact.setLights(CTX.getColor(R.color.notification_background), 1000, 1000);
        notificationCompact.setAutoCancel(true);

        try {
            final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

            mediaMetadataRetriever.setDataSource(kapture.getLocation());

            notificationCompact.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(mediaMetadataRetriever.getFrameAtTime()));

            mediaMetadataRetriever.close();
        } catch (Exception ignore) {}

        final Intent iPlay = new Intent(CTX, VideoActivity.class);

        iPlay.putExtra(VideoActivity.INTENT_URL, kapture.getLocation());

        iPlay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        final PendingIntent ipPlay =  PendingIntent.getActivity(
            CTX,
            id + 1,
            iPlay,
            PendingIntent.FLAG_IMMUTABLE
        );

        notificationCompact.setContentIntent(ipPlay);

        notificationCompact.addAction(
            0,
            CTX.getString(R.string.notification_btn_play),
            ipPlay
        );

        if(kapture.hasExtras()) {
            final Intent iExtra = new Intent(CTX, ActionActivity.class);

            iExtra.putExtra(ActionActivity.INTENT_KAPTURE_ID, kapture.getId());
            iExtra.putExtra(ActionActivity.INTENT_ACTION, Constants.Notification.Action.EXTRA);
            iExtra.putExtra(ActionActivity.INTENT_NOTIFICATION_ID, id);
            iExtra.putExtra(ActionActivity.INTENT_FROM, ActionActivity.FROM_NOTIFICATION);

            iExtra.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            notificationCompact.addAction(
                0,
                CTX.getString(R.string.notification_btn_extra),
                PendingIntent.getActivity(
                    CTX,
                        id + 2,
                    iExtra,
                    PendingIntent.FLAG_IMMUTABLE
                )
            );
        } else {
            final Intent iShare = new Intent(CTX, ActionActivity.class);

            iShare.putExtra(ActionActivity.INTENT_KAPTURE_ID, kapture.getId());
            iShare.putExtra(ActionActivity.INTENT_ACTION, Constants.Notification.Action.SHARE);
            iShare.putExtra(ActionActivity.INTENT_NOTIFICATION_ID, id);
            iShare.putExtra(ActionActivity.INTENT_FROM, ActionActivity.FROM_NOTIFICATION);

            iShare.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            notificationCompact.addAction(
                0,
                CTX.getString(R.string.notification_btn_share),
                PendingIntent.getActivity(
                    CTX,
                        id + 3,
                    iShare,
                    PendingIntent.FLAG_IMMUTABLE
                )
            );
        }

        final Intent iDelete = new Intent(CTX, ActionActivity.class);

        iDelete.putExtra(ActionActivity.INTENT_KAPTURE_ID, kapture.getId());
        iDelete.putExtra(ActionActivity.INTENT_ACTION, Constants.Notification.Action.DELETE);
        iDelete.putExtra(ActionActivity.INTENT_NOTIFICATION_ID, id);
        iDelete.putExtra(ActionActivity.INTENT_FROM, ActionActivity.FROM_NOTIFICATION);

        iDelete.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        notificationCompact.addAction(
            0,
            CTX.getString(R.string.notification_btn_delete),
            PendingIntent.getActivity(
                CTX,
                id + 4,
                iDelete,
                PendingIntent.FLAG_IMMUTABLE
            )
        );

        NOTIFICATION_MANAGER.notify(id, notificationCompact.build());
    }
}

