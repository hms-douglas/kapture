package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.wearable.input.RotaryEncoderHelper;

import dev.dect.kapture.R;

@SuppressLint("InflateParams")
public class VolumePopup extends Dialog {
    private final AudioManager AUDIO_MANAGER;

    private final ImageView PROGRESS;

    private final Canvas CANVAS;

    private final Bitmap BITMAP;

    private final float SWEEP_ANGLE,
                        TOP_LEFT,
                        BOTTOM_RIGHT,
                        START_ANGLE;

    private final Paint PAINT_BACKGROUND,
                        PAINT_PROGRESS;

    private CountDownTimer AUTO_CLOSE = null;

    private int VOLUME;

    @Override
    public boolean onGenericMotionEvent(@NonNull MotionEvent event) {
        setVolume(RotaryEncoderHelper.getRotaryAxisValue(event) < 0 ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER);

        return true;
    }


    public VolumePopup(Context ctx) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_volume, null);

        AUDIO_MANAGER = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        PROGRESS = view.findViewById(R.id.progress);

        final int screenSize = ctx.getSystemService(WindowManager.class).getCurrentWindowMetrics().getBounds().width(),
                  halfScreenSize = screenSize / 2;

        final float strokeWidth = 20,
                    radius = halfScreenSize - strokeWidth;

        BITMAP = Bitmap.createBitmap(screenSize, screenSize, Bitmap.Config.ARGB_8888);

        CANVAS = new Canvas(BITMAP);

        SWEEP_ANGLE = 75f;

        START_ANGLE = 180f - (SWEEP_ANGLE / 2f);

        TOP_LEFT = halfScreenSize - radius;
        BOTTOM_RIGHT = halfScreenSize + radius;

        PAINT_BACKGROUND = new Paint();
        PAINT_BACKGROUND.setStyle(Paint.Style.STROKE);
        PAINT_BACKGROUND.setStrokeWidth(strokeWidth);
        PAINT_BACKGROUND.setColor(ctx.getColor(R.color.group_container_background));
        PAINT_BACKGROUND.setStrokeCap(Paint.Cap.ROUND);

        PAINT_PROGRESS = new Paint();
        PAINT_PROGRESS.setStyle(Paint.Style.STROKE);
        PAINT_PROGRESS.setStrokeWidth(strokeWidth);
        PAINT_PROGRESS.setColor(ctx.getColor(R.color.main));
        PAINT_PROGRESS.setStrokeCap(Paint.Cap.ROUND);

        final ConstraintLayout btnUp = view.findViewById(R.id.volume_up),
                               btnDown = view.findViewById(R.id.volume_down);

        view.setOnGenericMotionListener((view1, motionEvent) -> {
            if(RotaryEncoderHelper.getRotaryAxisValue(motionEvent) > 0) {
                btnDown.callOnClick();
            } else {
                btnUp.callOnClick();
            }

            return true;
        });

        btnUp.setOnClickListener((v) -> setVolume(v, AudioManager.ADJUST_RAISE));

        btnDown.setOnClickListener((v) -> setVolume(v, AudioManager.ADJUST_LOWER));

        this.setCancelable(true);

        this.setContentView(view);
    }

    @Override
    public void show() {
        super.show();

        VOLUME = (15 * AUDIO_MANAGER.getStreamVolume(AudioManager.STREAM_MUSIC)) / AUDIO_MANAGER.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        drawProgress(AudioManager.ADJUST_SAME);

        setAutoClose();
    }

    @Override
    public void dismiss() {
        if(AUTO_CLOSE != null) {
            try {
                AUTO_CLOSE.cancel();
            } catch (Exception ignore) {}
        }

        super.dismiss();
    }

    private void setAutoClose() {
        if(AUTO_CLOSE != null) {
            try {
                AUTO_CLOSE.cancel();
            } catch (Exception ignore) {}
        }

        AUTO_CLOSE = new CountDownTimer(2500, 500) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                if(AUTO_CLOSE != null) {
                    dismiss();
                }

                AUTO_CLOSE = null;
            }
        }.start();
    }

    private void setVolume(View v, int adjust) {
        v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

        setVolume(adjust);
    }

    private void setVolume(int adjust) {
        setAutoClose();

        AUDIO_MANAGER.adjustVolume(adjust, AudioManager.FLAG_PLAY_SOUND);

        drawProgress(adjust);
    }

    private void drawProgress(int adjust) {
        if(adjust == AudioManager.ADJUST_LOWER) {
            VOLUME = Math.max(0, VOLUME - 1);
        } else if(adjust == AudioManager.ADJUST_RAISE) {
            VOLUME = Math.min(15, VOLUME + 1);
        }

        CANVAS.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        CANVAS.drawArc(TOP_LEFT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_RIGHT, START_ANGLE, SWEEP_ANGLE, false, PAINT_BACKGROUND);

        CANVAS.drawArc(TOP_LEFT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_RIGHT, START_ANGLE, (SWEEP_ANGLE * VOLUME) / 15, false, PAINT_PROGRESS);

        PROGRESS.setImageBitmap(BITMAP);
    }
}
