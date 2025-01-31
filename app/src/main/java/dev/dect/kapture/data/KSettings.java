package dev.dect.kapture.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.view.Gravity;
import android.view.WindowManager;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;

/** @noinspection ResultOfMethodCallIgnored*/
public class KSettings {
    public static final int[] VIDEO_RESOLUTIONS = new int[]{-1, 1440, 1080, 720, 640, 540, 480, 360, 240},
                              VIDEO_QUALITIES = new int[]{16000000, 14000000, 12000000, 10000000, 8000000, 6000000, 4000000, 2000000, 1000000},
                              VIDEO_FRAME_RATES = new int[]{144, 120, 90, 60, 50, 40, 30, 25, 20, 15, 10},
                              CAMERA_FACING_LENSES = new int[]{CameraCharacteristics.LENS_FACING_FRONT, CameraCharacteristics.LENS_FACING_BACK},
                              CAMERA_SHAPES = new int[]{0, 1, 2}, //circle, square, square corners
                              TEXT_ALIGNMENTS = new int[]{Gravity.START, Gravity.CENTER, Gravity.END},
                              MINIMIZE_SIDES = new int[]{0, 1}, //right, left
                              MENU_STYLES = new int[]{0, 1}, //horizontal, vertical
                              VIDEO_ORIENTATIONS = new int[]{Configuration.ORIENTATION_UNDEFINED, Configuration.ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_PORTRAIT};

    public static final String[] INTERNAL_FONTS_PATHS = new String[]{"font/roboto.ttf", "font/roboto_mono.ttf", "font/bebas_neue.ttf", "font/oswald.ttf", "font/pacifico.ttf", "font/permanent_marker.ttf", "font/silkscreen.ttf", "font/monoton.ttf", "font/orbitron.ttf"};

