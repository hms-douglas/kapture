package dev.dect.kapture.data;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.camera2.CameraCharacteristics;
import android.view.Gravity;

import dev.dect.kapture.fragment.KapturesFragment;
import dev.dect.kapture.popup.SortPopup;

public class DefaultSettings {
    public static final boolean
        IS_TO_RECORD_MIC = true,
        IS_TO_RECORD_INTERNAL_AUDIO = true,
        IS_TO_SHOW_FLOATING_MENU = false,
        IS_TO_SHOW_FLOATING_CAMERA = false,
        IS_TO_RECORD_SOUND_IN_STEREO = true,
        IS_TO_BOOST_MIC_VOLUME = true,
        IS_TO_GENERATE_MP3_AUDIO = false,
        IS_TO_GENERATE_MP3_ONLY_INTERNAL = false,
        IS_TO_GENERATE_MP3_ONLY_MIC = false,
        IS_TO_GENERATE_MP4_NO_AUDIO = false,
        IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO = false,
        IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO = false,
        SORT_BY_ASC = false,
        IS_TO_SHOW_NOTIFICATION_PROCESSING = true,
        IS_TO_SHOW_NOTIFICATION_CAPTURED = true,
        IS_TO_TOGGLE_CAMERA_ORIENTATION = false,
        IS_TO_DELAY_START_RECORDING = false,
        IS_TO_USE_TIME_LIMIT = false,
        IS_TO_STOP_ON_SCREEN_OFF = false,
        IS_TO_STOP_ON_SHAKE = false,
        IS_CAMERA_SCALABLE = false,
        IS_TO_SHOW_TEXT = false,
        IS_TO_SHOW_TIME_ON_MENU = true,
        IS_TO_SHOW_TIME_LIMIT_ON_MENU = false,
        IS_TO_SHOW_CLOSE_BUTTON_ON_MENU = false,
        IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU = true,
        IS_TO_SHOW_CAMERA_BUTTON_ON_MENU = true,
        IS_TO_SHOW_DRAW_BUTTON_ON_MENU = true,
        IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU = false,
        IS_TO_START_MENU_MINIMIZED = false,
        IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU = true,
        IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU = true;

    public static final int
        VIDEO_RESOLUTION = -1,
        VIDEO_QUALITY_bitRate = 6000000,
        VIDEO_FRAME_RATE = 30,
        VIDEO_ORIENTATION = Configuration.ORIENTATION_UNDEFINED,
        INTERNAL_AUDIO_SAMPLE_RATE = 44100,
        MIC_AUDIO_SAMPLE_RATE = 44100,
        LAYOUT_MANAGER_STYLE = KapturesFragment.STYLE_LIST,
        SORT_BY = SortPopup.SORT_DATE_CAPTURING,
        CAMERA_SIZE = 80,
        CAMERA_FACING_LENS = CameraCharacteristics.LENS_FACING_FRONT,
        CAMERA_SHAPE = KSettings.CAMERA_SHAPES[0],
        SECONDS_TO_START_RECORDING = 3,
        SECONDS_TIME_LIMIT = 90,
        TEXT_SIZE = 24,
        TEXT_ALIGNMENT = Gravity.START,
        MINIMIZING_SIDE = KSettings.MINIMIZE_SIDES[0],
        MENU_STYLE = KSettings.MENU_STYLES[0];

    public static final float
        MIC_BOOST_VOLUME_FACTOR = 5f;

    public static final String
        CAPTURE_FILE_FORMAT = "mp4",
        AUDIO_FILE_FORMAT = "mp3",
        LANGUAGE = Constants.LANGUAGES[0],
        TEXT_FONT_PATH = KSettings.INTERNAL_FONTS_PATHS[0],
        TEXT_COLOR = "#EB3B2EFF",
        TEXT_BACKGROUND = "#00000000",
        PEN_COLOR = "#EB3B2EFF",
        PEN_PREVIOUS_COLORS = "#EB3B2EFF,#05AC08FF,#FFFFFFFF,#0478FFFF,#FFD016FF";
}
