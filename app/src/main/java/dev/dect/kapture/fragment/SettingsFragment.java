package dev.dect.kapture.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.AboutActivity;
import dev.dect.kapture.activity.FilePickerActivity;
import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.activity.TokenActivity;
import dev.dect.kapture.activity.installer.InstallActivity;
import dev.dect.kapture.adapter.ListButton;
import dev.dect.kapture.adapter.ListButtonColor;
import dev.dect.kapture.adapter.ListButtonSubText;
import dev.dect.kapture.adapter.ListButtonSubTextSwitch;
import dev.dect.kapture.adapter.ListCameraSize;
import dev.dect.kapture.adapter.ListGroup;
import dev.dect.kapture.adapter.ListGroupDivisor;
import dev.dect.kapture.adapter.ListImageSize;
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
import dev.dect.kapture.popup.PickerPopup;
import dev.dect.kapture.popup.TimePopup;
import dev.dect.kapture.popup.VolumePopup;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.Utils;

@SuppressLint("ApplySharedPref")
public class SettingsFragment extends Fragment {
    private Context CONTEXT;

    private View VIEW;

    private RecyclerView RECYCLER_VIEW;

    private ActivityResultLauncher<Intent> LAUNCH_ACTIVITY_RESULT_FOR_FOLDER_PICKER,
                                           LAUNCH_ACTIVITY_RESULT_FOR_SCREENSHOT_FOLDER_PICKER,
                                           LAUNCH_ACTIVITY_RESULT_FOR_IMAGE_PICKER;

    private ListButtonSubText BUTTON_FOLDER,
                              BUTTON_SCREENSHOT_FOLDER,
                              BUTTON_IMAGE;

    private ListButtonSubText.Adapter BUTTON_FOLDER_ADAPTER,
                                      BUTTON_IMAGE_ADAPTER;

    private ListImageSize.Adapter LIST_IMAGE_SIZE_ADAPTER;

    private ListStorage.Adapter STORAGE_ADAPTER;

    private SharedPreferences SP_APP;

    private AppCompatButton BTN_PROFILE;

    private SharedPreferences SP_PROFILE;

    private SharedPreferences.OnSharedPreferenceChangeListener PROFILE_LISTENER;

    private boolean IS_TABLET_UI = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        VIEW = inflater.inflate(R.layout.fragment_settings, container, false);

        initVariables();

        initListeners();

        init();

        return VIEW;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateStorageInfo();

        updateImageInfo();