    public static final float[] MICROPHONE_BOOST = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 20f, 25f};

    private final Context CONTEXT;

    private final boolean IS_TO_RECORD_MIC,
                          IS_TO_RECORD_INTERNAL_SOUND,
                          IS_TO_SHOW_FLOATING_MENU,
                          IS_TO_SHOW_FLOATING_CAMERA,
                          IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO,
                          IS_TO_BOOST_MIC_VOLUME,
                          IS_TO_GENERATE_MP3_AUDIO,
                          IS_TO_GENERATE_MP3_ONLY_INTERNAL,
                          IS_TO_GENERATE_MP3_ONLY_MIC,
                          IS_TO_GENERATE_MP4_NO_AUDIO,
                          IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO,
                          IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO,
                          IS_TO_TOGGLE_CAMERA_ORIENTATION,
                          IS_TO_DELAY_START_RECORDING,
                          IS_TO_USE_TIME_LIMIT,
                          IS_TO_STOP_ON_SCREEN_OFF,
                          IS_TO_STOP_ON_SHAKE,
                          IS_TO_STOP_ON_BATTERY_LEVEL,
                          IS_CAMERA_SCALABLE,
                          IS_TO_SHOW_TEXT,
                          IS_TO_SHOW_TIME_ON_MENU,
                          IS_TO_SHOW_TIME_LIMIT_ON_MENU,
                          IS_TO_SHOW_CLOSE_BUTTON_ON_MENU,
                          IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU,
                          IS_TO_SHOW_CAMERA_BUTTON_ON_MENU,
                          IS_TO_SHOW_DRAW_BUTTON_ON_MENU,
                          IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU,
                          IS_TO_SHOW_PAUSE_RESUME_BUTTON_ON_MENU,
                          IS_TO_START_MENU_MINIMIZED,
                          IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU,
                          IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU,
                          IS_TO_SHOW_SCREENSHOT_BUTTON_ON_DRAW_MENU,
                          IS_TO_SHOW_DRAW_SCREENSHOT_BUTTON_ON_DRAW_MENU,
                          IS_TO_SHOW_IMAGE,
                          IS_TO_BEFORE_START_URL,
                          IS_TO_BEFORE_START_SET_MEDIA_VOLUME,
                          IS_TO_BEFORE_START_LAUNCH_APP,
                          IS_TO_SHOW_SHORTCUTS_BUTTON_ON_MENU,
                          IS_TO_OPEN_SHORTCUTS_ON_POPUP;

    private final int VIDEO_RESOLUTION,
                      VIDEO_QUALITY,
                      VIDEO_FRAME_RATE,
                      VIDEO_WIDTH,
                      VIDEO_HEIGHT,
                      CAMERA_SIZE,
                      CAMERA_FACING_LENS,
                      CAMERA_SHAPE,
                      SECONDS_TO_START_RECORDING,
                      SECONDS_TIME_LIMIT,
                      TEXT_SIZE,
                      TEXT_ALIGNMENT,
                      MINIMIZING_SIDE,
                      MENU_STYLE,
                      VIDEO_ORIENTATION,
                      STOP_ON_BATTERY_LEVEL_LEVEL,
                      IMAGE_SIZE,
                      BEFORE_START_MEDIA_VOLUME_PERCENTAGE;

    private final float MIC_BOOST_VOLUME_FACTOR;

    private final File SAVE_LOCATION,
                       SAVE_SCREENSHOT_LOCATION;

    private final String CAPTURE_FILE_FORMAT,
                         AUDIO_FILE_FORMAT,
                         TEXT_TEXT,
                         TEXT_COLOR,
                         TEXT_BACKGROUND,
                         TEXT_FONT_PATH,
                         IMAGE_PATH,
                         BEFORE_START_URL,
                         BEFORE_START_LAUNCH_APP_PACKAGE;

    private JSONArray SHORTCUTS_BUTTON_ON_MENU;

    public KSettings(Context ctx) {
        this.CONTEXT = ctx;

        final SharedPreferences spProfile = KSharedPreferences.getActiveProfileSp(ctx),
                                spApp = KSharedPreferences.getAppSp(ctx);

        this.IS_TO_RECORD_MIC = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_RECORD_MIC, DefaultSettings.IS_TO_RECORD_MIC);
        this.IS_TO_BOOST_MIC_VOLUME = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_BOOST_MIC_VOLUME, DefaultSettings.IS_TO_BOOST_MIC_VOLUME);
        this.MIC_BOOST_VOLUME_FACTOR = spProfile.getFloat(Constants.Sp.Profile.MIC_BOOST_VOLUME_FACTOR, DefaultSettings.MIC_BOOST_VOLUME_FACTOR);

        this.IS_TO_RECORD_INTERNAL_SOUND = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_RECORD_INTERNAL_AUDIO, DefaultSettings.IS_TO_RECORD_INTERNAL_AUDIO);
        this.IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_RECORD_SOUND_IN_STEREO, DefaultSettings.IS_TO_RECORD_SOUND_IN_STEREO);

        this.IS_TO_DELAY_START_RECORDING = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_DELAY_START_RECORDING, DefaultSettings.IS_TO_DELAY_START_RECORDING);
        this.SECONDS_TO_START_RECORDING = spProfile.getInt(Constants.Sp.Profile.SECONDS_TO_START_RECORDING, DefaultSettings.SECONDS_TO_START_RECORDING);

        this.IS_TO_USE_TIME_LIMIT = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_USE_TIME_LIMIT, DefaultSettings.IS_TO_USE_TIME_LIMIT);
        this.SECONDS_TIME_LIMIT = spProfile.getInt(Constants.Sp.Profile.SECONDS_TIME_LIMIT, DefaultSettings.SECONDS_TIME_LIMIT);

        this.IS_TO_STOP_ON_SCREEN_OFF = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_STOP_ON_SCREEN_OFF, DefaultSettings.IS_TO_STOP_ON_SCREEN_OFF);
        this.IS_TO_STOP_ON_SHAKE = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_STOP_ON_SHAKE, DefaultSettings.IS_TO_STOP_ON_SHAKE);

        this.IS_TO_STOP_ON_BATTERY_LEVEL= spProfile.getBoolean(Constants.Sp.Profile.IS_TO_STOP_ON_BATTERY_LEVEL, DefaultSettings.IS_TO_STOP_ON_BATTERY_LEVEL);
        this.STOP_ON_BATTERY_LEVEL_LEVEL = spProfile.getInt(Constants.Sp.Profile.STOP_ON_BATTERY_LEVEL_LEVEL, DefaultSettings.STOP_ON_BATTERY_LEVEL_LEVEL);

        this.IS_TO_SHOW_FLOATING_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_FLOATING_MENU, DefaultSettings.IS_TO_SHOW_FLOATING_MENU);
        this.MENU_STYLE = spProfile.getInt(Constants.Sp.Profile.MENU_STYLE, DefaultSettings.MENU_STYLE);

        this.IS_TO_SHOW_FLOATING_CAMERA = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_FLOATING_CAMERA, DefaultSettings.IS_TO_SHOW_FLOATING_CAMERA);
        this.IS_TO_TOGGLE_CAMERA_ORIENTATION = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_TOGGLE_CAMERA_ORIENTATION, DefaultSettings.IS_TO_TOGGLE_CAMERA_ORIENTATION);
        this.IS_CAMERA_SCALABLE = spProfile.getBoolean(Constants.Sp.Profile.IS_CAMERA_SCALABLE, DefaultSettings.IS_CAMERA_SCALABLE);

        this.IS_TO_SHOW_TEXT = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_TEXT, DefaultSettings.IS_TO_SHOW_TEXT);
        this.TEXT_TEXT = spProfile.getString(Constants.Sp.Profile.TEXT_TEXT, ctx.getString(R.string.app_name));
        this.TEXT_SIZE = spProfile.getInt(Constants.Sp.Profile.TEXT_SIZE, DefaultSettings.TEXT_SIZE);
        this.TEXT_COLOR = spProfile.getString(Constants.Sp.Profile.TEXT_COLOR, DefaultSettings.TEXT_COLOR);
        this.TEXT_BACKGROUND = spProfile.getString(Constants.Sp.Profile.TEXT_BACKGROUND, DefaultSettings.TEXT_BACKGROUND);
        this.TEXT_ALIGNMENT = spProfile.getInt(Constants.Sp.Profile.TEXT_ALIGNMENT, DefaultSettings.TEXT_ALIGNMENT);

        final String fontPath = spProfile.getString(Constants.Sp.Profile.TEXT_FONT_PATH, DefaultSettings.TEXT_FONT_PATH);

        if(fontPath.startsWith("font/") || new File(fontPath).exists()) {
            this.TEXT_FONT_PATH = fontPath;
        } else {
            this.TEXT_FONT_PATH = DefaultSettings.TEXT_FONT_PATH;
        }

        this.IS_TO_SHOW_IMAGE = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_IMAGE, DefaultSettings.IS_TO_SHOW_IMAGE);
        this.IMAGE_PATH = spProfile.getString(Constants.Sp.Profile.IMAGE_PATH, DefaultSettings.IMAGE_PATH);
        this.IMAGE_SIZE = spProfile.getInt(Constants.Sp.Profile.IMAGE_SIZE, DefaultSettings.IMAGE_SIZE);

        this.IS_TO_SHOW_TIME_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_TIME_ON_MENU, DefaultSettings.IS_TO_SHOW_TIME_ON_MENU);
        this.IS_TO_SHOW_TIME_LIMIT_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_TIME_LIMIT_ON_MENU, DefaultSettings.IS_TO_SHOW_TIME_LIMIT_ON_MENU);
        this.IS_TO_SHOW_CLOSE_BUTTON_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_CLOSE_BUTTON_ON_MENU, DefaultSettings.IS_TO_SHOW_CLOSE_BUTTON_ON_MENU);
        this.IS_TO_SHOW_CAMERA_BUTTON_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_CAMERA_BUTTON_ON_MENU, DefaultSettings.IS_TO_SHOW_CAMERA_BUTTON_ON_MENU);
        this.IS_TO_SHOW_DRAW_BUTTON_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_DRAW_BUTTON_ON_MENU, DefaultSettings.IS_TO_SHOW_DRAW_BUTTON_ON_MENU);
        this.IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU, DefaultSettings.IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU);
        this.IS_TO_SHOW_PAUSE_RESUME_BUTTON_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_PAUSE_RESUME_BUTTON_ON_MENU, DefaultSettings.IS_TO_SHOW_PAUSE_RESUME_BUTTON_ON_MENU);

        this.IS_TO_SHOW_SHORTCUTS_BUTTON_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_SHORTCUTS_BUTTON_ON_MENU, DefaultSettings.IS_TO_SHOW_SHORTCUTS_BUTTON_ON_MENU);

        try {
            this.SHORTCUTS_BUTTON_ON_MENU = new JSONArray(spProfile.getString(Constants.Sp.Profile.SHORTCUTS_BUTTON_ON_MENU, DefaultSettings.SHORTCUTS_BUTTON_ON_MENU));
        } catch (Exception ignore) {
            this.SHORTCUTS_BUTTON_ON_MENU = new JSONArray();
            this.SHORTCUTS_BUTTON_ON_MENU.put(Constants.HOME_PACKAGE_NAME);
        }

        this.IS_TO_OPEN_SHORTCUTS_ON_POPUP = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_OPEN_SHORTCUTS_ON_POPUP, DefaultSettings.IS_TO_OPEN_SHORTCUTS_ON_POPUP);

        this.IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU, DefaultSettings.IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU);
        this.IS_TO_START_MENU_MINIMIZED = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_START_MENU_MINIMIZED, DefaultSettings.IS_TO_START_MENU_MINIMIZED);
        this.MINIMIZING_SIDE = spProfile.getInt(Constants.Sp.Profile.MINIMIZING_SIDE, DefaultSettings.MINIMIZING_SIDE);

        this.IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU, DefaultSettings.IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU);
        this.IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU, DefaultSettings.IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU);
        this.IS_TO_SHOW_SCREENSHOT_BUTTON_ON_DRAW_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_SCREENSHOT_BUTTON_ON_DRAW_MENU, DefaultSettings.IS_TO_SHOW_SCREENSHOT_BUTTON_ON_DRAW_MENU);
        this.IS_TO_SHOW_DRAW_SCREENSHOT_BUTTON_ON_DRAW_MENU = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_SHOW_DRAW_SCREENSHOT_BUTTON_ON_DRAW_MENU, DefaultSettings.IS_TO_SHOW_DRAW_SCREENSHOT_BUTTON_ON_DRAW_MENU);

        this.IS_TO_BEFORE_START_URL = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_BEFORE_START_URL, DefaultSettings.IS_TO_BEFORE_START_URL);
        this.BEFORE_START_URL = spProfile.getString(Constants.Sp.Profile.BEFORE_START_URL, DefaultSettings.BEFORE_START_URL);

        this.IS_TO_BEFORE_START_SET_MEDIA_VOLUME = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_BEFORE_START_SET_MEDIA_VOLUME, DefaultSettings.IS_TO_BEFORE_START_SET_MEDIA_VOLUME);
        this.BEFORE_START_MEDIA_VOLUME_PERCENTAGE = spProfile.getInt(Constants.Sp.Profile.BEFORE_START_MEDIA_VOLUME_PERCENTAGE, DefaultSettings.BEFORE_START_MEDIA_VOLUME_PERCENTAGE);

        this.IS_TO_BEFORE_START_LAUNCH_APP = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_BEFORE_START_LAUNCH_APP, DefaultSettings.IS_TO_BEFORE_START_LAUNCH_APP);
        this.BEFORE_START_LAUNCH_APP_PACKAGE = spProfile.getString(Constants.Sp.Profile.BEFORE_START_LAUNCH_APP_PACKAGE, DefaultSettings.BEFORE_START_LAUNCH_APP_PACKAGE);

        this.CAMERA_SIZE = spProfile.getInt(Constants.Sp.Profile.CAMERA_SIZE, DefaultSettings.CAMERA_SIZE);
        this.CAMERA_FACING_LENS = spProfile.getInt(Constants.Sp.Profile.CAMERA_FACING_LENS, DefaultSettings.CAMERA_FACING_LENS);
        this.CAMERA_SHAPE = spProfile.getInt(Constants.Sp.Profile.CAMERA_SHAPE, DefaultSettings.CAMERA_SHAPE);

        this.VIDEO_RESOLUTION = spProfile.getInt(Constants.Sp.Profile.VIDEO_RESOLUTION, DefaultSettings.VIDEO_RESOLUTION);
        this.VIDEO_QUALITY = spProfile.getInt(Constants.Sp.Profile.VIDEO_QUALITY_bitRate, DefaultSettings.VIDEO_QUALITY_bitRate);
        this.VIDEO_FRAME_RATE = spProfile.getInt(Constants.Sp.Profile.VIDEO_FRAME_RATE, DefaultSettings.VIDEO_FRAME_RATE);
        this.VIDEO_ORIENTATION = spProfile.getInt(Constants.Sp.Profile.VIDEO_ORIENTATION, DefaultSettings.VIDEO_ORIENTATION);

        this.IS_TO_GENERATE_MP3_AUDIO = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_GENERATE_MP3_AUDIO, DefaultSettings.IS_TO_GENERATE_MP3_AUDIO);
        this.IS_TO_GENERATE_MP3_ONLY_INTERNAL = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_GENERATE_MP3_ONLY_INTERNAL, DefaultSettings.IS_TO_GENERATE_MP3_ONLY_INTERNAL);
        this.IS_TO_GENERATE_MP3_ONLY_MIC = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_GENERATE_MP3_ONLY_MIC, DefaultSettings.IS_TO_GENERATE_MP3_ONLY_MIC);

        this.IS_TO_GENERATE_MP4_NO_AUDIO = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_GENERATE_MP4_NO_AUDIO, DefaultSettings.IS_TO_GENERATE_MP4_NO_AUDIO);
        this.IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO, DefaultSettings.IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO);
        this.IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO, DefaultSettings.IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO);

        this.SAVE_LOCATION = new File(spApp.getString(Constants.Sp.App.FILE_SAVING_PATH, KFile.getDefaultFileLocation(ctx).getAbsolutePath()));

        this.SAVE_SCREENSHOT_LOCATION = new File(spApp.getString(Constants.Sp.App.SCREENSHOT_FILE_SAVING_PATH, KFile.getDefaultScreenshotFileLocation(ctx).getAbsolutePath()));

        this.CAPTURE_FILE_FORMAT = spProfile.getString(Constants.Sp.Profile.VIDEO_FILE_FORMAT, DefaultSettings.CAPTURE_FILE_FORMAT);
        this.AUDIO_FILE_FORMAT = spProfile.getString(Constants.Sp.Profile.AUDIO_FILE_FORMAT, DefaultSettings.AUDIO_FILE_FORMAT);

        final int[] wh = getSize(ctx, VIDEO_ORIENTATION);

        this.VIDEO_WIDTH = wh[0];
        this.VIDEO_HEIGHT = wh[1];
    }

    private int[] getSize(Context ctx, int orientation) {
        if(orientation == Configuration.ORIENTATION_UNDEFINED) {
            try {
                final Configuration config = ctx.getResources().getConfiguration();

                final Class configClass = config.getClass();

                if(configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass) == configClass.getField("semDesktopModeEnabled").getInt(config)) {
                    return getSize(ctx, Configuration.ORIENTATION_LANDSCAPE);
                }
            } catch (Exception ignore) {}

            return getSize(ctx, ctx.getResources().getConfiguration().orientation);
        }

        final Rect rect = ctx.getSystemService(WindowManager.class).getCurrentWindowMetrics().getBounds();

        final int min = Math.min(rect.width(), rect.height()),
                  max = Math.max(rect.width(), rect.height());

        int videoResolutionUsing = VIDEO_RESOLUTION;

        if(videoResolutionUsing == -1) {
            for(int i = 0; i < VIDEO_RESOLUTIONS.length; i++) {
                if(VIDEO_RESOLUTIONS[i] < 0) {
                    continue;
                }

                if(VIDEO_RESOLUTIONS[i] <= min) {
                    videoResolutionUsing = VIDEO_RESOLUTIONS[i];

                    break;
                }
            }
        }

        final float scale = (float) videoResolutionUsing / (float) min;

        int scaled = (int) ((float) max * scale);

        scaled = scaled % 2 == 0 ? scaled : (scaled + 1);

        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            return new int[]{videoResolutionUsing, scaled};
        }

        return new int[]{scaled, videoResolutionUsing};
    }

    public boolean isToRecordMic() {
        return IS_TO_RECORD_MIC;
    }

    public boolean isToRecordInternalAudio() {
        return IS_TO_RECORD_INTERNAL_SOUND;
    }

    public boolean isToShowFloatingMenu() {
        return IS_TO_SHOW_FLOATING_MENU;
    }

    public boolean isToShowFloatingCamera() {
        return IS_TO_SHOW_FLOATING_CAMERA;
    }

    public boolean isToRecordInternalAudioInStereo() {
        return IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO;
    }

    public boolean isToBoostMicVolume() {
        return IS_TO_BOOST_MIC_VOLUME;
    }

    public boolean isToToggleCameraOrientation() {
        return IS_TO_TOGGLE_CAMERA_ORIENTATION;
    }

    public boolean isCameraScalable() {
        return IS_CAMERA_SCALABLE;
    }

    public int getVideoResolution() {
        return VIDEO_RESOLUTION;
    }

    public int getVideoFrameRate() {
        return VIDEO_FRAME_RATE;
    }

    public int getVideoWidth() {
        return VIDEO_WIDTH;
    }

    public int getVideoHeight() {
        return VIDEO_HEIGHT;
    }

    public int getVideoDpi() {
        return Resources.getSystem().getDisplayMetrics().densityDpi;
    }

    public int getVideoOrientation() {
        return VIDEO_ORIENTATION;
    }

    public float getMicBoostVolumeFactor() {
        return MIC_BOOST_VOLUME_FACTOR;
    }

    public File getSavingLocationFile() {
        if(!SAVE_LOCATION.exists()) {
            SAVE_LOCATION.mkdirs();
        }

        return SAVE_LOCATION;
    }

    public String getSavingLocationPath(boolean simpleFormat) {
        return simpleFormat ? KFile.formatAndroidPathToUser(CONTEXT, SAVE_LOCATION.getAbsolutePath()) : SAVE_LOCATION.getAbsolutePath();
    }

    public File getSavingScreenshotLocationFile() {
        if(!SAVE_SCREENSHOT_LOCATION.exists()) {
            SAVE_SCREENSHOT_LOCATION.mkdirs();
        }

        return SAVE_SCREENSHOT_LOCATION;
    }

    public String getSavingScreenshotLocationPath(boolean simpleFormat) {
        return simpleFormat ? KFile.formatAndroidPathToUser(CONTEXT, SAVE_SCREENSHOT_LOCATION.getAbsolutePath()) : SAVE_SCREENSHOT_LOCATION.getAbsolutePath();
    }

    public int getVideoBitRate() {
        return VIDEO_QUALITY;
    }

    public int getCameraSize() {
        return CAMERA_SIZE;
    }

    public int getCameraFacingLens() {
        return CAMERA_FACING_LENS;
    }

    public int getCameraShape() {
        return CAMERA_SHAPE;
    }

    public int getMilliSecondsToStartRecording() {
        return SECONDS_TO_START_RECORDING * 1000;
    }

    public int getSecondsToStartRecording() {
        return SECONDS_TO_START_RECORDING;
    }

    public boolean isToDelayStartRecordingEnabled() {
        return IS_TO_DELAY_START_RECORDING;
    }

    public boolean isToDelayStartRecording() {
        return isToDelayStartRecordingEnabled() && SECONDS_TO_START_RECORDING > 0;
    }

    public boolean isToUseTimeLimitEnabled() {
        return IS_TO_USE_TIME_LIMIT;
    }

    public boolean isToUseTimeLimit() {
        return isToUseTimeLimitEnabled() && SECONDS_TIME_LIMIT > 0;
    }

    public boolean isToStopOnScreenOff() {
        return IS_TO_STOP_ON_SCREEN_OFF;
    }

    public boolean isToStopOnShake() {
        return IS_TO_STOP_ON_SHAKE;
    }

    public boolean isToStopOnBatteryLevel() {
        return IS_TO_STOP_ON_BATTERY_LEVEL;
    }

    public int getStopOnBatteryLevelLevel() {
        return STOP_ON_BATTERY_LEVEL_LEVEL;
    }

    public int getMilliSecondsTimeLimit() {
        return SECONDS_TIME_LIMIT * 1000;
    }

    public int getSecondsTimeLimit() {
        return SECONDS_TIME_LIMIT;
    }

    public String getVideoFileFormat() {
        return CAPTURE_FILE_FORMAT;
    }

    public String getAudioFileFormat() {
        return AUDIO_FILE_FORMAT;
    }

    public boolean isToGenerateMp3Audio() {
        return IS_TO_GENERATE_MP3_AUDIO;
    }

    public boolean isToGenerateMp3OnlyInternal() {
        return IS_TO_GENERATE_MP3_ONLY_INTERNAL;
    }

    public boolean isToGenerateMp3OnlyMic() {
        return IS_TO_GENERATE_MP3_ONLY_MIC;
    }

    public boolean isToGenerateMp4NoAudio() {
        return IS_TO_GENERATE_MP4_NO_AUDIO;
    }

    public boolean isToGenerateMp4OnlyInternalAudio() {
        return IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO;
    }

    public boolean isToGenerateMp4OnlyMicAudio() {
        return IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO;
    }

    public int getCameraShapeResource() {
        switch(CAMERA_SHAPE) {
            case 0:
                return R.drawable.camera_frame_oval;

            case 1:
                return R.drawable.camera_frame_square;

            case 2:
                return R.drawable.camera_frame_square_corner;

            default:
                return -1;
        }
    }

    public boolean isToShowText() {
        return IS_TO_SHOW_TEXT;
    }

    public boolean isToShowImageEnabled() {
        return IS_TO_SHOW_IMAGE;
    }

    public boolean isToShowImage() {
        return isToShowImageEnabled() && new File(IMAGE_PATH).exists();
    }

    public String getImagePath(boolean simpleFormat) {
        return (IMAGE_PATH == null || IMAGE_PATH.trim().equals("") || !new File(IMAGE_PATH).exists()) ? null : (simpleFormat ?  KFile.formatAndroidPathToUser(CONTEXT, IMAGE_PATH) : IMAGE_PATH);
    }

    public int getImageSize() {
        return IMAGE_SIZE;
    }

    public String getTextText() {
        return TEXT_TEXT;
    }

    public int getTextSize() {
        return TEXT_SIZE;
    }

    public String getTextFontPath() {
        return TEXT_FONT_PATH;
    }

    public Typeface getTextFontTypeface() {
       return getTypeFaceForFontPath(CONTEXT, TEXT_FONT_PATH);
    }

    public String getTextColor() {
        return TEXT_COLOR;
    }

    public String getTextBackground() {
        return TEXT_BACKGROUND;
    }

    public int getTextAlignment() {
        return TEXT_ALIGNMENT;
    }

    public boolean isToShowTimeOnMenu() {
        return IS_TO_SHOW_TIME_ON_MENU;
    }

    public boolean isToShowTimeLimitOnMenuEnabled() {
        return IS_TO_SHOW_TIME_LIMIT_ON_MENU;
    }

    public boolean isToShowTimeLimitOnMenu() {
        return isToShowTimeLimitOnMenuEnabled() && isToUseTimeLimit();
    }

    public boolean isToShowCloseButtonOnMenu() {
        return IS_TO_SHOW_CLOSE_BUTTON_ON_MENU;
    }

    public boolean isToShowCameraButtonOnMenu() {
        return IS_TO_SHOW_CAMERA_BUTTON_ON_MENU;
    }

    public boolean isToShowDrawButtonOnMenu() {
        return IS_TO_SHOW_DRAW_BUTTON_ON_MENU;
    }

    public boolean isToShowScreenshotButtonOnMenu() {
        return IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU;
    }

    public boolean isToShowMinimizeButtonOnMenu() {
        return IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU;
    }

    public boolean isToShowPauseResumeButtonOnMenu() {
        return IS_TO_SHOW_PAUSE_RESUME_BUTTON_ON_MENU;
    }

    public boolean isToShowShortcutsButtonOnMenu() {
        return IS_TO_SHOW_SHORTCUTS_BUTTON_ON_MENU;
    }

    public boolean isToOpenShortcutOnPopup() {
        return IS_TO_OPEN_SHORTCUTS_ON_POPUP;
    }

    public boolean isToStartMenuMinimized() {
        return IS_TO_START_MENU_MINIMIZED;
    }

    public boolean isToShowUndoRedoButtonOnDrawMenu() {
        return IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU;
    }

    public boolean isToShowClearButtonOnDrawMenu() {
        return IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU;
    }

    public boolean isToShowScreenshotButtonOnDrawMenu() {
        return IS_TO_SHOW_SCREENSHOT_BUTTON_ON_DRAW_MENU;
    }

    public boolean isToShowDrawScreenshotButtonOnDrawMenu() {
        return IS_TO_SHOW_DRAW_SCREENSHOT_BUTTON_ON_DRAW_MENU;
    }

    public boolean isToOpenUrlBeforeStart() {
        return IS_TO_BEFORE_START_URL;
    }

    public String getBeforeStartUrl() {
        return BEFORE_START_URL;
    }

    public boolean isToSetMediaVolumeBeforeStart() {
        return IS_TO_BEFORE_START_SET_MEDIA_VOLUME;
    }

    public int getBeforeStartMediaVolumePercentage() {
        return BEFORE_START_MEDIA_VOLUME_PERCENTAGE;
    }

    public boolean isToSetLaunchAppBeforeStart() {
        return IS_TO_BEFORE_START_LAUNCH_APP;
    }

    public String getBeforeStartLaunchAppPackage() {
        return BEFORE_START_LAUNCH_APP_PACKAGE;
    }

    public String getBeforeStartLaunchAppName() {
        if(getBeforeStartLaunchAppPackage().equals(Constants.HOME_PACKAGE_NAME)) {
            return CONTEXT.getString(R.string.package_launch_home);
        }

        return Utils.Package.getAppName(CONTEXT, getBeforeStartLaunchAppPackage());
    }

    public int getMinimizingSide() {
        return MINIMIZING_SIDE;
    }

    public int getMenuStyle() {
        return MENU_STYLE;
    }

    public String getMenuStyleFormatted() {
        for(int i = 0; i < MENU_STYLES.length; i++) {
            if(MENU_STYLES[i] == MENU_STYLE) {
                return getMenuStylesFormatted(CONTEXT)[i];
            }
        }

        return "?";
    }

    @SuppressLint("ApplySharedPref")
    public JSONArray getShortcutsPackagesForMenu() {
        JSONArray shortcuts = new JSONArray();

        boolean update = false;

        try {
            for(int i = 0; i < SHORTCUTS_BUTTON_ON_MENU.length(); i++) {
                if(Utils.Package.isAppInstalledAndEnabled(CONTEXT, SHORTCUTS_BUTTON_ON_MENU.getString(i))) {
                    shortcuts.put(SHORTCUTS_BUTTON_ON_MENU.getString(i));
                } else {
                    update = true;
                }
            }

            SHORTCUTS_BUTTON_ON_MENU = shortcuts;

            if(update) {
                KSharedPreferences.getActiveProfileSp(CONTEXT).edit().putString(Constants.Sp.Profile.SHORTCUTS_BUTTON_ON_MENU, shortcuts.toString()).commit();
            }
        } catch (Exception ignore) {
            shortcuts = new JSONArray();
            shortcuts.put(Constants.HOME_PACKAGE_NAME);
        }

        return shortcuts;
    }

    public String getShortcutsNamesFormattedForMenu() {
        return Utils.Converter.jsonArrayPacksToStringAppNames(CONTEXT, getShortcutsPackagesForMenu());
    }

    public static String[] getMenuStylesFormatted(Context ctx) {
        final String[] s = new String[MENU_STYLES.length];

        for(int i = 0; i < MENU_STYLES.length; i++) {
            switch(MENU_STYLES[i]) {
                case 0:
                    s[i] = ctx.getString(R.string.setting_menu_show_style_horizontal);
                    break;

                case 1:
                    s[i] = ctx.getString(R.string.setting_menu_show_style_vertical);
                    break;

                default:
                    s[i] = "?";
                    break;
            }
        }

        return s;
    }

    public static String[] getMinimizedSidesFormatted(Context ctx) {
        final String[] s = new String[MINIMIZE_SIDES.length];

        for(int i = 0; i < MINIMIZE_SIDES.length; i++) {
            switch(MINIMIZE_SIDES[i]) {
                case 0:
                    s[i] = ctx.getString(R.string.setting_minimize_side_right);
                    break;

                case 1:
                    s[i] = ctx.getString(R.string.setting_minimize_side_left);
                    break;

                default:
                    s[i] = "?";
                    break;
            }
        }

        return s;
    }

    public static Typeface getTypeFaceForFontPath(Context ctx, String path) {
        if(path.startsWith("font/")) {
            return Typeface.createFromAsset(ctx.getAssets(), path);
        } else if(new File(path).exists()) {
            return Typeface.createFromFile(path);
        }

        return Typeface.createFromAsset(ctx.getAssets(), DefaultSettings.TEXT_FONT_PATH);
    }

    public static String[] getFontsAvailableFormatted(KSettings ks) {
        final String[] paths = getFontsAvailablePath(ks),
                       s = new String[paths.length];

        for(int i = 0; i < paths.length; i++) {
            if(paths[i].startsWith("font/")) {
                String name = paths[i].replaceFirst("font/", "").replaceFirst(".ttf", "").replaceAll("_", " ");

                name = String.valueOf(name.charAt(0)).toUpperCase(Locale.ROOT) + name.substring(1);

                s[i] = name;
            } else {
                s[i] = KFile.removeFileExtension(new File(paths[i]).getName()).replaceAll("_", " ").replaceAll("-", " ");
            }
        }

        return s;
    }

    public static String[] getFontsAvailablePath(KSettings ks) {
        return Stream.concat(Arrays.stream(INTERNAL_FONTS_PATHS), Arrays.stream(getExternalFontsPaths(ks))).toArray(String[]::new);
    }

    public static String[] getExternalFontsPaths(KSettings ks) {
        final File folder = ks.getSavingLocationFile();

        if(folder.exists()) {
            final ArrayList<String> paths = new ArrayList<>();

            final File[] files = folder.listFiles();

            if(files != null) {
                for (File file : files) {
                    if (file.isFile() && KFile.getFileExtension(file).equals("ttf")) {
                        paths.add(file.getAbsolutePath());
                    }
                }
            }

            return paths.toArray(new String[0]);
        }

        return new String[]{};
    }

    public static String[] getVideoResolutionsFormatted(Context ctx) {
        final String[] s = new String[VIDEO_RESOLUTIONS.length];

        for(int i = 0; i < VIDEO_RESOLUTIONS.length; i++) {
            if(VIDEO_RESOLUTIONS[i] == -1) {
                s[i] = ctx.getString(R.string.setting_video_resolution_auto);
            } else {
                s[i] = VIDEO_RESOLUTIONS[i] + "p";
            }
        }

        return s;
    }

    public static String[] getVideoQualitiesFormatted() {
        final String[] s = new String[VIDEO_QUALITIES.length];

        for(int i = 0; i < VIDEO_QUALITIES.length; i++) {
            s[i] = VIDEO_QUALITIES[i] / 1000000 + " Mbps";
        }

        return s;
    }

    public static String[] getCameraFacingLensesFormated(Context ctx) {
        final String[] s = new String[CAMERA_FACING_LENSES.length];

        for(int i = 0; i < CAMERA_FACING_LENSES.length; i++) {
            switch(CAMERA_FACING_LENSES[i]) {
                case CameraCharacteristics.LENS_FACING_FRONT:
                    s[i] = ctx.getString(R.string.setting_camera_orientation_front);
                    break;

                case CameraCharacteristics.LENS_FACING_BACK:
                    s[i] = ctx.getString(R.string.setting_camera_orientation_back);
                    break;

                default:
                    s[i] = "?";
                    break;
            }
        }

        return s;
    }

    public static String[] getTextAlignmentsFormated(Context ctx) {
        final String[] s = new String[TEXT_ALIGNMENTS.length];

        for(int i = 0; i < TEXT_ALIGNMENTS.length; i++) {
            switch(TEXT_ALIGNMENTS[i]) {
                case Gravity.START:
                    s[i] = ctx.getString(R.string.setting_text_alignment_start);
                    break;

                case Gravity.CENTER:
                    s[i] = ctx.getString(R.string.setting_text_alignment_center);
                    break;

                case Gravity.END:
                    s[i] = ctx.getString(R.string.setting_text_alignment_end);
                    break;

                default:
                    s[i] = "?";
                    break;
            }
        }

        return s;
    }

    public static String[] getCameraShapesFormated(Context ctx) {
        final String[] s = new String[CAMERA_SHAPES.length];

        for(int i = 0; i < CAMERA_SHAPES.length; i++) {
            switch(CAMERA_SHAPES[i]) {
                case 0:
                    s[i] = ctx.getString(R.string.setting_camera_shape_circle);
                    break;

                case 1:
                    s[i] = ctx.getString(R.string.setting_camera_shape_square);
                    break;

                case 2:
                    s[i] = ctx.getString(R.string.setting_camera_shape_square_corner);
                    break;

                default:
                    s[i] = "?";
                    break;
            }
        }

        return s;
    }

    public static String[] getVideoOrientationsFormated(Context ctx) {
        final String[] s = new String[VIDEO_ORIENTATIONS.length];

        for(int i = 0; i < VIDEO_ORIENTATIONS.length; i++) {
            switch(VIDEO_ORIENTATIONS[i]) {
                case Configuration.ORIENTATION_UNDEFINED:
                    s[i] = ctx.getString(R.string.setting_video_orientation_auto);
                    break;

                case Configuration.ORIENTATION_LANDSCAPE:
                    s[i] = ctx.getString(R.string.setting_video_orientation_landscape);
                    break;

                case Configuration.ORIENTATION_PORTRAIT:
                    s[i] = ctx.getString(R.string.setting_video_orientation_portrait);
                    break;

                default:
                    s[i] = "?";
                    break;
            }
        }

        return s;
    }

    public static int getCameraShapeResourceExample(int shape) {
        switch(shape) {
            case 0:
                return R.drawable.camera_frame_example_background_oval;

            case 1:
                return R.drawable.camera_frame_example_background_square;

            case 2:
                return R.drawable.camera_frame_example_background_square_corner;

            default:
                return -1;
        }
    }
}
