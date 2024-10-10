package dev.dect.kapture.overlay;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import dev.dect.kapture.R;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class CountdownOverlay {
    private final KSettings KSETTINGS;

    private final Context CONTEXT;

    private final WindowManager WINDOW_MANAGER;

    private final Runnable COMPLETED;

    private final int ANIMATION_HELPER = 100;

    private View VIEW;

    private ProgressBar PROGRESS;

    private TextView TIME;

    private CountDownTimer TIMER;

    public CountdownOverlay(Context ctx, KSettings ks, Runnable completed) {
        this.KSETTINGS = ks;
        this.CONTEXT = ctx;
        this.WINDOW_MANAGER = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        this.COMPLETED = completed;
    }

    public void renderAndStart() {
        render();

        start();
    }

    private void start() {
        if(KSETTINGS.isToDelayStartRecording()) {
            TIMER = new CountDownTimer(KSETTINGS.getMilliSecondsToStartRecording(), 1000) {
                @Override
                public void onTick(long l) {
                    setProgress(l);
                }

                @Override
                public void onFinish() {
                    destroy();

                    COMPLETED.run();
                }
            }.start();
        } else {
            new Handler(Looper.getMainLooper()).post(COMPLETED);
        }
    }

    private void render() {
        if(!KSETTINGS.isToDelayStartRecording()) {
            return;
        }

        VIEW = LayoutInflater.from(CONTEXT).inflate(R.layout.overlay_recording_countdown, null, false);

        PROGRESS = VIEW.findViewById(R.id.progress);

        PROGRESS.setMax(KSETTINGS.getSecondsToStartRecording() * ANIMATION_HELPER);
        PROGRESS.setProgress(KSETTINGS.getSecondsToStartRecording() * ANIMATION_HELPER);

        TIME = VIEW.findViewById(R.id.time);

        TIME.setText(String.valueOf(KSETTINGS.getSecondsToStartRecording()));

        VIEW.findViewById(R.id.btnStop).setOnClickListener((l) -> {
            destroy();

            stop();

            COMPLETED.run();
        });

        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Utils.Overlay.setBasicLayoutParameters(layoutParams);

        new Handler(Looper.getMainLooper()).postDelayed(() -> WINDOW_MANAGER.addView(VIEW, layoutParams), 300);
    }

    private void destroy() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            WINDOW_MANAGER.removeViewImmediate(VIEW);
        }
    }

    private void stop() {
        if(TIMER != null) {
            try {
                TIMER.cancel();
            } catch (Exception ignore) {}
        }
    }

    private void setProgress(long milliseconds) {
        final int seconds = (int) (milliseconds / 1000);

        final ObjectAnimator animation = ObjectAnimator.ofInt(PROGRESS, "progress", PROGRESS.getProgress(), seconds * ANIMATION_HELPER);

        animation.setDuration(950);
        animation.setAutoCancel(true);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

        TIME.setText(String.valueOf(seconds + 1));
    }
}
