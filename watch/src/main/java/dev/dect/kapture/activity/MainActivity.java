package dev.dect.kapture.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.CurvedTextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.wearable.input.RotaryEncoderHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.KaptureAdapter;
import dev.dect.kapture.communication.Sender;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.server.WifiShare;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KTimer;
import dev.dect.kapture.utils.Utils;

/** @noinspection ResultOfMethodCallIgnored*/
@SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "StaticFieldLeak", "NotifyDataSetChanged"})
public class MainActivity extends Activity {
    public static final String EXTRA_LANGUAGE_CHANGED = "lang";

    private static MainActivity ACTIVITY;

    private final int PERMISSION_REQUEST_CODE = 0;

    private boolean IS_VISIBLE = false;

    private AppCompatButton BTN_START_STOP,
                            BTN_PAUSE_RESUME,
                            BTN_SENDING;

    private TextView TIME_EL,
                     TIME_LIMIT_EL;

    private CurvedTextView CLOCK_EL;

    private BottomSheetBehavior<View> BOTTOM_SHEET_EL;

    private ConstraintLayout BOTTOM_SHEET_BACKGROUND_EL;

    private ImageView BOTTOM_SHEET_TOGGLE_EL;

    private KSettings KSETTINGS;

    private NestedScrollView NESTED_SCROLLER_EL;

    private RecyclerView RECYCLER_VIEW;

    private ArrayList<Kapture> KAPTURES;

    private KaptureAdapter ADAPTER;

    private DB DATABASE;

    private SelectionTracker<Long> TRACKER;

    private AppCompatButton BTN_SELECTED;

    private ConstraintLayout BTN_SELECTED_CONTAINER;