        KProfile.updateButtonUi(CONTEXT, BTN_PROFILE);
    }

    @Override
    public void onDestroy() {
        try {
            SP_APP.unregisterOnSharedPreferenceChangeListener(PROFILE_LISTENER);
        } catch (Exception ignore) {}

        super.onDestroy();
    }

    private void initVariables() {
        CONTEXT = VIEW.getContext();

        SP_APP = KSharedPreferences.getAppSp(CONTEXT);
        SP_PROFILE = KSharedPreferences.getActiveProfileSp(CONTEXT);

        RECYCLER_VIEW = VIEW.findViewById(R.id.recyclerView);

        LAUNCH_ACTIVITY_RESULT_FOR_FOLDER_PICKER = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    final String path = result.getData().getStringExtra(FilePickerActivity.INTENT_PATH);

                    BUTTON_FOLDER.setValue(KFile.formatAndroidPathToUser(CONTEXT, path));
                    BUTTON_FOLDER_ADAPTER.notifyItemChanged(0);

                    SP_APP.edit().putString(Constants.Sp.App.FILE_SAVING_PATH, path).commit();
                }
            }
        );

        LAUNCH_ACTIVITY_RESULT_FOR_SCREENSHOT_FOLDER_PICKER = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    final String path = result.getData().getStringExtra(FilePickerActivity.INTENT_PATH);

                    BUTTON_SCREENSHOT_FOLDER.setValue(KFile.formatAndroidPathToUser(CONTEXT, path));
                    BUTTON_FOLDER_ADAPTER.notifyItemChanged(1);

                    SP_APP.edit().putString(Constants.Sp.App.SCREENSHOT_FILE_SAVING_PATH, path).commit();
                }
            }
        );

        LAUNCH_ACTIVITY_RESULT_FOR_IMAGE_PICKER = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    final String path = result.getData().getStringExtra(FilePickerActivity.INTENT_PATH);

                    BUTTON_IMAGE.setValue(KFile.formatAndroidPathToUser(CONTEXT, path));
                    BUTTON_IMAGE_ADAPTER.notifyItemChanged(0);

                    LIST_IMAGE_SIZE_ADAPTER.notifyItemChanged(0);

                    SP_PROFILE.edit().putString(Constants.Sp.Profile.IMAGE_PATH, path).commit();
                }
            }
        );

        BTN_PROFILE = VIEW.findViewById(R.id.btnProfile);

        PROFILE_LISTENER = KProfile.createAndAddProfileListenerUpdate(SP_APP, BTN_PROFILE, () -> {
            SP_PROFILE = KSharedPreferences.getActiveProfileSp(CONTEXT);

            rebuildRecyclerView();
        });

        IS_TABLET_UI = getResources().getBoolean(R.bool.is_tablet);
    }

    private void initListeners() {
        VIEW.findViewById(R.id.btnMore).setOnClickListener((v) -> {
            final PopupMenu popupMenu = new PopupMenu(CONTEXT, v, Gravity.END, 0, R.style.KPopupMenu);

            final Menu menu = popupMenu.getMenu();

            menu.setGroupDividerEnabled(true);

            popupMenu.getMenuInflater().inflate(R.menu.settings_more, menu);

            if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                menu.findItem(R.id.menuAppResetSettings).setEnabled(false);
                menu.findItem(R.id.menuProfileResetSettings).setEnabled(false);
            }

            popupMenu.setOnMenuItemClickListener((item) -> {
                final int idClicked = item.getItemId();

                if(idClicked == R.id.menuAppResetSettings) {
                    new DialogPopup(
                        CONTEXT,
                        R.string.menu_settings_reset_settings_confirm,
                        R.string.menu_settings_reset_app_settings_confirm_description,
                        R.string.popup_btn_reset,
                        this::resetAppSettings,
                        R.string.popup_btn_cancel,
                        null,
                        true,
                        false,
                        false
                    ).show();
                } else if(idClicked == R.id.menuProfileResetSettings) {
                    new DialogPopup(
                        CONTEXT,
                        R.string.menu_settings_reset_settings_confirm,
                        R.string.menu_settings_reset_profile_settings_confirm_description,
                        R.string.popup_btn_reset,
                        this::resetProfileSettings,
                        R.string.popup_btn_cancel,
                        null,
                        true,
                        false,
                        false
                    ).show();
                } else if(idClicked == R.id.menuOpenAccessibility) {
                    Utils.ExternalActivity.requestAccessibility(CONTEXT);
                } else if(idClicked == R.id.menuInstallWatch) {
                    startActivity(new Intent(CONTEXT, InstallActivity.class));
                } else if(idClicked == R.id.menuShowCommand) {
                    new DialogPopup(
                        CONTEXT,
                        DialogPopup.NO_TEXT,
                        R.string.command,
                        R.string.popup_btn_ok,
                        null,
                        DialogPopup.NO_TEXT,
                        null,
                        true,
                        false,
                        false
                    ).show();
                } else if(idClicked == R.id.menuAbout) {
                    startActivity(new Intent(CONTEXT, AboutActivity.class));
                }

                return true;
            });

            popupMenu.show();
        });

        if(IS_TABLET_UI) {
            VIEW.findViewById(R.id.btnBack).setOnClickListener((v) -> getActivity().onBackPressed());

            ((AppBarLayout) VIEW.findViewById(R.id.titleBar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                verticalOffset = Math.abs(verticalOffset);

                final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - VIEW.findViewById(R.id.toolbar).getHeight()) / 2f);

                VIEW.findViewById(R.id.titleExpanded).setAlpha(1 - opacity);
            });
        } else {
            ((AppBarLayout) VIEW.findViewById(R.id.titleBar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                verticalOffset = Math.abs(verticalOffset);

                final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - VIEW.findViewById(R.id.toolbar).getHeight()) / 2f);

                VIEW.findViewById(R.id.titleExpanded).setAlpha(1 - opacity);
            });
        }
    }

    private void init() {
        buildRecyclerView();

        KProfile.init(VIEW.findViewById(R.id.btnProfile));
    }

    private void buildRecyclerView() {
        final KSettings settings = new KSettings(CONTEXT);

        final ConcatAdapter concatAdapter = new ConcatAdapter();

        concatAdapter.addAdapter(buildAndGetFolderGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetVideoGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetMicrophoneGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetInternalAudioGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetCapturingGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetFloatingUiGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetExtraVideoGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetExtraAudioGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetWifiShareGroupAdapter());
        concatAdapter.addAdapter(buildAndGetAppGroupAdapter());

        RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(CONTEXT));

        RECYCLER_VIEW.setAdapter(concatAdapter);
    }

    private ListGroup.Adapter buildAndGetFolderGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListButtonSubText> listButtonSubTexts0 = new ArrayList<>();

        BUTTON_FOLDER = new ListButtonSubText(
            R.string.setting_folder_location,
            settings.getSavingLocationPath(true),
            (listButtonSubText) -> {
                if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                    Toast.makeText(CONTEXT, getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                if(!settings.getSavingLocationFile().exists()) {
                    settings.getSavingLocationFile().mkdirs();
                }

                final Intent intent = new Intent(CONTEXT, FilePickerActivity.class);

                intent.putExtra(FilePickerActivity.INTENT_PATH, KFile.getSavingLocation(CONTEXT).getAbsolutePath());
                intent.putExtra(FilePickerActivity.INTENT_TYPE, FilePickerActivity.TYPE_FOLDER);

                LAUNCH_ACTIVITY_RESULT_FOR_FOLDER_PICKER.launch(intent);
            },
            false
        );

        BUTTON_SCREENSHOT_FOLDER = new ListButtonSubText(
            R.string.setting_folder_location_screenshots,
            settings.getSavingScreenshotLocationPath(true),
            (listButtonSubText) -> {
                if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                    Toast.makeText(CONTEXT, getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                if(!settings.getSavingScreenshotLocationFile().exists()) {
                    settings.getSavingScreenshotLocationFile().mkdirs();
                }

                final Intent intent = new Intent(CONTEXT, FilePickerActivity.class);

                intent.putExtra(FilePickerActivity.INTENT_PATH, KFile.getSavingScreenshotLocation(CONTEXT).getAbsolutePath());
                intent.putExtra(FilePickerActivity.INTENT_TYPE, FilePickerActivity.TYPE_FOLDER);

                LAUNCH_ACTIVITY_RESULT_FOR_SCREENSHOT_FOLDER_PICKER.launch(intent);
            },
            true
        );

        listButtonSubTexts0.add(BUTTON_FOLDER);
        listButtonSubTexts0.add(BUTTON_SCREENSHOT_FOLDER);

        BUTTON_FOLDER_ADAPTER = new ListButtonSubText.Adapter(listButtonSubTexts0);

        concatAdapter.addAdapter(BUTTON_FOLDER_ADAPTER);

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_folder, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetVideoGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_resolution, settings.getVideoResolution(), KSettings.VIDEO_RESOLUTIONS, KSettings.getVideoResolutionsFormatted(CONTEXT), Constants.Sp.Profile.VIDEO_RESOLUTION, false));

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_quality, settings.getVideoBitRate(), KSettings.VIDEO_QUALITIES, KSettings.getVideoQualitiesFormatted(), Constants.Sp.Profile.VIDEO_QUALITY_bitRate, false));

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_fps, settings.getVideoFrameRate(), KSettings.VIDEO_FRAME_RATES, null, Constants.Sp.Profile.VIDEO_FRAME_RATE, false));

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_orientation, settings.getVideoOrientation(), KSettings.VIDEO_ORIENTATIONS, KSettings.getVideoOrientationsFormated(CONTEXT), Constants.Sp.Profile.VIDEO_ORIENTATION, true));

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
        listSwitches0.add(new ListSwitch(R.string.setting_internal_stereo, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_RECORD_SOUND_IN_STEREO, settings.isToRecordInternalAudioInStereo(), true));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));

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
                R.string.setting_start_url,
                settings.getBeforeStartUrl(),
                (listButtonSubText) -> {
                    new InputPopup.Text(
                        CONTEXT,
                        R.string.setting_start_url,
                        listButtonSubText.getValue(),
                        R.string.popup_btn_set,
                        new InputPopup.OnInputPopupListener() {
                            @Override
                            public void onStringInputSet(String input) {
                                if(!input.startsWith("https://") && !input.startsWith("http://")) {
                                    input = "http://" + input;
                                }

                                SP_PROFILE.edit().putString(Constants.Sp.Profile.BEFORE_START_URL, input).commit();

                                listButtonSubText.setValue(input);

                                concatAdapter.notifyDataSetChanged();
                            }
                        },
                        R.string.popup_btn_cancel,
                        null,
                        true,
                        false,
                        false
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_BEFORE_START_URL,
                settings.isToOpenUrlBeforeStart(),
                false
            )
        );

        listButtonSubTexts0.add(
            new ListButtonSubTextSwitch(
                R.string.setting_start_volume,
                settings.getBeforeStartMediaVolumePercentage() + "%",
                (listButtonSubText) -> {
                    new VolumePopup(
                        CONTEXT,
                        AudioManager.STREAM_MUSIC,
                        Integer.parseInt(listButtonSubText.getValue().replaceAll("%", "")),
                        R.string.popup_btn_set,
                        new VolumePopup.OnVolumePopupListener() {
                            @Override
                            public void onChanged(int min, int max, int volumePercentage) {
                                SP_PROFILE.edit().putInt(Constants.Sp.Profile.BEFORE_START_MEDIA_VOLUME_PERCENTAGE, volumePercentage).commit();

                                listButtonSubText.setValue(volumePercentage + "%");

                                concatAdapter.notifyDataSetChanged();
                            }
                        }
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
                    new PickerAppPopup(
                        CONTEXT,
                        R.string.setting_start_launch_select,
                        PickerAppPopup.NO_TEXT,
                        1,
                        true,
                        new ArrayList<>(Arrays.asList(SP_PROFILE.getString(Constants.Sp.Profile.BEFORE_START_LAUNCH_APP_PACKAGE, DefaultSettings.BEFORE_START_LAUNCH_APP_PACKAGE))),
                        (packages) -> {
                            final String packageName = packages.get(0);

                            SP_PROFILE.edit().putString(Constants.Sp.Profile.BEFORE_START_LAUNCH_APP_PACKAGE, packageName).commit();

                            listButtonSubText.setValue(packageName.equals(Constants.HOME_PACKAGE_NAME) ? getString(R.string.package_launch_home) : Utils.Package.getAppName(CONTEXT, packageName));

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
                        CONTEXT,
                        R.string.setting_countdown_countdown,
                        TimePopup.TYPE_SECOND,
                        Integer.parseInt(listButtonSubText.getValue()),
                        R.string.popup_btn_set,
                        new TimePopup.OnTimePopupListener() {
                            @Override
                            public void onTimeSet(int input) {
                            SP_PROFILE.edit().putInt(Constants.Sp.Profile.SECONDS_TO_START_RECORDING, input).commit();

                            listButtonSubText.setValue(input);

                            concatAdapter.notifyDataSetChanged();
                            }
                        },
                        R.string.popup_btn_cancel,
                        null,
                        true,
                        false
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_DELAY_START_RECORDING,
                settings.isToDelayStartRecordingEnabled(),
                true
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_capturing_countdown)));
        concatAdapter.addAdapter(new ListButtonSubTextSwitch.Adapter(listButtonSubTexts0, false));
    }

    private void buildAndGetCapturingGroupAdapter_stopOption(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListButtonSubTextSwitch> listButtonSubTexts1 = new ArrayList<>();

        listButtonSubTexts1.add(
            new ListButtonSubTextSwitch(
                R.string.setting_stop_time_limit,
                Utils.Formatter.timeInSeconds(settings.getSecondsTimeLimit()),
                (listButtonSubText) -> {
                    new TimePopup(
                        CONTEXT,
                        R.string.setting_stop_time_limit,
                        TimePopup.TYPE_MINUTE_SECOND,
                        Utils.Converter.timeToSeconds(listButtonSubText.getValue()),
                        R.string.popup_btn_set,
                        new TimePopup.OnTimePopupListener() {
                            @Override
                            public void onTimeSet(int input) {
                                SP_PROFILE.edit().putInt(Constants.Sp.Profile.SECONDS_TIME_LIMIT, input).commit();

                                listButtonSubText.setValue(Utils.Formatter.timeInSeconds(input));

                                concatAdapter.notifyDataSetChanged();
                            }
                        },
                        R.string.popup_btn_cancel,
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
                        CONTEXT,
                        R.string.setting_stop_battery_level,
                        Integer.parseInt(listButtonSubText.getValue().replaceAll("%", "")),
                        1,
                        99,
                        R.string.popup_btn_set,
                        new InputPopup.OnInputPopupListener() {
                            @Override
                            public void onIntInputSet(int input) {
                                SP_PROFILE.edit().putInt(Constants.Sp.Profile.STOP_ON_BATTERY_LEVEL_LEVEL, input).commit();

                                listButtonSubText.setValue(input + "%");

                                concatAdapter.notifyDataSetChanged();
                            }
                        },
                        R.string.popup_btn_cancel,
                        null,
                        true,
                        false,
                        false
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_STOP_ON_BATTERY_LEVEL,
                settings.isToStopOnBatteryLevel(),
                false
            )
        );

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_stop_screen_off, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_STOP_ON_SCREEN_OFF, settings.isToStopOnScreenOff(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_stop_shake, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_STOP_ON_SHAKE, settings.isToStopOnShake(), true));

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_capturing_stop)));
        concatAdapter.addAdapter(new ListButtonSubTextSwitch.Adapter(listButtonSubTexts1, false));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));
    }

    private ListGroup.Adapter buildAndGetFloatingUiGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        buildAndGetFloatingUiGroupAdapter_menu(settings, concatAdapter);

        buildAndGetFloatingUiGroupAdapter_menu_minimize(settings, concatAdapter);

        buildAndGetFloatingUiGroupAdapter_menu_shortcut(settings, concatAdapter);

        buildAndGetFloatingUiGroupAdapter_camera(settings, concatAdapter);

        buildAndGetFloatingUiGroupAdapter_draw(settings, concatAdapter);

        buildAndGetFloatingUiGroupAdapter_text(settings, concatAdapter);

        buildAndGetFloatingUiGroupAdapter_image(settings, concatAdapter);

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_floating_ui, concatAdapter));
    }

    private void buildAndGetFloatingUiGroupAdapter_menu(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListButtonSubTextSwitch> listButtonSubTexts0 = new ArrayList<>();

        listButtonSubTexts0.add(
            new ListButtonSubTextSwitch(
                R.string.setting_menu_show,
                settings.getMenuStyleFormatted(),
                (listButtonSubText) -> {
                    int activeIndex = 0;

                    final String[] valuesFormatted = KSettings.getMenuStylesFormatted(CONTEXT);

                    for(int i = 0; i < valuesFormatted.length; i++) {
                        if(valuesFormatted[i].equals(listButtonSubText.getValue())) {
                            activeIndex = i;

                            break;
                        }
                    }

                    new PickerPopup(
                        CONTEXT,
                        R.string.setting_menu_style,
                        KSettings.getMenuStylesFormatted(CONTEXT),
                        activeIndex,
                        (indexPicked) -> {
                            SP_PROFILE.edit().putInt(Constants.Sp.Profile.MENU_STYLE, KSettings.MENU_STYLES[indexPicked]).commit();

                            listButtonSubText.setValue(valuesFormatted[indexPicked]);

                            concatAdapter.notifyDataSetChanged();
                        }
                    ).show();
                },
                Constants.Sp.Profile.IS_TO_SHOW_FLOATING_MENU,
                settings.isToShowFloatingMenu(),
                false
            )
        );

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_time, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_SHOW_TIME_ON_MENU, settings.isToShowTimeOnMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_time_limit, R.string.setting_menu_show_time_limit_description, Constants.Sp.Profile.IS_TO_SHOW_TIME_LIMIT_ON_MENU, settings.isToShowTimeLimitOnMenuEnabled(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_pause_resume, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_SHOW_PAUSE_RESUME_BUTTON_ON_MENU, settings.isToShowPauseResumeButtonOnMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_close, R.string.setting_menu_show_close_description, Constants.Sp.Profile.IS_TO_SHOW_CLOSE_BUTTON_ON_MENU, settings.isToShowCloseButtonOnMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_screenshot, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU, settings.isToShowScreenshotButtonOnMenu(), false));

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_floating_ui_menu)));
        concatAdapter.addAdapter(new ListButtonSubTextSwitch.Adapter(listButtonSubTexts0, false));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));
        concatAdapter.addAdapter(new SimpleTextAdapter(getString(R.string.setting_menu_show_more), Gravity.CENTER, true));
    }

    private void buildAndGetFloatingUiGroupAdapter_menu_minimize(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_minimize, R.string.setting_menu_show_minimize_description, Constants.Sp.Profile.IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU, settings.isToShowMinimizeButtonOnMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_minimize_start_minimized, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_START_MENU_MINIMIZED, settings.isToStartMenuMinimized(), false));

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(
            new ListPicker.NumberInteger(
                R.string.setting_minimize_side,
                settings.getMinimizingSide(),
                KSettings.MINIMIZE_SIDES,
                KSettings.getMinimizedSidesFormatted(CONTEXT),
                Constants.Sp.Profile.MINIMIZING_SIDE,
                true
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_floating_ui_menu_minimize)));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));
        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0, false));
    }

    private void buildAndGetFloatingUiGroupAdapter_menu_shortcut(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_shortcut, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_SHOW_SHORTCUTS_BUTTON_ON_MENU, settings.isToShowShortcutsButtonOnMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_shortcut_pop, R.string.setting_shortcut_pop_description, Constants.Sp.Profile.IS_TO_OPEN_SHORTCUTS_ON_POPUP, settings.isToOpenShortcutOnPopup(), false));

        final ArrayList<ListButtonSubText> listButtonSubText0 = new ArrayList<>();

        listButtonSubText0.add(
            new ListButtonSubText(
                R.string.setting_shortcut_shortcuts,
                settings.getShortcutsNamesFormattedForMenu(),
                listButtonSubText -> {
                    new PickerAppPopup(
                        CONTEXT,
                        getString(R.string.setting_shortcut_select).replaceFirst("%d", String.valueOf(Constants.OVERLAY_MAX_SHORTCUTS)),
                        R.string.popup_btn_set,
                        Constants.OVERLAY_MAX_SHORTCUTS,
                        true,
                        Utils.Converter.jsonArrayStringToStringArrayList(SP_PROFILE.getString(Constants.Sp.Profile.SHORTCUTS_BUTTON_ON_MENU, DefaultSettings.SHORTCUTS_BUTTON_ON_MENU)),
                        (packages) -> {
                            SP_PROFILE.edit().putString(Constants.Sp.Profile.SHORTCUTS_BUTTON_ON_MENU, Utils.Converter.stringArrayListToJsonArray(packages).toString()).commit();
                            
                            listButtonSubText.setValue(Utils.Converter.arrayListPacksToStringAppNames(CONTEXT, packages));

                            concatAdapter.notifyDataSetChanged();
                        }
                    ).show();
                },
                true
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_floating_ui_menu_shortcut)));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));
        concatAdapter.addAdapter(new ListButtonSubText.Adapter(listButtonSubText0));
    }

    private void buildAndGetFloatingUiGroupAdapter_camera(KSettings settings, ConcatAdapter concatAdapter) {
        final ListCameraSize.Adapter listCameraSizeAdapter = new ListCameraSize.Adapter(new ListCameraSize(true));

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_camera, R.string.setting_menu_show_camera_description, Constants.Sp.Profile.IS_TO_SHOW_CAMERA_BUTTON_ON_MENU, settings.isToShowCameraButtonOnMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_camera_show, R.string.setting_camera_show_description, Constants.Sp.Profile.IS_TO_SHOW_FLOATING_CAMERA, settings.isToShowFloatingCamera(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_camera_orientation_toggle, R.string.setting_camera_orientation_toggle_description, Constants.Sp.Profile.IS_TO_TOGGLE_CAMERA_ORIENTATION, settings.isToToggleCameraOrientation(), false));

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(
            new ListPicker.NumberInteger(
                R.string.setting_camera_orientation,
                SP_PROFILE.getInt(Constants.Sp.Profile.CAMERA_FACING_LENS, DefaultSettings.CAMERA_FACING_LENS),
                KSettings.CAMERA_FACING_LENSES,
                KSettings.getCameraFacingLensesFormated(CONTEXT),
                Constants.Sp.Profile.CAMERA_FACING_LENS,
                false,
                new ListPicker.OnListPickerListener() {
                    @Override
                    public void onIntItemPicked(int value) {
                    listCameraSizeAdapter.notifyItemChanged(0);
                    }
                }
            )
        );

        listPickers0.add(
            new ListPicker.NumberInteger(
                R.string.setting_camera_shape,
                SP_PROFILE.getInt(Constants.Sp.Profile.CAMERA_SHAPE, DefaultSettings.CAMERA_SHAPE),
                KSettings.CAMERA_SHAPES,
                KSettings.getCameraShapesFormated(CONTEXT),
                Constants.Sp.Profile.CAMERA_SHAPE,
                false,
                new ListPicker.OnListPickerListener() {
                    @Override
                    public void onIntItemPicked(int value) {
                    listCameraSizeAdapter.notifyItemChanged(0);
                    }
                }
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_floating_ui_camera)));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));
        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0, false));

        final ArrayList<ListSwitch> listSwitches1 = new ArrayList<>();

        listSwitches1.add(new ListSwitch(R.string.setting_camera_scalable, R.string.setting_camera_scalable_description, Constants.Sp.Profile.IS_CAMERA_SCALABLE, settings.isCameraScalable(), false));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches1, false));
        concatAdapter.addAdapter(listCameraSizeAdapter);
    }

    private void buildAndGetFloatingUiGroupAdapter_draw(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_menu_show_draw, R.string.setting_menu_show_draw_description, Constants.Sp.Profile.IS_TO_SHOW_DRAW_BUTTON_ON_MENU, settings.isToShowDrawButtonOnMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_draw_show_screenshot, R.string.setting_draw_show_screenshot_description, Constants.Sp.Profile.IS_TO_SHOW_SCREENSHOT_BUTTON_ON_DRAW_MENU, settings.isToShowScreenshotButtonOnDrawMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_draw_show_draw_screenshot, R.string.setting_draw_show_draw_screenshot_description, Constants.Sp.Profile.IS_TO_SHOW_DRAW_SCREENSHOT_BUTTON_ON_DRAW_MENU, settings.isToShowDrawScreenshotButtonOnDrawMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_draw_show_undo_redo, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU, settings.isToShowUndoRedoButtonOnDrawMenu(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_draw_show_clear, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU, settings.isToShowClearButtonOnDrawMenu(), true));

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_floating_ui_draw)));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));
    }

    private void buildAndGetFloatingUiGroupAdapter_text(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListButtonColor> listButtonColors0 = new ArrayList<>();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_text_show, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_SHOW_TEXT, settings.isToShowText(), false));

        final ArrayList<ListButtonSubText> listButtonSubText0 = new ArrayList<>();

        listButtonSubText0.add(
            new ListButtonSubText(
                R.string.setting_text_text,
                settings.getTextText(),
                (listButtonSubText) -> {
                    new InputPopup.TextMultiLine(
                        CONTEXT,
                        R.string.setting_text_text,
                        listButtonSubText.getValue(),
                        R.string.popup_btn_set,
                        new InputPopup.OnInputPopupListener() {
                            @Override
                            public void onStringInputSet(String input) {
                                listButtonSubText.setValue(input);

                                SP_PROFILE.edit().putString(Constants.Sp.Profile.TEXT_TEXT, input).commit();

                                concatAdapter.notifyDataSetChanged();
                            }
                        },
                        R.string.popup_btn_cancel,
                        null,
                        true,
                        false,
                        false
                    ).show();
                },
                false
            )
        );

        listButtonColors0.add(
            new ListButtonColor(
                R.string.setting_text_color,
                settings.getTextColor(),
                (color) -> {
                    SP_PROFILE.edit().putString(Constants.Sp.Profile.TEXT_COLOR, color).commit();
                },
                false
            )
        );

        listButtonColors0.add(
            new ListButtonColor(
                R.string.setting_text_background_color,
                settings.getTextBackground(),
                (color) -> {
                    SP_PROFILE.edit().putString(Constants.Sp.Profile.TEXT_BACKGROUND, color).commit();
                },
                false
            )
        );

        final ArrayList<ListButtonSubText> listButtonSubText1 = new ArrayList<>();

        listButtonSubText1.add(
            new ListButtonSubText(
                R.string.setting_text_size,
                settings.getTextSize(),
                (listButtonSubText) -> {
                    new InputPopup.NumberInteger(
                            CONTEXT,
                            R.string.setting_text_text,
                            Integer.parseInt(listButtonSubText.getValue()),
                            R.string.popup_btn_set,
                            new InputPopup.OnInputPopupListener() {
                                @Override
                                public void onIntInputSet(int input) {
                                    listButtonSubText.setValue(input);

                                    SP_PROFILE.edit().putInt(Constants.Sp.Profile.TEXT_SIZE, input).commit();

                                    concatAdapter.notifyDataSetChanged();
                                }
                            },
                        R.string.popup_btn_cancel,
                        null,
                        true,
                        false,
                        false
                    ).show();
                },
                false
            )
        );

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(
            new ListPicker.Font(
                R.string.setting_text_font,
                settings.getTextFontPath(),
                KSettings.getFontsAvailablePath(settings),
                KSettings.getFontsAvailableFormatted(settings),
                Constants.Sp.Profile.TEXT_FONT_PATH,
                false
            )
        );

        listPickers0.add(
            new ListPicker.NumberInteger(
                R.string.setting_text_alignment,
                settings.getTextAlignment(),
                KSettings.TEXT_ALIGNMENTS,
                KSettings.getTextAlignmentsFormated(CONTEXT),
                Constants.Sp.Profile.TEXT_ALIGNMENT,
                true
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_floating_ui_text)));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));
        concatAdapter.addAdapter(new ListButtonSubText.Adapter(listButtonSubText0));
        concatAdapter.addAdapter(new ListButtonColor.Adapter(listButtonColors0));
        concatAdapter.addAdapter(new ListButtonSubText.Adapter(listButtonSubText1));
        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0, false));
    }

    private void buildAndGetFloatingUiGroupAdapter_image(KSettings settings, ConcatAdapter concatAdapter) {
        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_image_show, ListSwitch.NO_TEXT, Constants.Sp.Profile.IS_TO_SHOW_IMAGE, settings.isToShowImageEnabled(), false));

        final ArrayList<ListButtonSubText> listButtonSubTexts0 = new ArrayList<>();

        BUTTON_IMAGE = new ListButtonSubText(
            R.string.setting_image_location,
            settings.getImagePath(true) == null ? CONTEXT.getString(R.string.setting_image_no_image) : settings.getImagePath(true),
            (listButtonSubText) -> {
                if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                    Toast.makeText(CONTEXT, getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                final Intent intent = new Intent(CONTEXT, FilePickerActivity.class);

                intent.putExtra(FilePickerActivity.INTENT_PATH, settings.getImagePath(false));
                intent.putExtra(FilePickerActivity.INTENT_TYPE, FilePickerActivity.TYPE_IMAGE);

                LAUNCH_ACTIVITY_RESULT_FOR_IMAGE_PICKER.launch(intent);
            },
            false
        );

        listButtonSubTexts0.add(BUTTON_IMAGE);

        BUTTON_IMAGE_ADAPTER = new ListButtonSubText.Adapter(listButtonSubTexts0);

        LIST_IMAGE_SIZE_ADAPTER = new ListImageSize.Adapter(new ListImageSize(true));

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_floating_ui_image)));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));
        concatAdapter.addAdapter(BUTTON_IMAGE_ADAPTER);
        concatAdapter.addAdapter(LIST_IMAGE_SIZE_ADAPTER);
    }

    private ListGroup.Adapter buildAndGetExtraVideoGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_extra_vid_no_audio, R.string.setting_extra_vid_no_audio_description, Constants.Sp.Profile.IS_TO_GENERATE_VIDEO_NO_AUDIO, settings.isToGenerateVideo_NoAudio(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_extra_vid_internal, R.string.setting_extra_vid_internal_description, Constants.Sp.Profile.IS_TO_GENERATE_VIDEO_ONLY_INTERNAL_AUDIO, settings.isToGenerateVideo_OnlyInternalAudio(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_extra_vid_mic, R.string.setting_extra_vid_mic_description, Constants.Sp.Profile.IS_TO_GENERATE_VIDEO_ONLY_MIC_AUDIO, settings.isToGenerateVideo_OnlyMicAudio(), true));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_extra_video, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetExtraAudioGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_extra_audio, R.string.setting_extra_audio_description, Constants.Sp.Profile.IS_TO_GENERATE_AUDIO_AUDIO, settings.isToGenerateAudio_Audio(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_extra_internal, R.string.setting_extra_internal_description, Constants.Sp.Profile.IS_TO_GENERATE_AUDIO_ONLY_INTERNAL, settings.isToGenerateAudio_OnlyInternal(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_extra_mic, R.string.setting_extra_mic_description, Constants.Sp.Profile.IS_TO_GENERATE_AUDIO_ONLY_MIC, settings.isToGenerateAudio_OnlyMic(), true));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, false));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_extra_audio, concatAdapter));
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

        buildAndGetAppGroupAdapter_token(concatAdapter);

        buildAndGetAppGroupAdapter_notification(concatAdapter);

        buildAndGetAppGroupAdapter_storage(concatAdapter);

        buildAndGetAppGroupAdapter_ui(concatAdapter);

        buildAndGetAppGroupAdapter_performance(concatAdapter);

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_app, concatAdapter));
    }

    private void buildAndGetAppGroupAdapter_token(ConcatAdapter concatAdapter) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return;
        }

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(
            new ListSwitch(
                    R.string.settings_app_token_recycle,
                    R.string.settings_app_token_recycle_description,
                    Constants.Sp.App.IS_TO_RECYCLE_TOKEN,
                    TokenActivity.isToRecycle(CONTEXT),
                    new ListSwitch.OnListSwitchListener() {
                        @Override
                        public void onChange(boolean b) {
                            TokenActivity.clearToken();
                        }
                    },
                    false
            )
        );

        final ArrayList<ListButton> listButton0 = new ArrayList<>();

        listButton0.add(
            new ListButton(
                R.string.settings_app_token_clear,
                ListButton.NO_TEXT,
                () -> TokenActivity.clearToken(CONTEXT),
                true
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_app_token)));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, true));
        concatAdapter.addAdapter(new ListButton.Adapter(listButton0));
    }

    private void buildAndGetAppGroupAdapter_notification(ConcatAdapter concatAdapter) {
        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(
            new ListSwitch(
                R.string.notification_channel_name_processing,
                ListSwitch.NO_TEXT,
                Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_PROCESSING,
                SP_APP.getBoolean(Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_PROCESSING, DefaultSettings.IS_TO_SHOW_NOTIFICATION_PROCESSING),
                false
            )
        );

        listSwitches0.add(
            new ListSwitch(
                R.string.notification_channel_name_captured,
                ListSwitch.NO_TEXT,
                Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_CAPTURED,
                SP_APP.getBoolean(Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_CAPTURED, DefaultSettings.IS_TO_SHOW_NOTIFICATION_CAPTURED),
                false
            )
        );

        listSwitches0.add(
            new ListSwitch(
                R.string.notification_channel_name_wifi_share,
                ListSwitch.NO_TEXT,
                Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_WIFI_SHARE,
                SP_APP.getBoolean(Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_WIFI_SHARE, DefaultSettings.IS_TO_SHOW_NOTIFICATION_WIFI_SHARE),
                true
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_app_notification)));
        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0, true));
    }

    private void buildAndGetAppGroupAdapter_storage(ConcatAdapter concatAdapter) {
        STORAGE_ADAPTER = new ListStorage.Adapter(CONTEXT, true);

        final ArrayList<ListButton> listButton0 = new ArrayList<>();

        listButton0.add(
            new ListButton(
                R.string.storage_btn_clear_cache,
                ListButton.NO_TEXT,
                () -> {
                    new DialogPopup(
                        CONTEXT,
                        R.string.storage_btn_clear_cache,
                        R.string.storage_btn_clear_cache_description,
                        R.string.storage_btn_clear_cache,
                        () -> {
                            KFile.clearCache(CONTEXT);

                            updateStorageInfo();

                            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
                        },
                        R.string.popup_btn_cancel,
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
        concatAdapter.addAdapter(new SimpleTextAdapter(CONTEXT.getString(R.string.storage_message), Gravity.CENTER, false));
        concatAdapter.addAdapter(new ListButton.Adapter(listButton0));
    }

    private void buildAndGetAppGroupAdapter_ui(ConcatAdapter concatAdapter) {
        final ArrayList<ListButton> listButtons0 = new ArrayList<>();

        listButtons0.add(
            new ListButton(
                R.string.setting_ui_language,
                ListButton.NO_TEXT,
                () -> Utils.ExternalActivity.requestLanguageSettings(CONTEXT),
                false
            )
        );

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(
            new ListPicker.NumberInteger(
                R.string.setting_ui_theme,
                SP_APP.getInt(Constants.Sp.App.APP_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
                new int[]{
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                    AppCompatDelegate.MODE_NIGHT_YES,
                    AppCompatDelegate.MODE_NIGHT_NO
                },
                new String[] {
                    CONTEXT.getString(R.string.setting_ui_theme_auto),
                    CONTEXT.getString(R.string.setting_ui_theme_dark),
                    CONTEXT.getString(R.string.setting_ui_theme_light)
                },
                Constants.Sp.App.APP_THEME,
                true,
                new ListPicker.OnListPickerListener() {
                    /** @noinspection DataFlowIssue*/
                    @Override
                    public void onIntItemPicked(int value) {
                        setTheme(getActivity(), value, true);
                    }
                }
            )
        );

        concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_app_ui)));
        concatAdapter.addAdapter(new ListButton.Adapter(listButtons0));
        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0, true));
    }

    @SuppressLint("BatteryLife")
    private void buildAndGetAppGroupAdapter_performance(ConcatAdapter concatAdapter) {
        if(!((PowerManager) CONTEXT.getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(CONTEXT.getPackageName())) {
            final ArrayList<ListButton> listButton0 = new ArrayList<>();

            listButton0.add(
                new ListButton(
                    R.string.setting_performance_battery_optimization,
                    R.string.setting_performance_battery_optimization_description,
                    () -> {
                        try {
                            final Intent i = new Intent();

                            i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);

                            i.setData(Uri.parse("package:" + CONTEXT.getPackageName()));

                            startActivity(i);
                        } catch (Exception ignore) {
                            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
                        }
                    },
                    true
                )
            );

            concatAdapter.addAdapter(new ListGroupDivisor.Adapter(new ListGroupDivisor(R.string.setting_subgroup_app_performance)));
            concatAdapter.addAdapter(new ListButton.Adapter(listButton0));
        }
    }

    public static void setTheme(Activity activity, int mode, boolean relaunch) {
        AppCompatDelegate.setDefaultNightMode(mode);

        boolean isLightMode = mode == AppCompatDelegate.MODE_NIGHT_NO || !activity.getResources().getConfiguration().isNightModeActive();

        new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(isLightMode);

        if(relaunch) {
            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().relaunch();
            }

            Toast.makeText(activity, activity.getString(R.string.relaunch_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetAppSettings() {
        KSharedPreferences.resetAppSettings(CONTEXT);

        rebuildRecyclerView();

        setTheme(getActivity(), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, false);

        Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();

        ((MainActivity) getActivity()).relaunch();
    }

    private void resetProfileSettings() {
        KSharedPreferences.resetActiveProfileSp(CONTEXT);

        rebuildRecyclerView();

        Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
    }

    private void rebuildRecyclerView() {
        RECYCLER_VIEW.removeAllViews();

        buildRecyclerView();
    }

    private void updateStorageInfo() {
        try {
            STORAGE_ADAPTER.updateValues(CONTEXT);
            STORAGE_ADAPTER.notifyItemChanged(0);
        } catch (Exception ignore) {}
    }

    private void updateImageInfo() {
        try {
            if(!new File(SP_PROFILE.getString(Constants.Sp.Profile.IMAGE_PATH, DefaultSettings.IMAGE_PATH)).exists()) {
                BUTTON_IMAGE.setValue(CONTEXT.getString(R.string.setting_image_no_image));

                BUTTON_IMAGE_ADAPTER.notifyItemChanged(0);

                LIST_IMAGE_SIZE_ADAPTER.notifyItemChanged(0);
            }
        } catch (Exception ignore) {}
    }
}
