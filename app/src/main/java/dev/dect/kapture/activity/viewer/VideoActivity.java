package dev.dect.kapture.activity.viewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.slider.Slider;

import java.io.File;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class VideoActivity extends AppCompatActivity {
    private final String TAG = VideoActivity.class.getSimpleName();

    public static final String INTENT_URL = "url";

    private VideoView VIEW;

    private TextView TIME;

    private Slider TIME_BAR;

    private AppCompatButton BTN_TOGGLE;

    private ConstraintLayout OPTIONS,
                             WINDOW;

    private int DURATION,
                POSITION_START;

    private String TOTAL_TIME;

    private MediaPlayer VIEW_MEDIA_PLAYER;

    private Handler TIME_HANDLER;

    private File FILE;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_viewer_video);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        initVariables();

        initListeners();

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TIME_HANDLER.removeCallbacksAndMessages(null);
    }

    private void initVariables() {
        VIEW = findViewById(R.id.viewVideo);

        TIME = findViewById(R.id.time);

        TIME_BAR = findViewById(R.id.timeBar);

        BTN_TOGGLE = findViewById(R.id.btnToggle);

        OPTIONS = findViewById(R.id.options);

        WINDOW = findViewById(R.id.window);

        TIME_HANDLER = new Handler(Looper.getMainLooper());

        POSITION_START = 0;
    }

    private void initListeners() {
        WINDOW.setOnClickListener((l) -> {
            OPTIONS.setVisibility(View.VISIBLE);

            OPTIONS.animate().alpha(1f).setDuration(250).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                OPTIONS.clearAnimation();
                }
            });
        });

        OPTIONS.setOnClickListener((l) -> OPTIONS.animate().alpha(0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            OPTIONS.clearAnimation();

            OPTIONS.setVisibility(View.GONE);
            }
        }));

        VIEW.setOnCompletionListener((mediaPlayer) -> {
            pause();
            WINDOW.callOnClick();
        });

        VIEW.setOnPreparedListener((mp) -> {
            VIEW_MEDIA_PLAYER = mp;

            VIEW_MEDIA_PLAYER.seekTo(POSITION_START);

            DURATION = mp.getDuration();

            TOTAL_TIME = Utils.Formatter.timeInMillis(DURATION);

            TIME_BAR.setValueFrom(0);
            TIME_BAR.setValueTo(DURATION);
            TIME_BAR.setStepSize(1);

            TIME_BAR.setValue(0);

            updateTime();
        });

        BTN_TOGGLE.setOnClickListener((l) -> toggle());

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
                } catch (Exception e){
                    Log.e(TAG, "initListeners: " + e.getMessage());
                }
            }
        });

        findViewById(R.id.btnRewind).setOnClickListener((v) -> onNavigationClick(true));
        findViewById(R.id.btnForward).setOnClickListener((v) -> onNavigationClick(false));

        findViewById(R.id.btnMore).setOnClickListener((v) -> {
            final PopupMenu popupMenu = new PopupMenu(this, v, Gravity.END, 0, R.style.KPopupMenu);

            final Menu menu = popupMenu.getMenu();

            popupMenu.getMenuInflater().inflate(R.menu.view_video_more, menu);

            menu.setGroupDividerEnabled(true);

            popupMenu.setOnMenuItemClickListener((item) -> {
                final int id = item.getItemId();

                if(id == R.id.menuOpenWith) {
                    KFile.openFile(this, FILE, true);
                } else if(id == R.id.menuShare) {
                    KFile.shareFile(this, FILE);
                }

                return true;
            });

            popupMenu.show();
        });

        findViewById(R.id.btnBack).setOnClickListener((v) -> finish());
    }

    private void init() {
        FILE = new File(Objects.requireNonNull(getIntent().getStringExtra(INTENT_URL)));

        if(!FILE.exists()) {
            Toast.makeText(this, getString(R.string.toast_error_file_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final ActivityManager.TaskDescription.Builder taskDescription = new ActivityManager.TaskDescription.Builder();

        taskDescription.setLabel(FILE.getName());

        setTaskDescription(taskDescription.build());

        VIEW.setVideoPath(FILE.getAbsolutePath());

        ((TextView) findViewById(R.id.title)).setText(FILE.getName());

        play();
    }

    private void toggle() {
        if(VIEW.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void pause() {
        if(VIEW.isPlaying()) {
            VIEW.pause();

            BTN_TOGGLE.setBackgroundResource(R.drawable.icon_viewer_video_play);
        }
    }

    private void play() {
        VIEW.start();

        BTN_TOGGLE.setBackgroundResource(R.drawable.icon_viewer_video_pause);
    }

    private void onNavigationClick(boolean rewind) {
        try {
            if(VIEW_MEDIA_PLAYER.isPlaying()) {
                pause();

                VIEW_MEDIA_PLAYER.seekTo(VIEW_MEDIA_PLAYER.getCurrentPosition() + (10000 * (rewind ? -1 : 1)));

                play();
            } else {
                VIEW_MEDIA_PLAYER.seekTo(VIEW_MEDIA_PLAYER.getCurrentPosition() + (10000 * (rewind ? -1 : 1)));
            }

        } catch (Exception e) {
            Log.e(TAG, "onNavigationClick: " + e.getMessage());
        }
    }

    private void updateTime() {
        updateTimeUI();

        TIME_HANDLER.postDelayed(this::updateTime, 500);
    }

    private void updateTimeUI() {
        TIME.setText(Utils.Formatter.timeInMillis(VIEW.getCurrentPosition()) + " / " + TOTAL_TIME);

        TIME_BAR.setValue(VIEW.getCurrentPosition());
    }
}
