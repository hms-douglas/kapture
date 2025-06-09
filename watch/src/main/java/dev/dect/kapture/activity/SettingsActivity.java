package dev.dect.kapture.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.CurvedTextView;

import com.google.android.gms.wearable.Node;
import com.google.android.wearable.input.RotaryEncoderHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.ListButton;
import dev.dect.kapture.adapter.ListButtonSubText;
import dev.dect.kapture.adapter.ListButtonSubTextSwitch;
import dev.dect.kapture.adapter.ListGroup;
import dev.dect.kapture.adapter.ListGroupDivisor;
import dev.dect.kapture.adapter.ListPicker;
import dev.dect.kapture.adapter.ListStorage;
import dev.dect.kapture.adapter.ListSwitch;
import dev.dect.kapture.adapter.SimpleTextAdapter;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.InputPopup;
import dev.dect.kapture.popup.PickerAppPopup;
import dev.dect.kapture.popup.TimePopup;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;

@SuppressLint({"ApplySharedPref", "NotifyDataSetChanged"})
public class SettingsActivity extends Activity {
    private CurvedTextView CLOCK_EL;

    private RecyclerView RECYCLER_VIEW;

    private SharedPreferences SP_APP,
                              SP_PROFILE;

    private ListStorage.Adapter STORAGE_ADAPTER;

    private NestedScrollView NESTED_SCROLLER_EL;

    private final Node[] PHONE_NODE = new Node[1];

    private final BroadcastReceiver CLOCK_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateClock();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        initVariables();

