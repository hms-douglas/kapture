package dev.dect.kapture.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import dev.dect.kapture.data.Constants;
import dev.dect.kapture.notification.ShortcutOverlayNotification;
import dev.dect.kapture.overlay.ShortcutOverlay;

public class ShortcutOverlayService extends Service {
    private static final String EXTRA_REFRESH_UI = "refresh";

    private static boolean IS_RUNNING = false;

    private ShortcutOverlayNotification NOTIFICATION;

    private ShortcutOverlay SHORTCUT_OVERLAY;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NOTIFICATION = new ShortcutOverlayNotification(this);

        SHORTCUT_OVERLAY = new ShortcutOverlay(this);

        SHORTCUT_OVERLAY.render();

        IS_RUNNING = true;

        startForeground(Constants.Notification.Id.SHORTCUT, NOTIFICATION.create());
    }

    @Override
    public void onDestroy() {
        NOTIFICATION.destroy();

        SHORTCUT_OVERLAY.destroy();

        IS_RUNNING = false;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IS_RUNNING = true;

        if(CapturingService.isRecording() || CapturingService.isProcessing() || CapturingService.isInCountdown()) {
            SHORTCUT_OVERLAY.hide();
        } else {
            SHORTCUT_OVERLAY.show();
        }

        return START_STICKY;
    }

    public static void requestUiUpdate(Context ctx) {
        if(IS_RUNNING) {
            final Intent i = new Intent(ctx, ShortcutOverlayService.class);

            i.putExtra(EXTRA_REFRESH_UI, System.currentTimeMillis());

            ctx.startService(i);
        }
    }
}
