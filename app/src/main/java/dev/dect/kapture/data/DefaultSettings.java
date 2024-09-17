package dev.dect.kapture.data;

import android.content.res.Configuration;

import dev.dect.kapture.fragment.KapturesFragment;
import dev.dect.kapture.popup.SortPopup;

public class DefaultSettings {
    public static final boolean
        IS_TO_RECORD_MIC = true,
        IS_TO_RECORD_INTERNAL_AUDIO = true,
        IS_TO_SHOW_FLOATING_BUTTON = false,
        IS_TO_RECORD_SOUND_IN_STEREO = true,
        IS_TO_BOOST_MIC_VOLUME = true,
        IS_TO_GENERATE_MP3_AUDIO = false,
        IS_TO_GENERATE_MP3_ONLY_INTERNAL = false,
        IS_TO_GENERATE_MP3_ONLY_MIC = false,
        IS_TO_GENERATE_MP4_NO_AUDIO = false,
        IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO = false,
        IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO = false,
        SORT_BY_ASC = false;

    public static final int
        VIDEO_RESOLUTION = -1,
        VIDEO_QUALITY_bitRate = 6000000,
        VIDEO_FRAME_RATE = 30,
        INTERNAL_AUDIO_SAMPLE_RATE = 44100,
        MIC_AUDIO_SAMPLE_RATE = 44100,
        LAYOUT_MANAGER_STYLE = KapturesFragment.STYLE_LIST,
        SORT_BY = SortPopup.SORT_DATE_CAPTURING;

    public static final float
        MIC_BOOST_VOLUME_FACTOR = 5f;

    public static final String
        CAPTURE_FILE_FORMAT = "mp4",
        AUDIO_FILE_FORMAT = "mp3",
        LANGUAGE = Constants.LANGUAGES[0];
}
