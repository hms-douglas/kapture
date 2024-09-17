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

/** @noinspection ResultOfMethodCallIgnored*/
public class KSettings {
    public static final int[] VIDEO_RESOLUTIONS = new int[]{-1, 1080, 720, 640, 540, 480, 360, 240},
                              VIDEO_QUALITIES = new int[]{16000000, 14000000, 12000000, 10000000, 8000000, 6000000, 4000000, 2000000, 1000000},
                              VIDEO_FRAME_RATES = new int[]{144, 120, 90, 60, 50, 40, 30, 25, 20, 15, 10};

    public static final float[] MICROPHONE_BOOST = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 20f, 25f};


    private final Context CONTEXT;

    private final boolean IS_TO_RECORD_MIC,
                          IS_TO_RECORD_INTERNAL_SOUND,
                          IS_TO_SHOW_FLOATING_BUTTON,
                          IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO,
                          IS_TO_BOOST_MIC_VOLUME,
                          IS_TO_GENERATE_MP3_AUDIO,
                          IS_TO_GENERATE_MP3_ONLY_INTERNAL,
                          IS_TO_GENERATE_MP3_ONLY_MIC,
                          IS_TO_GENERATE_MP4_NO_AUDIO,
                          IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO,
                          IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO;

    private final int VIDEO_RESOLUTION,
                      VIDEO_QUALITY,
                      VIDEO_FRAME_RATE,
                      VIDEO_WIDTH,
                      VIDEO_HEIGHT,
                      INTERNAL_AUDIO_SAMPLE_RATE,
                      MIC_AUDIO_SAMPLE_RATE;

    private final float MIC_BOOST_VOLUME_FACTOR;

    private final File SAVE_LOCATION;

    private final String CAPTURE_FILE_FORMAT,
                         AUDIO_FILE_FORMAT;

    public KSettings(Context ctx) {
        this.CONTEXT = ctx;

        final SharedPreferences sp = ctx.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE);

        this.IS_TO_RECORD_MIC = sp.getBoolean(Constants.SP_KEY_IS_TO_RECORD_MIC, DefaultSettings.IS_TO_RECORD_MIC);
        this.IS_TO_BOOST_MIC_VOLUME = sp.getBoolean(Constants.SP_KEY_IS_TO_BOOST_MIC_VOLUME, DefaultSettings.IS_TO_BOOST_MIC_VOLUME);
        this.MIC_AUDIO_SAMPLE_RATE = sp.getInt(Constants.SP_KEY_MIC_AUDIO_SAMPLE_RATE, DefaultSettings.MIC_AUDIO_SAMPLE_RATE);

        this.MIC_BOOST_VOLUME_FACTOR = sp.getFloat(Constants.SP_KEY_MIC_BOOST_VOLUME_FACTOR, DefaultSettings.MIC_BOOST_VOLUME_FACTOR);

        this.IS_TO_RECORD_INTERNAL_SOUND = sp.getBoolean(Constants.SP_KEY_IS_TO_RECORD_INTERNAL_AUDIO, DefaultSettings.IS_TO_RECORD_INTERNAL_AUDIO);
        this.IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO = sp.getBoolean(Constants.SP_KEY_IS_TO_RECORD_SOUND_IN_STEREO, DefaultSettings.IS_TO_RECORD_SOUND_IN_STEREO);
        this.INTERNAL_AUDIO_SAMPLE_RATE = sp.getInt(Constants.SP_KEY_INTERNAL_AUDIO_SAMPLE_RATE, DefaultSettings.INTERNAL_AUDIO_SAMPLE_RATE);

        this.IS_TO_SHOW_FLOATING_BUTTON = sp.getBoolean(Constants.SP_KEY_IS_TO_SHOW_FLOATING_BUTTON, DefaultSettings.IS_TO_SHOW_FLOATING_BUTTON);

        this.VIDEO_RESOLUTION = sp.getInt(Constants.SP_KEY_VIDEO_RESOLUTION, DefaultSettings.VIDEO_RESOLUTION);
        this.VIDEO_QUALITY = sp.getInt(Constants.SP_KEY_VIDEO_QUALITY_bitRate, DefaultSettings.VIDEO_QUALITY_bitRate);
        this.VIDEO_FRAME_RATE = sp.getInt(Constants.SP_KEY_VIDEO_FRAME_RATE, DefaultSettings.VIDEO_FRAME_RATE);

        this.IS_TO_GENERATE_MP3_AUDIO = sp.getBoolean(Constants.SP_KEY_IS_TO_GENERATE_MP3_AUDIO, DefaultSettings.IS_TO_GENERATE_MP3_AUDIO);
        this.IS_TO_GENERATE_MP3_ONLY_INTERNAL = sp.getBoolean(Constants.SP_KEY_IS_TO_GENERATE_MP3_ONLY_INTERNAL, DefaultSettings.IS_TO_GENERATE_MP3_ONLY_INTERNAL);
        this.IS_TO_GENERATE_MP3_ONLY_MIC = sp.getBoolean(Constants.SP_KEY_IS_TO_GENERATE_MP3_ONLY_MIC, DefaultSettings.IS_TO_GENERATE_MP3_ONLY_MIC);

        this.IS_TO_GENERATE_MP4_NO_AUDIO = sp.getBoolean(Constants.SP_KEY_IS_TO_GENERATE_MP4_NO_AUDIO, DefaultSettings.IS_TO_GENERATE_MP4_NO_AUDIO);
        this.IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO = sp.getBoolean(Constants.SP_KEY_IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO, DefaultSettings.IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO);
        this.IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO = sp.getBoolean(Constants.SP_KEY_IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO, DefaultSettings.IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO);

        this.SAVE_LOCATION = new File(sp.getString(Constants.SP_KEY_FILE_SAVING_PATH, KFile.getDefaultFileLocation(ctx).getAbsolutePath()));

        this.CAPTURE_FILE_FORMAT = sp.getString(Constants.SP_KEY_VIDEO_FILE_FORMAT, DefaultSettings.CAPTURE_FILE_FORMAT);
        this.AUDIO_FILE_FORMAT = sp.getString(Constants.SP_KEY_AUDIO_FILE_FORMAT, DefaultSettings.AUDIO_FILE_FORMAT);

        final int[] wh = getSize(ctx);

        this.VIDEO_WIDTH = wh[0];
        this.VIDEO_HEIGHT = wh[1];
    }

    private int[] getSize(Context ctx) {
        final Rect rect = ctx.getSystemService(WindowManager.class).getCurrentWindowMetrics().getBounds();

        if(this.VIDEO_RESOLUTION == -1) {
            return new int[]{rect.width(), rect.height()};
        }

        if(ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            final int percentage = (this.VIDEO_RESOLUTION * 100) / rect.width();

            return new int[]{this.VIDEO_RESOLUTION, (rect.height() * percentage) / 100};
        }

        final int percentage = (this.VIDEO_RESOLUTION * 100) / rect.height();

        return new int[]{(rect.width() * percentage) / 100, this.VIDEO_RESOLUTION};
    }

    public boolean isToRecordMic() {
        return IS_TO_RECORD_MIC;
    }

    public boolean isToRecordInternalAudio() {
        return IS_TO_RECORD_INTERNAL_SOUND;
    }

    public boolean isToShowFloatingButton() {
        return IS_TO_SHOW_FLOATING_BUTTON;
    }

    public boolean isToRecordInternalAudioInStereo() {
        return IS_TO_RECORD_INTERNAL_SOUND_IN_STEREO;
    }

    public boolean isToBoostMicVolume() {
        return IS_TO_BOOST_MIC_VOLUME;
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

    public int getInternalAudioSampleRate() {
        return INTERNAL_AUDIO_SAMPLE_RATE;
    }

    public float getMicBoostVolumeFactor() {
        return MIC_BOOST_VOLUME_FACTOR;
    }

    public int getMicAudioSampleRate() {
        return MIC_AUDIO_SAMPLE_RATE;
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

    public int getMicAudioBitRate() {
        return 16 * getMicAudioSampleRate();
    }

    public int getVideoBitRate() {
        return VIDEO_QUALITY;
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
