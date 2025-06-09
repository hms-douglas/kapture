package dev.dect.kapture.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.WindowManager;

import java.io.File;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;

/** @noinspection ResultOfMethodCallIgnored*/
public class KSettings {
    public static final int[] VIDEO_RESOLUTIONS = new int[]{-1, 480, 450, 360, 240},
                              VIDEO_QUALITIES = new int[]{8000000, 6000000, 4000000, 2000000, 1000000},
                              VIDEO_FRAME_RATES = new int[]{40, 30, 25, 20, 15, 10}; //horizontal, vertical

    private final Context CONTEXT;

    private final boolean IS_TO_RECORD_MIC,
                          IS_TO_RECORD_INTERNAL_SOUND,
                          IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO,
                          IS_TO_DELAY_START_RECORDING,
                          IS_TO_USE_TIME_LIMIT,
                          IS_TO_STOP_ON_BATTERY_LEVEL,
                          IS_TO_BEFORE_START_SET_MEDIA_VOLUME,
                          IS_TO_BEFORE_START_LAUNCH_APP;

    private final int VIDEO_RESOLUTION,
                      VIDEO_QUALITY,
                      VIDEO_FRAME_RATE,
                      VIDEO_WIDTH,
                      VIDEO_HEIGHT,
                      SECONDS_TO_START_RECORDING,
                      SECONDS_TIME_LIMIT,
                      STOP_ON_BATTERY_LEVEL_LEVEL,
                      BEFORE_START_MEDIA_VOLUME_PERCENTAGE,
                      AUDIO_SAMPLE_RATE,
                      AUDIO_QUALITY;

    private final File SAVE_LOCATION;

    private final String BEFORE_START_LAUNCH_APP_PACKAGE;

    public KSettings(Context ctx) {
        this.CONTEXT = ctx;

        final SharedPreferences spProfile = KSharedPreferences.getActiveProfileSp(ctx),
                                spApp = KSharedPreferences.getAppSp(ctx);

        this.IS_TO_RECORD_MIC = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_RECORD_MIC, DefaultSettings.IS_TO_RECORD_MIC);

        this.IS_TO_RECORD_INTERNAL_SOUND = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_RECORD_INTERNAL_AUDIO, DefaultSettings.IS_TO_RECORD_INTERNAL_AUDIO);
        this.IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_RECORD_SOUND_IN_STEREO, DefaultSettings.IS_TO_RECORD_SOUND_IN_STEREO);

        this.IS_TO_DELAY_START_RECORDING = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_DELAY_START_RECORDING, DefaultSettings.IS_TO_DELAY_START_RECORDING);
        this.SECONDS_TO_START_RECORDING = spProfile.getInt(Constants.Sp.Profile.SECONDS_TO_START_RECORDING, DefaultSettings.SECONDS_TO_START_RECORDING);

        this.IS_TO_USE_TIME_LIMIT = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_USE_TIME_LIMIT, DefaultSettings.IS_TO_USE_TIME_LIMIT);
        this.SECONDS_TIME_LIMIT = spProfile.getInt(Constants.Sp.Profile.SECONDS_TIME_LIMIT, DefaultSettings.SECONDS_TIME_LIMIT);

        this.IS_TO_STOP_ON_BATTERY_LEVEL= spProfile.getBoolean(Constants.Sp.Profile.IS_TO_STOP_ON_BATTERY_LEVEL, DefaultSettings.IS_TO_STOP_ON_BATTERY_LEVEL);
        this.STOP_ON_BATTERY_LEVEL_LEVEL = spProfile.getInt(Constants.Sp.Profile.STOP_ON_BATTERY_LEVEL_LEVEL, DefaultSettings.STOP_ON_BATTERY_LEVEL_LEVEL);

        this.IS_TO_BEFORE_START_SET_MEDIA_VOLUME = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_BEFORE_START_SET_MEDIA_VOLUME, DefaultSettings.IS_TO_BEFORE_START_SET_MEDIA_VOLUME);
        this.BEFORE_START_MEDIA_VOLUME_PERCENTAGE = spProfile.getInt(Constants.Sp.Profile.BEFORE_START_MEDIA_VOLUME_PERCENTAGE, DefaultSettings.BEFORE_START_MEDIA_VOLUME_PERCENTAGE);

        this.IS_TO_BEFORE_START_LAUNCH_APP = spProfile.getBoolean(Constants.Sp.Profile.IS_TO_BEFORE_START_LAUNCH_APP, DefaultSettings.IS_TO_BEFORE_START_LAUNCH_APP);
        this.BEFORE_START_LAUNCH_APP_PACKAGE = spProfile.getString(Constants.Sp.Profile.BEFORE_START_LAUNCH_APP_PACKAGE, DefaultSettings.BEFORE_START_LAUNCH_APP_PACKAGE);

        this.VIDEO_RESOLUTION = spProfile.getInt(Constants.Sp.Profile.VIDEO_RESOLUTION, DefaultSettings.VIDEO_RESOLUTION);
        this.VIDEO_QUALITY = spProfile.getInt(Constants.Sp.Profile.VIDEO_QUALITY_bitRate, DefaultSettings.VIDEO_QUALITY_bitRate);
        this.VIDEO_FRAME_RATE = spProfile.getInt(Constants.Sp.Profile.VIDEO_FRAME_RATE, DefaultSettings.VIDEO_FRAME_RATE);

        this.AUDIO_SAMPLE_RATE = spProfile.getInt(Constants.Sp.Profile.AUDIO_SAMPLE_RATE, DefaultSettings.AUDIO_SAMPLE_RATE);
        this.AUDIO_QUALITY = spProfile.getInt(Constants.Sp.Profile.AUDIO_QUALITY_bitRate, DefaultSettings.AUDIO_QUALITY_bitRate);

        this.SAVE_LOCATION = KFile.getSavingLocation(ctx);

        final int[] wh = getSize(ctx, ctx.getResources().getConfiguration().orientation);

        this.VIDEO_WIDTH = wh[0];
        this.VIDEO_HEIGHT = wh[1];
    }

    private int[] getSize(Context ctx, int orientation) {
        if(orientation == Configuration.ORIENTATION_UNDEFINED) {
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

    public boolean isToRecordInternalAudioInStereo() {
        return IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO;
    }

    public int getInternalAudioChannelNumber() {
        return IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO ? 2 : 1;
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

    public int getAudioSampleRate() {
        return AUDIO_SAMPLE_RATE;
    }

    public int getAudioBitRate() {
        return AUDIO_QUALITY;
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

    public int getVideoBitRate() {
        return VIDEO_QUALITY;
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
}
