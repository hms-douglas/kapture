
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
        import dev.dect.kapture.activity.NotificationActionActivity;
        import dev.dect.kapture.activity.viewer.VideoActivity;
        import dev.dect.kapture.data.Constants;
        import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.utils.Utils;

@SuppressLint("LaunchActivityFromNotification")
public class SavedNotification {
    private final Context CTX;

    private final NotificationManager NOTIFICATION_MANAGER;

    public SavedNotification(Context ctx) {
        this.CTX = ctx;
        this.NOTIFICATION_MANAGER = ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE));

        NOTIFICATION_MANAGER.createNotificationChannel(new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_CAPTURED, CTX.getString(R.string.notification_channel_name_captured), NotificationManager.IMPORTANCE_LOW));
    }

    public void createAndShow(Kapture kapture) {
        NOTIFICATION_MANAGER.cancelAll();

        final NotificationCompat.Builder notificationCompact = new NotificationCompat.Builder(CTX, Constants.NOTIFICATION_CHANNEL_ID_CAPTURED);

        notificationCompact.setSmallIcon(R.drawable.icon_notification_off);
        notificationCompact.setPriority(NotificationManager.IMPORTANCE_LOW);
        notificationCompact.setColorized(true);
        notificationCompact.setColor(CTX.getColor(R.color.notification_background));
        notificationCompact.setShowWhen(true);
        notificationCompact.setWhen(new Date().getTime());
        notificationCompact.setSubText(CTX.getString(R.string.notification_saved));
        notificationCompact.setContentText(CTX.getString(R.string.notification_tap_show));
        notificationCompact.setLights(CTX.getColor(R.color.notification_background), 1000, 1000);

        try {
            final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

            mediaMetadataRetriever.setDataSource(kapture.getLocation());

            notificationCompact.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(mediaMetadataRetriever.getFrameAtTime()));

            mediaMetadataRetriever.close();
        } catch (Exception ignore) {}

        final Intent iPlay = new Intent(CTX, VideoActivity.class);

        iPlay.putExtra(VideoActivity.INTENT_URL, kapture.getLocation());

        iPlay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        final PendingIntent ipPlay =  PendingIntent.getActivity(
            CTX,
            0,
            iPlay,
            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        notificationCompact.setContentIntent(ipPlay);

        notificationCompact.addAction(
            0,
            CTX.getString(R.string.notification_btn_play),
            ipPlay
        );

        if(kapture.hasExtras()) {
            final Intent iExtra = new Intent(CTX, NotificationActionActivity.class);

            iExtra.putExtra(NotificationActionActivity.INTENT_KAPTURE_ID, kapture.getId());
            iExtra.putExtra(NotificationActionActivity.INTENT_ACTION, Constants.NOTIFICATION_CAPTURED_ACTION_EXTRA);

            iExtra.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            notificationCompact.addAction(
                0,
                CTX.getString(R.string.notification_btn_extra),
                PendingIntent.getActivity(
                    CTX,
                    1,
                    iExtra,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
                )
            );
        } else {
            final Intent iShare = new Intent(CTX, NotificationActionActivity.class);

            iShare.putExtra(NotificationActionActivity.INTENT_KAPTURE_ID, kapture.getId());
            iShare.putExtra(NotificationActionActivity.INTENT_ACTION, Constants.NOTIFICATION_CAPTURED_ACTION_SHARE);

            iShare.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            notificationCompact.addAction(
                0,
                CTX.getString(R.string.notification_btn_share),
                PendingIntent.getActivity(
                    CTX,
                    1,
                    iShare,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
                )
            );
        }

        final Intent iDelete = new Intent(CTX, NotificationActionActivity.class);

        iDelete.putExtra(NotificationActionActivity.INTENT_KAPTURE_ID, kapture.getId());
        iDelete.putExtra(NotificationActionActivity.INTENT_ACTION, Constants.NOTIFICATION_CAPTURED_ACTION_DELETE);

        iDelete.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        notificationCompact.addAction(
            0,
            CTX.getString(R.string.notification_btn_delete),
            PendingIntent.getActivity(
                CTX,
                2,
                iDelete,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
            )
        );

        NOTIFICATION_MANAGER.notify(Utils.generateTimeId(), notificationCompact.build());
    }
}
