package dev.dect.kapture.overlay;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.service.ShortcutOverlayService;
import dev.dect.kapture.utils.Utils;

public class ShortcutOverlay {
    private final String TAG = ShortcutOverlay.class.getSimpleName();

    private final Context CONTEXT;

    private final View VIEW;

    private final ImageView CLOSE;

    private final WindowManager WINDOW_MANAGER;

    private int SIZE_CLOSE;

    private final int[] XY_CLOSE = new int[2];

    private boolean IN_RANGE_TO_REMOVE;

    public ShortcutOverlay(Context ctx) {
        this.CONTEXT = ctx;

        WINDOW_MANAGER = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);

        VIEW = LayoutInflater.from(CONTEXT).inflate(R.layout.overlay_shortcut, null, false);

        CLOSE = new ImageView(ctx);

        IN_RANGE_TO_REMOVE = false;

        init();
    }

    private void init() {
        VIEW.findViewById(R.id.btnStart).setOnClickListener((v) -> CapturingService.requestStartRecording(CONTEXT));

        SIZE_CLOSE = CONTEXT.getResources().getDimensionPixelSize(R.dimen.overlay_btn_remove_size);

        CLOSE.setImageResource(R.drawable.overlay_btn_remove);

        CLOSE.setVisibility(View.INVISIBLE);
    }

    private WindowManager.LayoutParams getCloseLayoutParameters() {
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Utils.Overlay.setBasicLayoutParameters(layoutParams);

        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        layoutParams.gravity = Gravity.BOTTOM|Gravity.CENTER;

        layoutParams.y = 300;

        return layoutParams;
    }

    private WindowManager.LayoutParams getBtnLayoutParameters() {
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Utils.Overlay.setBasicLayoutParameters(layoutParams);

        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        final int[] coordinates = new int[4];

        final long[] time = new long[1];

        VIEW.setOnTouchListener((v, e) -> {
            switch(e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    coordinates[0] = layoutParams.x;
                    coordinates[1] = layoutParams.y;
                    coordinates[2] = (int) e.getRawX();
                    coordinates[3] = (int) e.getRawY();

                    time[0] = System.currentTimeMillis();

                    animateBtnIn();

                    animateCloseIn();
                    break;

                case MotionEvent.ACTION_MOVE:
                    layoutParams.x = (int) (coordinates[0] + (e.getRawX() - coordinates[2]));
                    layoutParams.y = (int) (coordinates[1] + (e.getRawY() - coordinates[3]));

                    WINDOW_MANAGER.updateViewLayout(VIEW, layoutParams);

                    if(Utils.isInRange((int) e.getRawX(), XY_CLOSE[0], XY_CLOSE[0] + SIZE_CLOSE) && Utils.isInRange((int) e.getRawY(), XY_CLOSE[1], XY_CLOSE[1] + SIZE_CLOSE)) {
                        if(!IN_RANGE_TO_REMOVE) {
                            IN_RANGE_TO_REMOVE = true;

                            VIEW.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        }
                    } else {
                        IN_RANGE_TO_REMOVE = false;
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    if(IN_RANGE_TO_REMOVE) {
                        CONTEXT.stopService(new Intent(CONTEXT, ShortcutOverlayService.class));

                        return true;
                    }

                    animateCloseOut();

                    if(new Date().getTime() - time[0] <= 200) {
                        v.performClick();
                    } else {
                       animateBtnOut();
                    }

                    break;

                case MotionEvent.ACTION_CANCEL:
                    animateCloseOut();
                    animateBtnOut();

                    IN_RANGE_TO_REMOVE = false;
                    break;
            }

            return true;
        });

        return layoutParams;
    }

    public void render() {
        try {
            if(VIEW.getWindowToken() == null && VIEW.getParent() == null) {
                WINDOW_MANAGER.addView(CLOSE, getCloseLayoutParameters());

                calculateClosePosition();

                WINDOW_MANAGER.addView(VIEW, getBtnLayoutParameters());

                animateBtnOut();
            }
        } catch (Exception e) {
            Log.e(TAG, "render: " + e.getMessage());
        }
    }

    public void destroy() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            WINDOW_MANAGER.removeViewImmediate(VIEW);
            WINDOW_MANAGER.removeViewImmediate(CLOSE);
        }
    }

    public void hide() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            VIEW.setVisibility(View.GONE);
        }
    }

    public void show() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            VIEW.setVisibility(View.VISIBLE);
        }
    }

    private void calculateClosePosition() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            CLOSE.getLocationOnScreen(XY_CLOSE);

            CLOSE.setAlpha(0f);
            CLOSE.setScaleX(0f);
            CLOSE.setScaleY(0f);

            CLOSE.setVisibility(View.VISIBLE);
        }, 200);
    }

    private void animateBtnIn() {
        VIEW.animate().alpha(1f).setDuration(200).start();
    }

    private void animateBtnOut() {
        VIEW.clearAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                VIEW.animate().alpha(0.5f).setDuration(200).start();
            } catch (Exception e) {
                Log.e(TAG, "animateBtnOut: " + e.getMessage());
            }
        }, 1500);
    }

    private void animateCloseIn() {
        CLOSE.animate().scaleY(1f).scaleX(1f).alpha(1f).setDuration(500).start();
    }

    private void animateCloseOut() {
        CLOSE.animate().scaleY(0.5f).scaleX(0.5f).alpha(0f).setDuration(200).start();
    }
}
