package dev.dect.kapture.data;

import android.content.Context;

import java.util.ArrayList;

import dev.dect.kapture.R;

public class Constants {
    public static final String
        GITHUB_REPOSITORY_URL = "https://github.com/hms-douglas/kapture",
        GITHUB_PROJECT_LATEST_VERSION_URL = GITHUB_REPOSITORY_URL + "/raw/master/dist/latest.json",
        GITHUB_PROJECT_LATEST_VERSION_TAG_VERSION_CODE = "versionCode",
        GITHUB_PROJECT_LATEST_VERSION_TAG_VERSION_NAME = "versionName",
        GITHUB_PROJECT_LATEST_VERSION_TAG_LINK = "link",
        LICENSE_URL_APACHE_V2 = "https://www.apache.org/licenses/LICENSE-2.0.txt",
        LICENSE_URL_LGPL_V3 = "https://www.gnu.org/licenses/lgpl-3.0.html#license-text",
        LICENSE_URL_LOTTIES = "https://lottiefiles.com/page/license",
        LICENSE_URL_MIT = "https://mit-license.org",
        LICENSE_URL_BSD3_CLAUSE = "https://opensource.org/license/bsd-3-clause",
        LICENSE_URL_OPEN_FONT = "https://openfontlicense.org/",
        SP = "kapture",
        SP_KEY_LAST_FILE_ID = "k0",
        SP_KEY_IS_TO_RECORD_MIC = "k1",
        SP_KEY_IS_TO_RECORD_INTERNAL_AUDIO = "k2",
        SP_KEY_IS_TO_SHOW_FLOATING_MENU = "k3",
        SP_KEY_VIDEO_RESOLUTION = "k4",
        SP_KEY_VIDEO_FRAME_RATE = "k5",
        SP_KEY_FILE_SAVING_PATH = "k7",
        SP_KEY_OVERLAY_MENU_X_POS = "k8",
        SP_KEY_OVERLAY_MENU_Y_POS = "k9",
        SP_KEY_IS_TO_RECORD_SOUND_IN_STEREO = "k10",
        //k11 removed | never used
        //k12 removed | never used
        SP_KEY_VIDEO_FILE_FORMAT = "k13",
        SP_KEY_IS_TO_BOOST_MIC_VOLUME = "k14",
        SP_KEY_MIC_BOOST_VOLUME_FACTOR = "k15",
        SP_KEY_IS_TO_GENERATE_MP3_AUDIO = "k16",
        SP_KEY_IS_TO_GENERATE_MP3_ONLY_INTERNAL = "k17",
        SP_KEY_IS_TO_GENERATE_MP3_ONLY_MIC = "k18",
        SP_KEY_AUDIO_FILE_FORMAT = "k19",
        SP_KEY_IS_TO_GENERATE_MP4_NO_AUDIO = "k20",
        SP_KEY_IS_TO_GENERATE_MP4_ONLY_INTERNAL_AUDIO = "k21",
        SP_KEY_IS_TO_GENERATE_MP4_ONLY_MIC_AUDIO = "k22",
        SP_KEY_PERMISSION_STEPS = "k23",
        SP_KEY_SORT_BY = "k24",
        SP_KEY_SORT_BY_ASC = "k25",
        SP_KEY_LAYOUT_MANAGER_STYLE = "k26",
        SP_KEY_APP_LANGUAGE = "k27",
        SP_KEY_APP_THEME = "k28",
        SP_KEY_VIDEO_QUALITY_bitRate = "k29",
        SP_KEY_IS_TO_SHOW_FLOATING_CAMERA = "k30",
        SP_KEY_OVERLAY_CAMERA_X_POS = "k31",
        SP_KEY_OVERLAY_CAMERA_Y_POS = "k32",
        SP_KEY_CAMERA_SIZE = "k33",
        SP_KEY_CAMERA_FACING_LENS = "k34",
        SP_KEY_CAMERA_SHAPE = "k35",
        SP_KEY_IS_TO_SHOW_NOTIFICATION_PROCESSING = "k36",
        SP_KEY_IS_TO_SHOW_NOTIFICATION_CAPTURED = "k37",
        SP_KEY_IS_TO_TOGGLE_CAMERA_ORIENTATION = "k38",
        SP_KEY_SECONDS_TO_START_RECORDING = "k39",
        SP_KEY_IS_TO_DELAY_START_RECORDING = "k40",
        SP_KEY_IS_TO_USE_TIME_LIMIT = "k41",
        SP_KEY_SECONDS_TIME_LIMIT = "k42",
        SP_KEY_IS_TO_STOP_ON_SCREEN_OFF = "k43",
        SP_KEY_IS_TO_STOP_ON_SHAKE = "k44",
        SP_KEY_IS_CAMERA_SCALABLE = "k45",
        SP_KEY_IS_TO_SHOW_TEXT = "k46",
        SP_KEY_TEXT_TEXT = "k47",
        SP_KEY_TEXT_COLOR = "k48",
        SP_KEY_TEXT_BACKGROUND = "k49",
        SP_KEY_TEXT_SIZE = "k50",
        SP_KEY_TEXT_FONT_PATH = "k51",
        SP_KEY_TEXT_ALIGNMENT = "k52",
        SP_KEY_OVERLAY_TEXT_X_POS = "k53",
        SP_KEY_OVERLAY_TEXT_Y_POS = "k54",
        SP_KEY_IS_TO_SHOW_TIME_ON_MENU = "k55",
        SP_KEY_IS_TO_SHOW_TIME_LIMIT_ON_MENU = "k56",
        SP_KEY_IS_TO_SHOW_CLOSE_BUTTON_ON_MENU = "k57",
        SP_KEY_IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU = "k58",
        SP_KEY_IS_TO_SHOW_CAMERA_BUTTON_ON_MENU = "k59",
        SP_KEY_IS_TO_SHOW_DRAW_BUTTON_ON_MENU = "k60",
        SP_KEY_IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU = "k61",
        SP_KEY_SCREENSHOT_FILE_SAVING_PATH = "k62",
        SP_KEY_IS_TO_START_MENU_MINIMIZED = "k63",
        SP_KEY_MINIMIZING_SIDE = "k64",
        SP_KEY_MENU_STYLE = "k65",
        SP_KEY_OVERLAY_DRAW_X_POS = "k66",
        SP_KEY_OVERLAY_DRAW_Y_POS = "k67",
        SP_KEY_IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU = "k68",
        SP_KEY_IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU = "k68",
        SP_KEY_DRAW_PEN_SIZE = "k69",
        SP_KEY_DRAW_PEN_COLOR = "k70",
        SP_KEY_DRAW_PEN_PREVIOUS_COLORS = "k71",
        SP_KEY_VIDEO_ORIENTATION = "k72",
        SP_KEY_IS_TO_SHOW_PAUSE_RESUME_BUTTON_ON_MENU = "k73",
        NOTIFICATION_CHANNEL_ID_CAPTURING = "channel1",
        NOTIFICATION_CHANNEL_ID_CAPTURED = "channel2",
        NOTIFICATION_CHANNEL_ID_PROCESSING = "channel3",
        NOTIFICATION_CAPTURING_ACTION_STOP = "ACTION.STOP",
        NOTIFICATION_CAPTURING_ACTION_PAUSE = "ACTION.PAUSE",
        NOTIFICATION_CAPTURING_ACTION_RESUME = "ACTION.RESUME",
        NOTIFICATION_CAPTURED_ACTION_SHARE = "ACTION.SHARE",
        NOTIFICATION_CAPTURED_ACTION_DELETE = "ACTION.DELETE",
        NOTIFICATION_CAPTURED_ACTION_EXTRA = "ACTION.EXTRA",
        QUICK_SETTINGS_TILE_ACTION_UPDATE = "ACTION.UPDATE",
        SHORTCUT_STATIC_ACTION_START = "ACTION.START",
        SHORTCUT_STATIC_ACTION_STOP = NOTIFICATION_CAPTURING_ACTION_STOP,
        SHORTCUT_STATIC_ACTION_PAUSE = NOTIFICATION_CAPTURING_ACTION_PAUSE,
        SHORTCUT_STATIC_ACTION_RESUME = NOTIFICATION_CAPTURING_ACTION_RESUME;

    public static final String[] LANGUAGES = new String[]{"auto", "en-US", "pt-BR"};

    public static String[] getLanguagesNames(Context ctx) {
        final ArrayList<String> names = new ArrayList<>();

        for(String l : LANGUAGES) {
            switch (l) {
                case "auto":
                    names.add(ctx.getString(R.string.lang_auto));
                    break;

                case "en-US":
                    names.add(ctx.getString(R.string.lang_en_us));
                    break;

                case "pt-BR":
                    names.add(ctx.getString(R.string.lang_pt_br));
                    break;
            }
        }

        return names.toArray(new String[0]);
    }
}
