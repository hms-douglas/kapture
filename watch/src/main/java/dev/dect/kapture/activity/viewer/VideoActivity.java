package dev.dect.kapture.activity.viewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.wearable.input.RotaryEncoderHelper;

import java.io.File;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.popup.VolumePopup;
import dev.dect.kapture.utils.Utils;

@SuppressLint("ClickableViewAccessibility")
public class VideoActivity extends Activity {
    public static final String INTENT_URL = "url";

    private VideoView VIEW;

    private MediaPlayer VIEW_MEDIA_PLAYER;

    private ImageView BTN_PLAY_PAUSE,
                      BTN_CROP;

    private TextView CURRENT_TIME_EL;

    private ProgressBar CURRENT_PROGRESS;

    private ConstraintLayout CONTROL_VIEW;

    private boolean SHOWING_CONTROLS,
                    HOLDING_COMPLETE,
                    CROPPING;

    private CountDownTimer CONTROL,
                           VIDEO_TIMESTAMP,
                           HOLDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewer_video);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initVariables();

        initListeners();

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            VIDEO_TIMESTAMP.cancel();
        } catch (Exception ignore) {}
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if(!VIEW.isPlaying()) {
            VIEW.start();
        }

        onNavigationClick(RotaryEncoderHelper.getRotaryAxisValue(event) >= 0);

        return true;
    }

    private void initVariables() {
        VIEW = findViewById(R.id.view_video);

        CONTROL_VIEW = findViewById(R.id.view_control);

        BTN_PLAY_PAUSE = findViewById(R.id.btn_play_pause);
        BTN_CROP = findViewById(R.id.btn_crop);

        SHOWING_CONTROLS = false;

        HOLDING_COMPLETE = false;

        CROPPING = true;

        CURRENT_TIME_EL = findViewById(R.id.current_time);

        CURRENT_PROGRESS = findViewById(R.id.view_progress);
    }

    private void initListeners() {
        VIEW.setOnCompletionListener((mediaPlayer) -> {
            BTN_PLAY_PAUSE.setImageResource(R.drawable.icon_viewer_video_play);

            showControls();
        });

        VIEW.setOnPreparedListener((mp) -> {
            ((TextView) findViewById(R.id.total_time)).setText(Utils.Formatter.timeInMillis(VIEW.getDuration()));

            CURRENT_PROGRESS.setMax(VIEW.getDuration());

            VIEW_MEDIA_PLAYER = mp;
        });

        CONTROL_VIEW.setOnClickListener((l) -> showControls());

        findViewById(R.id.video_container).setOnClickListener((l) -> showControls());

        BTN_PLAY_PAUSE.setOnClickListener((l) -> togglePlayPause());

        findViewById(R.id.btn_rewind).setOnClickListener((l) -> onNavigationClick(true));
        findViewById(R.id.btn_foward).setOnClickListener((l) -> onNavigationClick(false));

        findViewById(R.id.btn_rewind).setOnTouchListener((v, e) -> onNavigationHold(v, e, true));
        findViewById(R.id.btn_foward).setOnTouchListener((v, e) -> onNavigationHold(v, e, false));

        findViewById(R.id.btn_volume).setOnClickListener((l) -> new VolumePopup(this).show());

        BTN_CROP.setOnClickListener((v) -> toggleCropping());
    }

    private void init() {
        final File file = new File(Objects.requireNonNull(getIntent().getStringExtra(INTENT_URL)));

        if(!file.exists()) {
            Toast.makeText(this, getString(R.string.toast_error_file_not_found), Toast.LENGTH_SHORT).show();

            finish();

            return;
        }

        VIEW.setVideoPath(file.getAbsolutePath());

        VIEW.start();

        showCurrentTime();
    }

    private void showControls() {
        if(SHOWING_CONTROLS) {
            try {
                CONTROL.cancel();
            } catch (Exception ignore) {}
        } else {
            SHOWING_CONTROLS = true;

            CONTROL_VIEW.setVisibility(View.VISIBLE);
        }

        CONTROL = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                hideControls();
            }
        }.start();
    }

    private void hideControls() {
        try {
            CONTROL.cancel();
        } catch (Exception ignore) {}

        SHOWING_CONTROLS = false;

        CONTROL_VIEW.setVisibility(View.GONE);
    }

    private void togglePlayPause() {
        if(VIEW.isPlaying()) {
            VIEW.pause();
        } else {
            VIEW.start();
        }

        showPlayPauseUI();
    }

    private void showPlayPauseUI() {
        if(VIEW.isPlaying()) {
            BTN_PLAY_PAUSE.setImageResource(R.drawable.icon_viewer_video_pause);
        } else {
            BTN_PLAY_PAUSE.setImageResource(R.drawable.icon_viewer_video_play);
        }
    }

    private void showCurrentTime() {
        CURRENT_TIME_EL.setText(Utils.Formatter.timeInMillis(VIEW.getCurrentPosition()));

        CURRENT_PROGRESS.setProgress(VIEW.getCurrentPosition());

        VIDEO_TIMESTAMP = new CountDownTimer(500, 100) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                showCurrentTime();
            }
        }.start();
    }

    private void onNavigationClick(boolean rewind) {
        VIEW.pause();

        VIEW_MEDIA_PLAYER.seekTo(VIEW_MEDIA_PLAYER.getCurrentPosition() + (2000 * (rewind ? -1 : 1)));

        VIEW.start();

        showPlayPauseUI();
    }

    private boolean onNavigationHold(View view, MotionEvent motionEvent, boolean rewind) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                HOLDING = new CountDownTimer(300, 100) {
                    @Override
                    public void onTick(long l) {}

                    @Override
                    public void onFinish() {
                        HOLDING_COMPLETE = true;

                        VIEW.pause();

                        showPlayPauseUI();

                        fastAction(rewind ? -1 : 1);
                    }
                }.start();

                view.setPressed(true);

                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(!HOLDING_COMPLETE) {
                    try {
                        HOLDING.cancel();
                    } catch (Exception ignore) {}

                    view.callOnClick();
                } else {
                    HOLDING_COMPLETE = false;

                    VIEW.start();

                    showPlayPauseUI();
                }

                view.setPressed(false);

                return true;
        }

        return false;
    }

    private void fastAction(int factor) {
        showControls();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(HOLDING_COMPLETE) {
                VIEW_MEDIA_PLAYER.seekTo(VIEW_MEDIA_PLAYER.getCurrentPosition() + (4000 * factor));

                fastAction(factor);
            }
        }, 500);
    }

    private void toggleCropping() {
        if(CROPPING) {
            VIEW.getLayoutParams().width = 0;
            VIEW.getLayoutParams().height = 0;

            BTN_CROP.setImageResource(R.drawable.icon_viewer_video_fit_crop);
        } else {
            VIEW.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            VIEW.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

            BTN_CROP.setImageResource(R.drawable.icon_viewer_video_fit);
        }

        CROPPING = !CROPPING;

        VIEW.requestLayout();
    }
}