    private final BroadcastReceiver CLOCK_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        updateClock();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Default);

        setContentView(R.layout.activity_main);

        initVariables();

        initListeners();

        init();
    }

    @Override
    public void onResume() {
        IS_VISIBLE = true;

        ACTIVITY = this;

        final IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);

        registerReceiver(CLOCK_RECEIVER, filter);

        updateClock();

        KSETTINGS = new KSettings(this);

        updateTimeLimitInfo();

        super.onResume();
    }

    @Override
    public void onPause() {
        IS_VISIBLE = false;

        unregisterReceiver(CLOCK_RECEIVER);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ACTIVITY = null;
    }

    /** @noinspection deprecation*/
    @Override
    public void onBackPressed() {
        onBackButton();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_CODE && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.toast_error_permission), Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if(BOTTOM_SHEET_EL.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            final int scrollTo = (int) (NESTED_SCROLLER_EL.getY() + (RotaryEncoderHelper.getRotaryAxisValue(event) < 0 ? Constants.BEZEL_SCROLL_BY : (Constants.BEZEL_SCROLL_BY * -1)));

            NESTED_SCROLLER_EL.smoothScrollBy(0, scrollTo);
        }

        return true;
    }

    public static MainActivity getInstance() {
        return ACTIVITY;
    }

    private void initVariables() {
        KSETTINGS = new KSettings(this);

        CLOCK_EL = findViewById(R.id.clock);

        BTN_START_STOP = findViewById(R.id.startStopCapturing);
        BTN_PAUSE_RESUME = findViewById(R.id.pauseResumeCapturing);

        TIME_EL = findViewById(R.id.time);
        TIME_LIMIT_EL = findViewById(R.id.timeLimit);

        BOTTOM_SHEET_EL = BottomSheetBehavior.from(findViewById(R.id.bottomSheet));
        BOTTOM_SHEET_BACKGROUND_EL = findViewById(R.id.bottomSheetBackground);
        BOTTOM_SHEET_TOGGLE_EL = findViewById(R.id.bottomSheetToggle);

        BTN_SELECTED = findViewById(R.id.selectd);

        BTN_SELECTED_CONTAINER = findViewById(R.id.selectButtonContainer);

        BTN_SENDING = findViewById(R.id.btnSendPhone);

        NESTED_SCROLLER_EL = findViewById(R.id.nestedScrollView);

        RECYCLER_VIEW = findViewById(R.id.recyclerView);

        DATABASE = new DB(this);

        getAndValidateKaptures();
    }

    private void initListeners() {
        BTN_START_STOP.setOnClickListener((l) -> CapturingService.requestToggleRecording(this));

        BTN_PAUSE_RESUME.setOnClickListener((l) -> CapturingService.requestTogglePauseResumeRecording());

        BOTTOM_SHEET_EL.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                    BOTTOM_SHEET_TOGGLE_EL.setImageResource(R.drawable.icon_bottom_sheet_collapse);
                } else if(newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    BOTTOM_SHEET_TOGGLE_EL.setImageResource(R.drawable.icon_bottom_sheet_expand);

                    unselectAll();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                BOTTOM_SHEET_BACKGROUND_EL.setAlpha(1f - slideOffset);

                findViewById(R.id.title).setAlpha(slideOffset);

                findViewById(R.id.selectContainer).setAlpha(slideOffset);

                RECYCLER_VIEW.setAlpha(slideOffset);
            }
        });

        BOTTOM_SHEET_TOGGLE_EL.setOnClickListener((v) -> bottomSheetToggle());

        BTN_SELECTED.setOnClickListener((l) -> {
            if(TRACKER.getSelection().size() == ADAPTER.getItemCount()) {
                unselectAll();
            } else {
                selectAll();
            }
        });

        NESTED_SCROLLER_EL.setOnTouchListener((view, motionEvent) -> {
            if(TRACKER.hasSelection()) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    BTN_SELECTED_CONTAINER.setAlpha(0);

                    BTN_SELECTED_CONTAINER.setVisibility(View.VISIBLE);

                    final ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);

                    va.addUpdateListener((animation) -> BTN_SELECTED_CONTAINER.setAlpha((float) animation.getAnimatedValue()));

                    va.setDuration(500);

                    va.start();
                } else {
                    BTN_SELECTED_CONTAINER.setVisibility(View.GONE);

                    BTN_SELECTED_CONTAINER.setAlpha(0);

                    BTN_START_STOP.setAnimation(null);
                }
            } else {
                BTN_SELECTED_CONTAINER.setVisibility(View.GONE);
            }

            return false;
        });

        findViewById(R.id.btnMore).setOnClickListener((v) -> startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.btnDelete).setOnClickListener((v) -> deleteSelected());
        findViewById(R.id.btnWifiShare).setOnClickListener((v) -> wifiShareSelected());
        BTN_SENDING.setOnClickListener((v) -> sendToPhoneSelected());

        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::onBackButton);
    }

    private void init() {
        checkAndRequestPermissions();

        updateClock();

        updateTimeLimitInfo();

        requestUIUpdate();

        buildRecyclerView();

        if(getIntent().hasExtra(EXTRA_LANGUAGE_CHANGED)) {
            findViewById(R.id.btnMore).callOnClick();
        }
    }
    private void checkAndRequestPermissions() {
        ActivityCompat.requestPermissions(this, new String[] {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        }, PERMISSION_REQUEST_CODE);
    }

    private void updateClock() {
        CLOCK_EL.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
    }

    public void requestUIUpdate() {
        updateTimeLimitInfo();

        if(CapturingService.isProcessing()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            findViewById(R.id.btnContainer).setVisibility(View.GONE);
            findViewById(R.id.loader).setVisibility(View.VISIBLE);

            TIME_EL.setText(getString(R.string.processing));
            TIME_EL.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.default_font));

            return;
        } else if(CapturingService.isInCountdown()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            findViewById(R.id.btnContainer).setVisibility(View.GONE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            findViewById(R.id.btnContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.loader).setVisibility(View.GONE);

            TIME_EL.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.time_font));
        }

        if(CapturingService.isRecording()) {
            updateTime(KTimer.getTime());

            BTN_START_STOP.setText("");

            final int size = (int) getResources().getDimension(R.dimen.btn_circle_height);

            if(BTN_START_STOP.getWidth() > (size + 1)) {
                final ValueAnimator va = ValueAnimator.ofInt(BTN_START_STOP.getWidth(), size);

                va.addUpdateListener((animation) -> {
                    BTN_START_STOP.getLayoutParams().width = (int) animation.getAnimatedValue();
                    BTN_START_STOP.requestLayout();
                });

                va.setDuration(250);

                va.start();

                BTN_START_STOP.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(getApplicationContext(), R.drawable.icon_capture_stop), null, null, null);
            } else {
                BTN_START_STOP.getLayoutParams().width = size;
                BTN_START_STOP.requestLayout();

                BTN_START_STOP.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(this, R.drawable.icon_capture_stop), null, null, null);
            }

            BTN_START_STOP.setPadding((int) getResources().getDimension(R.dimen.btn_circle_padding_circle), 0,0,0);
            BTN_START_STOP.setCompoundDrawablePadding(0);
            BTN_START_STOP.setText("");

            BTN_PAUSE_RESUME.setVisibility(View.VISIBLE);
        } else {
            BTN_PAUSE_RESUME.setVisibility(View.GONE);

            BTN_START_STOP.setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.btn_circle_drawable_padding));

            BTN_START_STOP.setPadding((int) getResources().getDimension(R.dimen.btn_circle_padding), 0,(int) getResources().getDimension(R.dimen.btn_circle_text_padding),0);

            BTN_START_STOP.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(this, R.drawable.icon_capture_start), null, null, null);

            BTN_START_STOP.setText(getString(R.string.btn_start));

            BTN_START_STOP.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;

            BTN_START_STOP.requestLayout();

            TIME_EL.setText("00:00");
        }

        if(CapturingService.isPaused()) {
            BTN_PAUSE_RESUME.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(this, R.drawable.icon_capture_resume), null, null, null);
        } else {
            BTN_PAUSE_RESUME.setCompoundDrawablesWithIntrinsicBounds(Utils.getDrawable(this, R.drawable.icon_capture_pause), null, null, null);
        }
    }

    private void onBackButton() {
        if(BOTTOM_SHEET_EL.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            if(TRACKER.hasSelection()) {
                unselectAll();
            } else {
                NESTED_SCROLLER_EL.scrollTo(0, 0);

                bottomSheetToggle();
            }
        } else {
            finish();
        }
    }

    private void bottomSheetToggle() {
        BOTTOM_SHEET_EL.setState(
            BOTTOM_SHEET_EL.getState() == BottomSheetBehavior.STATE_EXPANDED
            ?  BottomSheetBehavior.STATE_COLLAPSED
            : BottomSheetBehavior.STATE_EXPANDED
        );
    }

    private void updateTimeLimitInfo() {
        if(CapturingService.isProcessing() || !KSETTINGS.isToUseTimeLimit()) {
            TIME_LIMIT_EL.setVisibility(View.GONE);
        } else {
            TIME_LIMIT_EL.setVisibility(View.VISIBLE);

            TIME_LIMIT_EL.setText(Utils.Formatter.timeInSeconds(KSETTINGS.getSecondsTimeLimit()));
        }
    }

    private void getAndValidateKaptures() {
        KAPTURES = new ArrayList<>();

        for(Kapture kapture : DATABASE.selectAllKaptures(true)) {
            if(new File(kapture.getLocation()).exists()) {
                KAPTURES.add(kapture);
            } else {
                if(kapture.getThumbnailCachedFile().exists()) {
                    kapture.getThumbnailCachedFile().delete();
                }

                DATABASE.deleteKapture(kapture);
            }
        }
    }

    private void buildRecyclerView() {
        ADAPTER = new KaptureAdapter(KAPTURES);

        RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(this));

        RECYCLER_VIEW.setNestedScrollingEnabled(false);

        RECYCLER_VIEW.setAdapter(ADAPTER);

        findViewById(R.id.bottomSheet).setVisibility(KAPTURES.isEmpty() ? View.GONE : View.VISIBLE);

        initTracker();
    }

    private void initTracker() {
        TRACKER = new SelectionTracker.Builder<>(
            "kaptures",
            RECYCLER_VIEW,
            new StableIdKeyProvider(RECYCLER_VIEW),
            new KaptureAdapter.ItemLookup(RECYCLER_VIEW),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build();

        ADAPTER.setTracker(TRACKER);

        TRACKER.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                final int size = TRACKER.getSelection().size();

                if(size > 0) {
                    findViewById(R.id.selectContainer).setVisibility(View.VISIBLE);

                    BTN_SELECTED_CONTAINER.setVisibility(View.VISIBLE);

                    BTN_SELECTED.setText(size + " " + (size == 1 ? getString(R.string.selected) : getString(R.string.selected_plural)));

                    BTN_SELECTED.setCompoundDrawablesWithIntrinsicBounds(
                        Utils.getDrawable(getApplicationContext(), size == ADAPTER.getItemCount() ? R.drawable.checkbox_on : R.drawable.checkbox_off),
                        null,
                        null,
                        null
                    );
                } else {
                    findViewById(R.id.selectContainer).setVisibility(View.GONE);

                    BTN_SELECTED_CONTAINER.setVisibility(View.GONE);
                }
            }
        });
    }

    private void selectAll() {
        for(int i = 0; i < ADAPTER.getItemCount(); i++) {
            TRACKER.select((long) i);
        }
    }

    private void unselectAll() {
        if(TRACKER.hasSelection()) {
            for (int i = 0; i < ADAPTER.getItemCount(); i++) {
                TRACKER.deselect((long) i);
            }
        }
    }

    private ArrayList<Kapture> getSelected() {
        final ArrayList<Kapture> kaptures = new ArrayList<>();

        for(long index : TRACKER.getSelection()) {
            kaptures.add(KAPTURES.get((int) index));
        }

        return kaptures;
    }

    private void deleteSelected() {
        if(!TRACKER.hasSelection()) {
            return;
        }

        new DialogPopup(
            this,
            R.string.popup_btn_delete,
            R.string.popup_delete_text_2,
            R.drawable.icon_delete,
            () -> {
                final ArrayList<Kapture> toRemove = getSelected();

                for(Kapture kapture : toRemove) {
                    File f = new File(kapture.getLocation());

                    if(f.exists()) {
                        f.delete();
                    }

                    if(kapture.getThumbnailCachedFile().exists()) {
                        kapture.getThumbnailCachedFile().delete();
                    }

                    DATABASE.deleteKapture(kapture);
                }

                unselectAll();

                KAPTURES.removeAll(toRemove);

                ADAPTER.notifyDataSetChanged();

                setEmptyAdapterIfEmpty();

                Toast.makeText(this, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
            },
            DialogPopup.RES_ID_DEFAULT,
            null,
            false,
            false,
            true
        ).show();
    }

    public void wifiShareSelected() {
        if(!TRACKER.hasSelection()) {
            return;
        }

        new WifiShare(this, getSelected()).start();
    }

    public void sendToPhoneSelected() {
        if(!TRACKER.hasSelection()) {
            return;
        }

        new DialogPopup(
            this,
            R.string.send_phone,
            R.string.send_phone_message,
            R.drawable.icon_send_to_phone,
            () -> {
                try {
                    new Sender(this).sendKapturesFiles(getSelected());

                    Toast.makeText(this, getString(R.string.toast_info_sending_phone), Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, getString(R.string.toast_info_sending_phone_2), Toast.LENGTH_LONG).show();

                    updateSendingButton(false);

                    unselectAll();
                } catch (Exception ignore) {
                    Toast.makeText(this, getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
                }
            },
            DialogPopup.RES_ID_DEFAULT,
            null,
            true,
            false,
            false
        ).show();
    }

    public void requestUiUpdate(Kapture kapture) {
        new Handler(Looper.getMainLooper()).post(() -> {
            requestUIUpdate();

            if(kapture != null) {
                KAPTURES.add(0, kapture);

                if(KAPTURES.size() == 1) {
                    setEmptyAdapterIfEmpty();

                    BOTTOM_SHEET_BACKGROUND_EL.setAlpha(0);
                }

                BOTTOM_SHEET_EL.setState(BottomSheetBehavior.STATE_EXPANDED);

                ADAPTER.notifyDataSetChanged();
            }
        });
    }

    private void setEmptyAdapterIfEmpty() {
        if(KAPTURES.isEmpty()) {
            RECYCLER_VIEW.removeAllViews();

            BOTTOM_SHEET_EL.setState(BottomSheetBehavior.STATE_COLLAPSED);

            findViewById(R.id.bottomSheet).setVisibility(View.GONE);
        } else if(KAPTURES.size() == 1) {
            findViewById(R.id.bottomSheet).setVisibility(View.VISIBLE);

            RECYCLER_VIEW.setAdapter(ADAPTER);
        }
    }

    public void updateTime(int seconds) {
        if(IS_VISIBLE) {
            runOnUiThread(() -> TIME_EL.setText(Utils.Formatter.timeInSeconds(seconds)));
        }
    }

    public void updateTimeCountdown(int seconds) {
        if(IS_VISIBLE) {
            runOnUiThread(() -> TIME_EL.setText(String.valueOf(seconds)));
        }
    }

    public void updateSendingButton(boolean enable) {
        BTN_SENDING.setBackgroundTintList(ColorStateList.valueOf(getColor(enable ? R.color.btn_main_background : R.color.btn_secondary_background)));
        BTN_SENDING.setEnabled(enable);
    }
}