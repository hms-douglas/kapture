package dev.dect.kapture.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Surface;
import android.view.WindowManager;

import dev.dect.kapture.data.KSettings;

@SuppressLint("InflateParams")
public class Overlay {
    private final MenuOverlay MENU_OVERLAY;

    private final CameraOverlay CAMERA_OVERLAY;

    private final TextOverlay TEXT_OVERLAY;

    private final ImageOverlay IMAGE_OVERLAY;

    public Overlay(Context ctx, KSettings ks) {
        final WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

        this.CAMERA_OVERLAY = new CameraOverlay(ctx, ks, windowManager);

        this.MENU_OVERLAY = new MenuOverlay(ctx, ks, windowManager, CAMERA_OVERLAY);
        this.TEXT_OVERLAY = new TextOverlay(ctx, ks, windowManager);
        this.IMAGE_OVERLAY = new ImageOverlay(ctx, ks, windowManager);
    }

    public void render() {
        MENU_OVERLAY.render();

        CAMERA_OVERLAY.render();

        TEXT_OVERLAY.render();

        IMAGE_OVERLAY.render();
    }

    public void destroy() {
        MENU_OVERLAY.destroy();

        CAMERA_OVERLAY.destroy();

        TEXT_OVERLAY.destroy();

        IMAGE_OVERLAY.render();
    }

    public void setMediaRecorderSurface(Surface s) {
        MENU_OVERLAY.setMediaRecorderSurface(s);
    }

    public void refreshRecordingState() {
        MENU_OVERLAY.refreshRecordingState();
    }
}
