package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.service.CapturingService;

@SuppressLint("InflateParams")
public class OverlayPopup {
    private final KSettings KSETTINGS;

    private final Context CONTEXT;

    private final WindowManager WINDOW_MANAGER;

    private View VIEW = null;

    public OverlayPopup(Context ctx, KSettings ks) {
        this.CONTEXT = ctx;
        this.KSETTINGS = ks;
        this.WINDOW_MANAGER = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
    }

    private void render() {
        VIEW = LayoutInflater.from(CONTEXT).inflate(R.layout.overlay_recording_controls, null, false);

        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final SharedPreferences sp = CONTEXT.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE);

        if(sp.contains(Constants.SP_KEY_OVERLAY_X_POS)) {
            layoutParams.x = sp.getInt(Constants.SP_KEY_OVERLAY_X_POS, 0);
            layoutParams.y = sp.getInt(Constants.SP_KEY_OVERLAY_Y_POS, 0);
        } else {
            layoutParams.gravity = Gravity.TOP;
        }

        final int[] coordinates = new int[4];

        final long[] time = new long[1];

        VIEW.findViewById(R.id.btnStop).setOnTouchListener((v, e) -> {
            switch(e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    coordinates[0] = layoutParams.x;
                    coordinates[1] = layoutParams.y;
                    coordinates[2] = (int) e.getRawX();
                    coordinates[3] = (int) e.getRawY();

                    time[0] = new Date().getTime();
                    break;

                case MotionEvent.ACTION_MOVE:
                    layoutParams.x = (int) (coordinates[0] + (e.getRawX() - coordinates[2]));
                    layoutParams.y = (int) (coordinates[1] + (e.getRawY() - coordinates[3]));

                    WINDOW_MANAGER.updateViewLayout(VIEW, layoutParams);
                    break;

                case MotionEvent.ACTION_UP:
                    if(new Date().getTime() - time[0] <= 200) {
                        v.performClick();
                    } else {
                        final SharedPreferences.Editor editor = sp.edit();

                        editor.putInt(Constants.SP_KEY_OVERLAY_X_POS, layoutParams.x);
                        editor.putInt(Constants.SP_KEY_OVERLAY_Y_POS, layoutParams.y);

                        editor.apply();
                    }
            }

            return true;
        });

        VIEW.findViewById(R.id.btnStop).setOnClickListener((v) -> CapturingService.requestStopRecording());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                WINDOW_MANAGER.addView(VIEW, layoutParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1500);
    }

    public void requestRender() {
        if(KSETTINGS.isToShowFloatingButton()) {
            render();
        }
    }

    public void destroy() {
        if(VIEW != null) {
            WINDOW_MANAGER.removeView(VIEW);

            VIEW = null;
        }
    }
}
