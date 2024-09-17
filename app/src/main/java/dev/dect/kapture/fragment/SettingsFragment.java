package dev.dect.kapture.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.core.os.LocaleListCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.AboutActivity;
import dev.dect.kapture.activity.TokenActivity;
import dev.dect.kapture.adapter.ListButtonSubText;
import dev.dect.kapture.adapter.ListGroup;
import dev.dect.kapture.adapter.ListPicker;
import dev.dect.kapture.adapter.ListSwitch;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;

@SuppressLint("ApplySharedPref")
public class SettingsFragment extends Fragment {
    private Context CONTEXT;

    private View VIEW;

    private RecyclerView RECYCLER_VIEW;

    private ActivityResultLauncher<Intent> LAUNCH_ACTIVITY_RESULT_FOR_FOLDER_PICKER;

    private ListButtonSubText BUTTON_FOLDER;

    private ListButtonSubText.Adapter BUTTON_FOLDER_ADAPTER;

    private SharedPreferences SP;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        VIEW = inflater.inflate(R.layout.fragment_settings, container, false);

        initVariables();

        initListeners();

        init();

        return VIEW;
    }

    private void initVariables() {
        CONTEXT = VIEW.getContext();

        SP = CONTEXT.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE);

        RECYCLER_VIEW = VIEW.findViewById(R.id.recyclerView);

        LAUNCH_ACTIVITY_RESULT_FOR_FOLDER_PICKER = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if(result.getResultCode() == Activity.RESULT_OK) {
                final String path = KFile.uriToAbsolutPath(CONTEXT, result.getData().getData());

                BUTTON_FOLDER.setValue(KFile.formatAndroidPathToUser(CONTEXT, path));
                BUTTON_FOLDER_ADAPTER.notifyItemChanged(0);

                SP.edit().putString(Constants.SP_KEY_FILE_SAVING_PATH, path).commit();
            }
        });
    }

    private void initListeners() {
        ((AppBarLayout) VIEW.findViewById(R.id.titleBar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            verticalOffset = Math.abs(verticalOffset);

            final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - VIEW.findViewById(R.id.toolbar).getHeight()) / 2f);

            VIEW.findViewById(R.id.titleExpanded).setAlpha(1 - opacity);
        });

        VIEW.findViewById(R.id.btnMore).setOnClickListener((v) -> {
            final PopupMenu popupMenu = new PopupMenu(CONTEXT, v, Gravity.END, 0, R.style.KPopupMenu);

            final Menu menu = popupMenu.getMenu();

            menu.setGroupDividerEnabled(true);

            popupMenu.getMenuInflater().inflate(R.menu.settings_more, menu);

            if(!TokenActivity.hasToken() || CapturingService.isRecording()) {
                menu.findItem(R.id.resetToken).setEnabled(false);
            }

            if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                menu.findItem(R.id.menuResetSettings).setEnabled(false);
            }

            popupMenu.setOnMenuItemClickListener((item) -> {
                final int idClicked = item.getItemId();

                if(idClicked == R.id.menuResetSettings) {
                    new DialogPopup(
                        CONTEXT,
                        R.string.menu_settings_reset_settings_confirm,
                        R.string.menu_settings_reset_settings_confirm_description,
                        R.string.popup_btn_reset,
                        this::resetSettings,
                        R.string.popup_btn_cancel,
                        null,
                        true,
                        false,
                        false
                    ).show();
                } else if(idClicked == R.id.resetToken) {
                    TokenActivity.clearToken(CONTEXT);
                } else if(idClicked == R.id.menuOpenAccessibility) {
                    Utils.ExternalActivity.requestAccessibility(CONTEXT);
                } else if(idClicked == R.id.menuShowCommand) {
                    new DialogPopup(
                        CONTEXT,
                        -1,
                        R.string.command,
                        R.string.popup_btn_ok,
                        null,
                        -1,
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
    }

    private void init() {
        buildRecyclerView();
    }

    private void buildRecyclerView() {
        final KSettings settings = new KSettings(CONTEXT);

        final ConcatAdapter concatAdapter = new ConcatAdapter();

        concatAdapter.addAdapter(buildAndGetFolderGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetVideoGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetMicrophoneGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetInternaAudioGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetFloatingUiGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetExtraVideoGroupAdapter(settings));
        concatAdapter.addAdapter(buildAndGetExtraAudioGroupAdapter(settings));
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
            () -> {
                if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                    Toast.makeText(CONTEXT, getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                try {
                    Intent intent = new Intent("com.sec.android.app.myfiles.PICK_SELECT_PATH");

                    intent.addCategory(Intent.CATEGORY_DEFAULT);

                    LAUNCH_ACTIVITY_RESULT_FOR_FOLDER_PICKER.launch(intent);
                } catch (Exception ignore) {
                    LAUNCH_ACTIVITY_RESULT_FOR_FOLDER_PICKER.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
                }
            },
            true
        );

        listButtonSubTexts0.add(BUTTON_FOLDER);

        BUTTON_FOLDER_ADAPTER = new ListButtonSubText.Adapter(listButtonSubTexts0);

        concatAdapter.addAdapter(BUTTON_FOLDER_ADAPTER);

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_folder, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetVideoGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_resolution, settings.getVideoResolution(), KSettings.VIDEO_RESOLUTIONS, KSettings.getVideoResolutionsFormatted(CONTEXT), Constants.SP_KEY_VIDEO_RESOLUTION, false));

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_quality, settings.getVideoBitRate(), KSettings.VIDEO_QUALITIES, KSettings.getVideoQualitiesFormatted(), Constants.SP_KEY_VIDEO_QUALITY_bitRate, false));

        listPickers0.add(new ListPicker.NumberInteger(R.string.setting_video_fps, settings.getVideoFrameRate(), KSettings.VIDEO_FRAME_RATES, null, Constants.SP_KEY_VIDEO_FRAME_RATE, false));

        listPickers0.add(
            new ListPicker.Text(
                R.string.setting_video_file_format,
                settings.getVideoFileFormat(),
                new String[] {
                    DefaultSettings.CAPTURE_FILE_FORMAT
                },
                new String[] {
                    CONTEXT.getString(R.string.setting_video_file_format_mp4)
                },
                Constants.SP_KEY_VIDEO_FILE_FORMAT,
                true
            )
        );

        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_video, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetMicrophoneGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_is_mic_capture, ListSwitch.NO_TEXT, Constants.SP_KEY_IS_TO_RECORD_MIC, settings.isToRecordMic(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_is_mic_volume, R.string.setting_is_mic_volume_description, Constants.SP_KEY_IS_TO_BOOST_MIC_VOLUME, settings.isToBoostMicVolume(), false));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0));

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(new ListPicker.NumberFloat(R.string.setting_mic_volume_factor, settings.getMicBoostVolumeFactor(), KSettings.MICROPHONE_BOOST, null, Constants.SP_KEY_MIC_BOOST_VOLUME_FACTOR, true));

        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_mic, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetInternaAudioGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_is_capture_internal, R.string.setting_is_capture_internal_description, Constants.SP_KEY_IS_TO_RECORD_INTERNAL_AUDIO, settings.isToRecordInternalAudio(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_is_capture_internal_stereo, ListSwitch.NO_TEXT, Constants.SP_KEY_IS_TO_RECORD_SOUND_IN_STEREO, settings.isToRecordInternalAudioInStereo(), true));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_internal, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetFloatingUiGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_is_show_floating_buttons, R.string.setting_is_show_floating_buttons_description, Constants.SP_KEY_IS_TO_SHOW_FLOATING_BUTTON, settings.isToShowFloatingButton(), true));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_floating_ui, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetExtraVideoGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_extra_vid_no_audio, R.string.setting_extra_vid_no_audio_description, Constants.SP_KEY_IS_TO_GENERATE_MP4_NO_AUDIO, settings.isToGenerateMp4NoAudio(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_extra_vid_internal, R.string.setting_extra_vid_internal_description, Constants.SP_KEY_IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO, settings.isToGenerateMp4OnlyInternalAudio(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_extra_vid_mic, R.string.setting_extra_vid_mic_description, Constants.SP_KEY_IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO, settings.isToGenerateMp4OnlyMicAudio(), true));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_extra_video, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetExtraAudioGroupAdapter(KSettings settings) {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListSwitch> listSwitches0 = new ArrayList<>();

        listSwitches0.add(new ListSwitch(R.string.setting_extra_audio, R.string.setting_extra_audio_description, Constants.SP_KEY_IS_TO_GENERATE_MP3_AUDIO, settings.isToGenerateMp3Audio(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_extra_internal, R.string.setting_extra_internal_description, Constants.SP_KEY_IS_TO_GENERATE_MP3_ONLY_INTERNAL, settings.isToGenerateMp3OnlyInternal(), false));
        listSwitches0.add(new ListSwitch(R.string.setting_extra_mic, R.string.setting_extra_mic_description, Constants.SP_KEY_IS_TO_GENERATE_MP3_ONLY_MIC, settings.isToGenerateMp3OnlyMic(), false));

        concatAdapter.addAdapter(new ListSwitch.Adapter(listSwitches0));

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(
            new ListPicker.Text(
                R.string.setting_audio_file_format,
                settings.getAudioFileFormat(),
                new String[] {
                    DefaultSettings.AUDIO_FILE_FORMAT
                },
                new String[] {
                    CONTEXT.getString(R.string.setting_audio_file_format_mp3)
                },
                Constants.SP_KEY_AUDIO_FILE_FORMAT,
                true
            )
        );

        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_extra_audio, concatAdapter));
    }

    private ListGroup.Adapter buildAndGetAppGroupAdapter() {
        final ConcatAdapter concatAdapter = new ConcatAdapter();

        final ArrayList<ListPicker> listPickers0 = new ArrayList<>();

        listPickers0.add(
            new ListPicker.Text(
                R.string.setting_app_language,
                SP.getString(Constants.SP_KEY_APP_LANGUAGE, DefaultSettings.LANGUAGE),
                Constants.LANGUAGES,
                Constants.getLanguagesNames(CONTEXT),
                Constants.SP_KEY_APP_LANGUAGE,
                false,
                new ListPicker.OnListPickerListener() {
                    @Override
                    public void onStringItemPicked(String value) {
                        setLanguage(value);
                    }
                }
            )
        );

        listPickers0.add(
            new ListPicker.NumberInteger(
                R.string.setting_app_theme,
                SP.getInt(Constants.SP_KEY_APP_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
                new int[]{
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                    AppCompatDelegate.MODE_NIGHT_YES,
                    AppCompatDelegate.MODE_NIGHT_NO
                },
                new String[]{
                    CONTEXT.getString(R.string.setting_app_theme_auto),
                    CONTEXT.getString(R.string.setting_app_theme_dark),
                    CONTEXT.getString(R.string.setting_app_theme_light)
                },
                Constants.SP_KEY_APP_THEME,
                true,
                new ListPicker.OnListPickerListener() {
                    /** @noinspection DataFlowIssue*/
                    @Override
                    public void onIntItemPicked(int value) {
                        setTheme(getActivity(), value);
                    }
                }
            )
        );

        concatAdapter.addAdapter(new ListPicker.Adapter(listPickers0));

        return new ListGroup.Adapter(new ListGroup(R.string.setting_group_app, concatAdapter));
    }

    private void setLanguage(String lang) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang));
    }

    public static void setTheme(Activity activity, int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);

        boolean isLightMode = mode == AppCompatDelegate.MODE_NIGHT_NO || !activity.getResources().getConfiguration().isNightModeActive();

        new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(isLightMode);
    }

    private void resetSettings() {
        final SharedPreferences.Editor editor = SP.edit();

        editor.remove(Constants.SP_KEY_IS_TO_RECORD_MIC);
        editor.remove(Constants.SP_KEY_IS_TO_RECORD_INTERNAL_AUDIO);
        editor.remove(Constants.SP_KEY_IS_TO_SHOW_FLOATING_BUTTON);
        editor.remove(Constants.SP_KEY_VIDEO_RESOLUTION);
        editor.remove(Constants.SP_KEY_VIDEO_FRAME_RATE);
        editor.remove(Constants.SP_KEY_FILE_SAVING_PATH);
        editor.remove(Constants.SP_KEY_IS_TO_RECORD_SOUND_IN_STEREO);
        editor.remove(Constants.SP_KEY_INTERNAL_AUDIO_SAMPLE_RATE);
        editor.remove(Constants.SP_KEY_MIC_AUDIO_SAMPLE_RATE);
        editor.remove(Constants.SP_KEY_VIDEO_FILE_FORMAT);
        editor.remove(Constants.SP_KEY_IS_TO_BOOST_MIC_VOLUME);
        editor.remove(Constants.SP_KEY_MIC_BOOST_VOLUME_FACTOR);
        editor.remove(Constants.SP_KEY_IS_TO_GENERATE_MP3_AUDIO);
        editor.remove(Constants.SP_KEY_IS_TO_GENERATE_MP3_ONLY_INTERNAL);
        editor.remove(Constants.SP_KEY_IS_TO_GENERATE_MP3_ONLY_MIC);
        editor.remove(Constants.SP_KEY_AUDIO_FILE_FORMAT);
        editor.remove(Constants.SP_KEY_IS_TO_GENERATE_MP4_NO_AUDIO);
        editor.remove(Constants.SP_KEY_IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO);
        editor.remove(Constants.SP_KEY_IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO);
        editor.remove(Constants.SP_KEY_APP_LANGUAGE);
        editor.remove(Constants.SP_KEY_APP_THEME);

        editor.commit();

        final int curPosition = RECYCLER_VIEW.computeVerticalScrollOffset();

        RECYCLER_VIEW.removeAllViews();

        buildRecyclerView();

        RECYCLER_VIEW.scrollBy(0, curPosition);

        setLanguage(DefaultSettings.LANGUAGE);
        setTheme(getActivity(), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_info_success_generic), Toast.LENGTH_SHORT).show();
    }
}
