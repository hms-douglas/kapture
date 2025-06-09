package dev.dect.kapture.utils;

import android.app.Activity;
import android.content.Context;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

import java.util.Objects;

import dev.dect.kapture.activity.TokenActivity;

public class KMediaProjection {
    private static MediaProjection MEDIA_PROJECTION;

    public static void generate(Context ctx) {
        MEDIA_PROJECTION = ((MediaProjectionManager) ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(Activity.RESULT_OK, Objects.requireNonNull(TokenActivity.getToken()));
    }

    public static void destroy() {
        MEDIA_PROJECTION.stop();
        MEDIA_PROJECTION = null;
    }

    public static MediaProjection get() {
        return MEDIA_PROJECTION;
    }
}
