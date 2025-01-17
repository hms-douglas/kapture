package dev.dect.kapture.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class ImageOverlay {
    private final KSettings KSETTINGS;

    private final Context CONTEXT;

    private final WindowManager WINDOW_MANAGER;

    private View VIEW;

    public ImageOverlay(Context ctx, KSettings ks, WindowManager wm) {
        this.KSETTINGS = ks;
        this.CONTEXT = ctx;
        this.WINDOW_MANAGER = wm;
    }

    public void render() {
        if(!KSETTINGS.isToShowImage()) {
            return;
        }

        VIEW = LayoutInflater.from(CONTEXT).inflate(R.layout.overlay_recording_image, null, false);

        final ImageView imageView = VIEW.findViewById(R.id.image);

        imageView.setImageBitmap(BitmapFactory.decodeFile(KSETTINGS.getImagePath(false)));

        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Utils.Overlay.setBasicLayoutParameters(layoutParams);

        setLayoutParametersSize(layoutParams);

        Utils.Overlay.setLayoutParametersPosition(
            layoutParams,
            CONTEXT,
            Constants.Sp.Profile.OVERLAY_IMAGE_X_POS,
            Constants.Sp.Profile.OVERLAY_IMAGE_Y_POS
        );

        Utils.Overlay.setDefaultDraggableView(
            VIEW,
            layoutParams,
            WINDOW_MANAGER,
            Constants.Sp.Profile.OVERLAY_IMAGE_X_POS,
            Constants.Sp.Profile.OVERLAY_IMAGE_Y_POS
        );

        new Handler(Looper.getMainLooper()).postDelayed(() -> WINDOW_MANAGER.addView(VIEW, layoutParams), 300);
    }

    public void destroy() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            WINDOW_MANAGER.removeViewImmediate(VIEW);
        }
    }

    private void setLayoutParametersSize(WindowManager.LayoutParams layoutParams) {
        final int size = Utils.Converter.dpToPx(CONTEXT, KSETTINGS.getImageSize());

        layoutParams.height = size;
        layoutParams.width = size;
    }
}