        init();
    }

    @Override
    public void onResume() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);

        registerReceiver(CLOCK_RECEIVER, filter);

        updateClock();

        super.onResume();
    }

    @Override
    public void onPause() {
        unregisterReceiver(CLOCK_RECEIVER);

        super.onPause();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        final int scrollTo = (int) (NESTED_SCROLLER_EL.getY() + (RotaryEncoderHelper.getRotaryAxisValue(event) < 0 ? Constants.BEZEL_SCROLL_BY : (Constants.BEZEL_SCROLL_BY * -1)));

        NESTED_SCROLLER_EL.smoothScrollBy(0, scrollTo);

        return true;
    }

    private void initVariables() {
        CLOCK_EL = findViewById(R.id.clock);

        SP_APP = KSharedPreferences.getAppSp(this);

        SP_PROFILE = KSharedPreferences.getActiveProfileSp(this);

        NESTED_SCROLLER_EL = findViewById(R.id.nestedScrollView);

        RECYCLER_VIEW = findViewById(R.id.recyclerView);

        Utils.Remote.getPhoneNodeAsync(this, PHONE_NODE);
    }

    private void init() {
        updateClock();

        buildRecyclerView();
    }

    private void buildRecyclerView() {
        final KSettings settings = new KSettings(this);

        final ConcatAdapter concatAdapter = new ConcatAdapter();

        concatAdapter.addAdapter(buildAndGetFolderGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetVideoGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetMicrophoneGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetInternalAudioGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetCapturingGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetWifiShareGroupAdapter());
        concatAdapter.addAdapter(buildAndGetAppGroupAdapter());
        concatAdapter.addAdapter(buildAndGetMoreGroupAdapter());

        RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(this));

        RECYCLER_VIEW.setNestedScrollingEnabled(false);

        RECYCLER_VIEW.setAdapter(concatAdapter);
    }

    private ListGroup.Adapter buildAndGetFolderGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListButtonSubText> listButtonSubTexts0 = new ArrayList<>();

        listButtonSubTexts0.add(
            new ListButtonSubText(
                R.string.setting_folder_location,
                settings.getSavingLocationPath(true),
                (listButtonSubText) -> {
                    if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                        Toast.makeText(this, getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    Toast.makeText(this, getString(R.string.toast_info_change_folder_location), Toast.LENGTH_SHORT).show();
                },
                true
            )
        );

        concatAdapter.addAdapter(new ListButtonSubText.Adapter(listButtonSubTexts0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_folder, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetVideoGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_resolution, settings.getVideoResolution(), KSettings.VIDEO_RESOLUTIONS, KSettings.getVideoResolutionsFormatted(this), Constants.Sp.Profile.VIDEO_RESOLUTION, false));

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_quality, settings.getVideoBitRate(), KSettings.VIDEO_QUALITIES, KSettings.getVideoQualitiesFormatted(), Constants.Sp.Profile.VIDEO_QUALITY_bitRate, false));

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_fps, settings.getVideoFrameRate(), KSettings.VIDEO_FRAME_RATES, null, Constants.Sp.Profile.VIDEO_FRAME_RATE, true));

        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0, false));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_video, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetMicrophoneGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_mic_capture, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_RECORD_MIC, settings.isToRecordMic(), true));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_mic, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetInternalAudioGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_internal_capture, R.string.setting_internal_capture_description, Constants.Sp.Profile.IS_TO_RECORD_INTERNAL_AUDIO, settings.isToRecordInternalAudio(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_internal_stereo, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_RECORD_SOUND_IN_STEREO, settings.isToRecordInternalAudioInStereo(), false));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));

        concatAdapter.addAdapter(new SimpleTextAdapter(getString(R.string.setting_internal_description), Gravity.CENTER, true));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_internal, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetCapturingGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        buildAndGetCapturingGroupAdapter_beforeStartOption(settings, concatAdapter);

        buildAndGetCapturingGroupAdapter_countdown(settings, concatAdapter);

        buildAndGetCapturingGroupAdapter_stopOption(settings, concatAdapter);

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_capturing, concatAdapter));
    }

    private void buildAndGetCapturingGroupAdapter_beforeStartOption(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListButtonSubTextSwitch> listButtonSubTexts0 = new ArrayList<>();

        listButtonSubTexts0.add(
            new ListButtonSubTextSwitch(
                R.string.setting_start_volume,
                settings.getBeforeStartMediaVolumePercentage() + "%",
                (listButtonSubText) -> {
                    new InputPopup.NumberInteger(
                        this,
                        R.string.popup_media_volume,
                        Integer.parseInt(listButtonSubText.getValue().replaceAll("%", "")),
                        0,
                        100,
                        InputPopup.RES_ID_DEFAULT,
                        new InputPopup.OnInputPopupListener() {
                            @Override
                            public void onIntInputSet(int input) {
                                SP_PROFILE.edit().putInt(Constants.Sp.Profile.BEFORE_START_MEDIA_VOLUME_PERCENTAGE, input).commit();

                                listButtonSubText.setValue(input + "%");

                                concatAdapter.notifyDataSetChanged();
                            }
                        },
                        InputPopup.RES_ID_DEFAULT,
                        null,
                        true,
                        false,
                        false
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_BEFORE_START_SET_MEDIA_VOLUME,
                settings.isToSetMediaVolumeBeforeStart(),
                false
            )
        );

        listButtonSubTexts0.add(
            new ListButtonSubTextSwitch(
                R.string.setting_start_launch,
                settings.getBeforeStartLaunchAppName(),
                (listButtonSubText) -> {
                    Toast.makeText(this, getString(R.string.toast_info_loading), Toast.LENGTH_SHORT).show();

                    new PickerAppPopup(
                        this,
                        R.string.setting_start_launch_select,
                        PickerAppPopup.RES_ID_DEFAULT,
                        1,
                        true,
                        new ArrayList<>(Arrays.asList(SP_PROFILE.getString(Constants.Sp.Profile.BEFORE_START_LAUNCH_APP_PACKAGE, DefaultSettings.BEFORE_START_LAUNCH_APP_PACKAGE))),
                        (packages) -> {
                            final String packageName = packages.get(0);

                            SP_PROFILE.edit().putString(Constants.Sp.Profile.BEFORE_START_LAUNCH_APP_PACKAGE, packageName).commit();

                            listButtonSubText.setValue(packageName.equals(Constants.HOME_PACKAGE_NAME) ? getString(R.string.package_launch_home) : Utils.Package.getAppName(this, packageName));

                            concatAdapter.notifyDataSetChanged();
                        }
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_BEFORE_START_LAUNCH_APP,
                settings.isToSetLaunchAppBeforeStart(),
                false
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_capturing_start)));
        concatAdapter.addAdapter(new ListButtonSubTextSwitch.Adapter(listButtonSubTexts0, false));
        concatAdapter.addAdapter(new SimpleTextAdapter(getString(R.string.setting_start_message), Gravity.CENTER, true));
    }

    private void buildAndGetCapturingGroupAdapter_countdown(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListButtonSubTextSwitch> listButtonSubTexts0 = new ArrayList<>();

        listButtonSubTexts0.add(
            new ListButtonSubTextSwitch(
                R.string.setting_countdown_countdown,
                settings.getSecondsToStartRecording(),
                (listButtonSubText) -> {
                    new TimePopup(
                        this,
                        R.string.setting_countdown_countdown,
                        TimePopup.TYPE_SECOND,
                        Integer.parseInt(listButtonSubText.getValue()),
                        TimePopup.RES_ID_DEFAULT,
                        new TimePopup.OnTimePopupListener() {
                            @Override
                            public void onTimeSet(int input) {
                                SP_PROFILE.edit().putInt(Constants.Sp.Profile.SECONDS_TO_START_RECORDING, input).commit();

                                listButtonSubText.setValue(input);

                                concatAdapter.notifyDataSetChanged();
                            }
                        },
                        TimePopup.RES_ID_DEFAULT,
                        null,
                        true,
                        false
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_DELAY_START_RECORDING,
                settings.isToDelayStartRecordingEnabled(),
                false
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_capturing_countdown)));
        concatAdapter.addAdapter(new ListButtonSubTextSwitch.Adapter(listButtonSubTexts0, false));
        concatAdapter.addAdapter(new SimpleTextAdapter(getString(R.string.setting_countdown_countdown_description), Gravity.CENTER, true));
    }

    private void buildAndGetCapturingGroupAdapter_stopOption(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListButtonSubTextSwitch> listButtonSubTexts1 = new ArrayList<>();

        listButtonSubTexts1.add(
            new ListButtonSubTextSwitch(
                R.string.setting_stop_time_limit,
                Utils.Formatter.timeInSeconds(settings.getSecondsTimeLimit()),
                (listButtonSubText) -> {
                    new TimePopup(
                        this,
                        R.string.setting_stop_time_limit,
                        TimePopup.TYPE_MINUTE_SECOND,
                        Utils.Converter.timeToSeconds(listButtonSubText.getValue()),
                        TimePopup.RES_ID_DEFAULT,
                        new TimePopup.OnTimePopupListener() {
                            @Override
                            public void onTimeSet(int input) {
                                SP_PROFILE.edit().putInt(Constants.Sp.Profile.SECONDS_TIME_LIMIT, input).commit();

                                listButtonSubText.setValue(Utils.Formatter.timeInSeconds(input));

                                concatAdapter.notifyDataSetChanged();
                            }
                        },
                        TimePopup.RES_ID_DEFAULT,
                        null,
                        true,
                        false
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_USE_TIME_LIMIT,
                settings.isToUseTimeLimitEnabled(),
                false
            )
        );

        listButtonSubTexts1.add(
            new ListButtonSubTextSwitch(
                R.string.setting_stop_battery_level,
                settings.getStopOnBatteryLevelLevel() + "%",
                (listButtonSubText) -> {
                    new InputPopup.NumberInteger(
                        this,
                        R.string.setting_stop_battery_level,
                        Integer.parseInt(listButtonSubText.getValue().replaceAll("%", "")),
                        1,
                        99,
                        InputPopup.RES_ID_DEFAULT,
                        new InputPopup.OnInputPopupListener() {
                            @Override
                            public void onIntInputSet(int input) {
                                SP_PROFILE.edit().putInt(Constants.Sp.Profile.STOP_ON_BATTERY_LEVEL_LEVEL, input).commit();

                                listButtonSubText.setValue(input + "%");

                                concatAdapter.notifyDataSetChanged();
                            }
                        },
                        InputPopup.RES_ID_DEFAULT,
                        null,
                        true,
                        false,
                        false
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_STOP_ON_BATTERY_LEVEL,
                settings.isToStopOnBatteryLevel(),
                true
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_capturing_stop)));
        concatAdapter.addAdapter(new ListButtonSubTextSwitch.Adapter(listButtonSubTexts1, false));
    }

    private ListGroup.Adapter buildAndGetWifiShareGroupAdapter() {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(
            new ListSwitch(
                R.string.setting_group_wifi_share_request_password,
                ListSwitch.NO_TEXT,
                Constants.Sp.App.WIFI_SHARE_IS_TO_SHOW_PASSWORD,
                SP_APP.getBoolean(Constants.Sp.App.WIFI_SHARE_IS_TO_SHOW_PASSWORD, DefaultSettings.WIFI_SHARE_IS_TO_SHOW_PASSWORD),
                null,
                false
            )
        );

        listSwitches0.add(
            new ListSwitch(
                R.string.setting_group_wifi_share_refresh_password,
                R.string.setting_group_wifi_share_refresh_password_description,
                Constants.Sp.App.WIFI_SHARE_IS_TO_REFRESH_PASSWORD,
                SP_APP.getBoolean(Constants.Sp.App.WIFI_SHARE_IS_TO_REFRESH_PASSWORD, DefaultSettings.WIFI_SHARE_IS_TO_REFRESH_PASSWORD),
                null,
                true
            )
        );

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, true));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_wifi_share, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetAppGroupAdapter() {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListButtonSubText> listButtonSubText0 = new ArrayList<>();

        try {
            final String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

            listButtonSubText0.add(
                new ListButtonSubText(
                    R.string.about_version,
                    version,
                    (listButtonSubText) -> startActivity(new Intent(this, UpdateActivity.class)),
                    false
                )
            );
        } catch (Exception ignore) {}

        concatAdapter.addAdapter(new ListButtonSubText.Adapter(listButtonSubText0));

        final ArrayList<ListButton> listButton0 = new ArrayList<>();

        listButton0.add(
            new ListButton(
                R.string.menu_about,
                ListButton.NO_TEXT,
                () -> Utils.ExternalActivity.requestSettings(this),
                false
            )
        );

        listButton0.add(
            new ListButton(
                R.string.menu_settings_reset_settings,
                ListButton.NO_TEXT,
                () -> {
                    new DialogPopup(
                        this,
                        R.string.menu_settings_reset_settings,
                        R.string.menu_settings_reset_settings_description,
                        DialogPopup.RES_ID_DEFAULT,
                        this::resetSettings,
                        DialogPopup.RES_ID_DEFAULT,
                        null,
                        true,
                        false,
                        true
                    ).show();
                },
                true
            )
        );

        concatAdapter.addAdapter(new ListButton.Adapter(listButton0));

        buildAndGetAppGroupAdapter_storage(concatAdapter);

        buildAndGetAppGroupAdapter_performance(concatAdapter);

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_app, concatAdapter));
    }

    private void buildAndGetAppGroupAdapter_storage(ConcatAdapter concatAdapter) {
        STORAGE_ADAPTER = new ListStorage.Adapter(this, true);

        final ArrayList<ListButton> listButton0 = new ArrayList<>();

        listButton0.add(
            new ListButton(
                R.string.storage_btn_clear_cache,
                ListButton.NO_TEXT,
                () -> {
                    new DialogPopup(
                        this,
                        R.string.storage_btn_clear_cache,
                        R.string.storage_btn_clear_cache_description,
                        R.drawable.icon_delete,
                        () -> {
                            KFile.clearCache(this);

                            updateStorageInfo();

                            Toast.makeText(this, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
                        },
                        DialogPopup.RES_ID_DEFAULT,
                        null,
                        true,
                        false,
                        true
                    ).show();
                },
                true
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_app_storage)));
        concatAdapter.addAdapter(STORAGE_ADAPTER);
        concatAdapter.addAdapter(new SimpleTextAdapter(getString(R.string.storage_message), Gravity.CENTER, false));
        concatAdapter.addAdapter(new ListButton.Adapter(listButton0));
    }

    @SuppressLint("BatteryLife")
    private void buildAndGetAppGroupAdapter_performance(ConcatAdapter concatAdapter) {
        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_performance_battery_optimization)));
        concatAdapter.addAdapter(new SimpleTextAdapter(getString(R.string.setting_performance_battery_optimization_description), Gravity.CENTER, true));
    }

    private ListGroup.Adapter buildAndGetMoreGroupAdapter() {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListButton> listButton0 = new ArrayList<>();

        listButton0.add(
            new ListButton(
                R.string.menu_settings_open_accessibility,
                ListButton.NO_TEXT,
                    () -> Utils.ExternalActivity.requestAccessibility(this),
                false
            )
        );

        listButton0.add(
            new ListButton(
                R.string.about_btn_github,
                ListButton.NO_TEXT,
                () -> Utils.Remote.openLinkOnPhone(this, PHONE_NODE, Constants.Url.App.REPOSITORY),
                false
            )
        );

        listButton0.add(
            new ListButton(
                R.string.title_credits,
                ListButton.NO_TEXT,
                () -> startActivity(new Intent(this, CreditsActivity.class)),
                true
            )
        );

        concatAdapter.addAdapter(new ListButton.Adapter(listButton0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_more, concatAdapter));
    }

    private void updateClock() {
        CLOCK_EL.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
    }

    private void updateStorageInfo() {
        try {
            STORAGE_ADAPTER.updateValues(this);
            STORAGE_ADAPTER.notifyItemChanged(0);
        } catch (Exception ignore) {}
    }

    private void resetSettings() {
        KSharedPreferences.resetAppSettings(this);

        KSharedPreferences.resetActiveProfileSp(this);

        RECYCLER_VIEW.removeAllViews();

        buildRecyclerView();
    }
}