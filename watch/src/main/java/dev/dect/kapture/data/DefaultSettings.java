package dev.dect.kapture.data;

public class DefaultSettings {
    public static final boolean
        IS_TO_RECORD_MIC = true,
        IS_TO_RECORD_INTERNAL_AUDIO = false,
        IS_TO_RECORD_SOUND_IN_STEREO = true,
        IS_TO_DELAY_START_RECORDING = false,
        IS_TO_USE_TIME_LIMIT = false,
        IS_TO_STOP_ON_BATTERY_LEVEL = false,
        IS_TO_BEFORE_START_SET_MEDIA_VOLUME = false,
        IS_TO_BEFORE_START_LAUNCH_APP = false,
        WIFI_SHARE_IS_TO_SHOW_PASSWORD = true,
        WIFI_SHARE_IS_TO_REFRESH_PASSWORD = true;

    public static final int
        VIDEO_RESOLUTION = -1,
        VIDEO_QUALITY_bitRate = 6000000,
        VIDEO_FRAME_RATE = 30,
        SECONDS_TO_START_RECORDING = 3,
        SECONDS_TIME_LIMIT = 90,
        STOP_ON_BATTERY_LEVEL_LEVEL = 30,
        BEFORE_START_MEDIA_VOLUME_PERCENTAGE = 50,
        AUDIO_SAMPLE_RATE = 44100,
        AUDIO_QUALITY_bitRate = 96000;

    public static final String
        BEFORE_START_LAUNCH_APP_PACKAGE = "dev.dect.kapture";
}
