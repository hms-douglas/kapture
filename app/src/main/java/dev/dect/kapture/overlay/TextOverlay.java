package dev.dect.kapture.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class TextOverlay {
    private final KSettings KSETTINGS;

    private final Context CONTEXT;

    private final WindowManager WINDOW_MANAGER;

    private View VIEW;

    public TextOverlay(Context ctx, KSettings ks, WindowManager wm) {
        this.KSETTINGS = ks;
        this.CONTEXT = ctx;
        this.WINDOW_MANAGER = wm;
    }

    public void render() {
        if(!KSETTINGS.isToShowText()) {
            return;
        }

        VIEW = LayoutInflater.from(CONTEXT).inflate(R.layout.overlay_recording_text, null, false);

        final TextView textView = VIEW.findViewById(R.id.text);

        textView.setText(KSETTINGS.getTextText());
        textView.setTextSize(KSETTINGS.getTextSize());
        textView.setTextColor(Utils.Converter.hexColorToInt(KSETTINGS.getTextColor()));
        textView.setBackgroundColor(Utils.Converter.hexColorToInt(KSETTINGS.getTextBackground()));
        textView.setTypeface(KSETTINGS.getTextFontTypeface());
        textView.setGravity(KSETTINGS.getTextAlignment());

        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Utils.Overlay.setBasicLayoutParameters(layoutParams);

        Utils.Overlay.setLayoutParametersPosition(
            layoutParams,
            CONTEXT,
            Constants.SP_KEY_OVERLAY_TEXT_X_POS,
            Constants.SP_KEY_OVERLAY_TEXT_Y_POS
        );

        Utils.Overlay.setDefaultDraggableView(
            VIEW,
            layoutParams,
            WINDOW_MANAGER,
            Constants.SP_KEY_OVERLAY_TEXT_X_POS,
            Constants.SP_KEY_OVERLAY_TEXT_Y_POS
        );

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            WINDOW_MANAGER.addView(VIEW, layoutParams);
        }, 300);
    }

    public void destroy() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            WINDOW_MANAGER.removeViewImmediate(VIEW);
        }
    }
}
