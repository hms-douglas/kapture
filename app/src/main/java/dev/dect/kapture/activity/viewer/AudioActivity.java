package dev.dect.kapture.activity.viewer;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.slider.Slider;

import java.io.File;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.installer.InstallActivity;
import dev.dect.kapture.utils.Utils;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class AudioActivity extends AppCompatActivity {
    private final String TAG = AudioActivity.class.getSimpleName();

    public static final String INTENT_URL = "url";

    private TextView CURRENT_TIME;

    private Slider TIME_BAR;

    private AppCompatButton BTN_TOGGLE;

    private MediaPlayer VIEW_MEDIA_PLAYER;

    private Handler TIME_HANDLER;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        setContentView(R.layout.activity_viewer_audio);

        initVariables();

        initListeners();

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            VIEW_MEDIA_PLAYER.release();
            VIEW_MEDIA_PLAYER.stop();
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e.getMessage());
        }

        TIME_HANDLER.removeCallbacksAndMessages(null);
    }

    private void initVariables() {
        CURRENT_TIME = findViewById(R.id.currentTime);

        TIME_BAR = findViewById(R.id.timeBar);

        BTN_TOGGLE = findViewById(R.id.btnToggle);

        TIME_HANDLER = new Handler(Looper.getMainLooper());

        VIEW_MEDIA_PLAYER = new MediaPlayer();
    }

    private void initListeners() {
        findViewById(R.id.window).setOnClickListener((v) -> finish());
        findViewById(R.id.btnClose).setOnClickListener((v) -> finish());

        BTN_TOGGLE.setOnClickListener((l) -> toggle());

        VIEW_MEDIA_PLAYER.setOnCompletionListener((mediaPlayer) -> pause());

        VIEW_MEDIA_PLAYER.setOnPreparedListener((mp) -> {
            ((TextView) findViewById(R.id.maxTime)).setText(Utils.Formatter.timeInMillis(mp.getDuration()));

            TIME_BAR.setValueFrom(0);
            TIME_BAR.setValueTo(mp.getDuration());
            TIME_BAR.setStepSize(1);

            TIME_BAR.setValue(0);

            updateTime();

            play();
        });

        TIME_BAR.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                pause();
                return true;
            } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                play();
                return true;
            }

            return false;
        });

        TIME_BAR.addOnChangeListener((slider, value, fromUser) -> {
            if(fromUser) {
                try {
                    if(VIEW_MEDIA_PLAYER != null && !VIEW_MEDIA_PLAYER.isPlaying()) {
                        VIEW_MEDIA_PLAYER.seekTo((int) value);

                        updateTimeUI();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "initListeners: " + e.getMessage());
                }
            }
        });
    }

    private void init() {
        final File f = new File(Objects.requireNonNull(getIntent().getStringExtra(INTENT_URL)));

        if(!f.exists()) {
            Toast.makeText(this, getString(R.string.toast_error_file_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.fileName)).setText(f.getName());

        final ActivityManager.TaskDescription.Builder taskDescription = new ActivityManager.TaskDescription.Builder();

        taskDescription.setLabel(f.getName());

        setTaskDescription(taskDescription.build());

        try {
            VIEW_MEDIA_PLAYER.setDataSource(f.getAbsolutePath());
            VIEW_MEDIA_PLAYER.prepare();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            Log.e(TAG, "init: " + e.getMessage());

            finish();
        }
    }

    private void toggle() {
        if(VIEW_MEDIA_PLAYER.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void pause() {
        VIEW_MEDIA_PLAYER.pause();

        BTN_TOGGLE.setBackgroundResource(R.drawable.icon_viewer_audio_play);
    }

    private void play() {
        VIEW_MEDIA_PLAYER.start();

        BTN_TOGGLE.setBackgroundResource(R.drawable.icon_viewer_audio_pause);
    }

    private void updateTime() {
        updateTimeUI();

        TIME_HANDLER.postDelayed(this::updateTime, 500);
    }

    private void updateTimeUI() {
        CURRENT_TIME.setText(Utils.Formatter.timeInMillis(VIEW_MEDIA_PLAYER.getCurrentPosition()));

        TIME_BAR.setValue(VIEW_MEDIA_PLAYER.getCurrentPosition());
    }
}
