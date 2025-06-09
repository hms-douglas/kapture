package dev.dect.kapture.data;

public class Constants {
    public static final String
        HOME_PACKAGE_NAME = "_home",
        EXT_VIDEO_FORMAT = "mp4",
        EXT_AUDIO_FORMAT = "wav",
        NO_PROFILE = "no_profile";

    public static final int
        OVERLAY_MAX_SHORTCUTS = 6;

    public static class Sp {
       public static final String
                CLONED = "is_cloned";

        public static class App {
            public static final String
                SP = "kapture",
                LAST_FILE_ID = "k0",
                FILE_SAVING_PATH = "k7",
                PERMISSION_STEPS = "k23",
                SORT_BY = "k24",
                SORT_BY_ASC = "k25",
                LAYOUT_MANAGER_STYLE = "k26",
                APP_THEME = "k28",
                IS_TO_SHOW_NOTIFICATION_PROCESSING = "k36",
                IS_TO_SHOW_NOTIFICATION_CAPTURED = "k37",
                SCREENSHOT_FILE_SAVING_PATH = "k62",
                IS_TO_RECYCLE_TOKEN = "k83",
                WIFI_SHARE_IS_TO_SHOW_PASSWORD = "k84",
                WIFI_SHARE_IS_TO_REFRESH_PASSWORD = "k85",
                ACTIVE_PROFILE_NAME = "k86",
                PROFILE_NAMES = "k87",
                IS_TO_SHOW_NOTIFICATION_WIFI_SHARE = "k88";
        }

        public static class Profile {
            public static final String
                SP_FILE_NAME_DEFAULT = "kdefault",
                SP_FILE_NAME = "profile_name",
                SP_FILE_USER_NAME = "file_user_name",
                SP_FILE_ICON = "file_icon",
                SP_FILE_ICON_COLOR = "file_icon_color",
                IS_TO_RECORD_MIC = "k1",
                IS_TO_RECORD_INTERNAL_AUDIO = "k2",
                IS_TO_SHOW_FLOATING_MENU = "k3",
                VIDEO_RESOLUTION = "k4",
                VIDEO_FRAME_RATE = "k5",
                OVERLAY_MENU_X_POS = "k8",
                OVERLAY_MENU_Y_POS = "k9",
                IS_TO_RECORD_SOUND_IN_STEREO = "k10",
                IS_TO_GENERATE_AUDIO_AUDIO = "k16",
                IS_TO_GENERATE_AUDIO_ONLY_INTERNAL = "k17",
                IS_TO_GENERATE_AUDIO_ONLY_MIC = "k18",
                IS_TO_GENERATE_VIDEO_NO_AUDIO = "k20",
                IS_TO_GENERATE_VIDEO_ONLY_INTERNAL_AUDIO = "k21",
                IS_TO_GENERATE_VIDEO_ONLY_MIC_AUDIO = "k22",
                VIDEO_QUALITY_bitRate = "k29",
                IS_TO_SHOW_FLOATING_CAMERA = "k30",
                OVERLAY_CAMERA_X_POS = "k31",
                OVERLAY_CAMERA_Y_POS = "k32",
                CAMERA_SIZE = "k33",
                CAMERA_FACING_LENS = "k34",
                CAMERA_SHAPE = "k35",
                IS_TO_TOGGLE_CAMERA_ORIENTATION = "k38",
                SECONDS_TO_START_RECORDING = "k39",
                IS_TO_DELAY_START_RECORDING = "k40",
                IS_TO_USE_TIME_LIMIT = "k41",
                SECONDS_TIME_LIMIT = "k42",
                IS_TO_STOP_ON_SCREEN_OFF = "k43",
                IS_TO_STOP_ON_SHAKE = "k44",
                IS_CAMERA_SCALABLE = "k45",
                IS_TO_SHOW_TEXT = "k46",
                TEXT_TEXT = "k47",
                TEXT_COLOR = "k48",
                TEXT_BACKGROUND = "k49",
                TEXT_SIZE = "k50",
                TEXT_FONT_PATH = "k51",
                TEXT_ALIGNMENT = "k52",
                OVERLAY_TEXT_X_POS = "k53",
                OVERLAY_TEXT_Y_POS = "k54",
                IS_TO_SHOW_TIME_ON_MENU = "k55",
                IS_TO_SHOW_TIME_LIMIT_ON_MENU = "k56",
                IS_TO_SHOW_CLOSE_BUTTON_ON_MENU = "k57",
                IS_TO_SHOW_MINIMIZE_BUTTON_ON_MENU = "k58",
                IS_TO_SHOW_CAMERA_BUTTON_ON_MENU = "k59",
                IS_TO_SHOW_DRAW_BUTTON_ON_MENU = "k60",
                IS_TO_SHOW_SCREENSHOT_BUTTON_ON_MENU = "k61",
                IS_TO_START_MENU_MINIMIZED = "k63",
                MINIMIZING_SIDE = "k64",
                MENU_STYLE = "k65",
                OVERLAY_DRAW_X_POS = "k66",
                OVERLAY_DRAW_Y_POS = "k67",
                IS_TO_SHOW_UNDO_REDO_BUTTON_ON_DRAW_MENU = "k68",
                IS_TO_SHOW_CLEAR_BUTTON_ON_DRAW_MENU = "k68",
                DRAW_PEN_SIZE = "k69",
                DRAW_PEN_COLOR = "k70",
                DRAW_PEN_PREVIOUS_COLORS = "k71",
                VIDEO_ORIENTATION = "k72",
                IS_TO_SHOW_PAUSE_RESUME_BUTTON_ON_MENU = "k73",
                IS_TO_STOP_ON_BATTERY_LEVEL = "k74",
                STOP_ON_BATTERY_LEVEL_LEVEL = "k75",
                IS_TO_SHOW_SCREENSHOT_BUTTON_ON_DRAW_MENU = "k76",
                IS_TO_SHOW_DRAW_SCREENSHOT_BUTTON_ON_DRAW_MENU = "k77",
                IS_TO_SHOW_IMAGE = "k78",
                IMAGE_PATH = "k79",
                IMAGE_SIZE = "k80",
                OVERLAY_IMAGE_X_POS = "k81",
                OVERLAY_IMAGE_Y_POS = "k82",
                IS_TO_BEFORE_START_URL = "k86",
                BEFORE_START_URL = "k87",
                IS_TO_BEFORE_START_SET_MEDIA_VOLUME = "k88",
                BEFORE_START_MEDIA_VOLUME_PERCENTAGE = "k89",
                IS_TO_BEFORE_START_LAUNCH_APP = "k90",
                BEFORE_START_LAUNCH_APP_PACKAGE = "k91",
                IS_TO_SHOW_SHORTCUTS_BUTTON_ON_MENU = "k92",
                SHORTCUTS_BUTTON_ON_MENU = "k93",
                IS_TO_OPEN_SHORTCUTS_ON_POPUP = "k94",
                AUDIO_SAMPLE_RATE = "k95",
                AUDIO_QUALITY_bitRate = "k96";
        }
    }

    private static class Action {
        public static final String
            START = "ACTION.START",
            STOP = "ACTION.STOP",
            PAUSE = "ACTION.PAUSE",
            RESUME = "ACTION.RESUME",
            SHARE = "ACTION.SHARE",
            DELETE = "ACTION.DELETE",
            EXTRA = "ACTION.EXTRA",
            WIFI_SHARE = "ACTION.WIFI_SHARE",
            UPDATE = "ACTION.UPDATE",
            SET_PROFILE = "ACTION.SET_PROFILE";
    }

    public static class Widget {
        public static class Action {
            public static final String
                START = Constants.Action.START,
                STOP = Constants.Action.STOP,
                PAUSE = Constants.Action.PAUSE,
                RESUME = Constants.Action.RESUME,
                WIFI_SHARE = Constants.Action.WIFI_SHARE,
                SET_PROFILE = Constants.Action.SET_PROFILE;
        }
    }

    public static class QuickTile {
        public static class Action {
            public static final String
                WIFI_SHARE = Constants.Action.WIFI_SHARE,
                UPDATE = Constants.Action.UPDATE;
        }
    }

    public static class Shortcut {
        public static class Action { // also change on shortcuts.xml
            public static final String
                START = Constants.Action.START,
                STOP = Constants.Action.STOP,
                PAUSE = Constants.Action.PAUSE,
                RESUME = Constants.Action.RESUME;
        }
    }

    public static class Url {
        public static class License {
            public static final String APACHE_V2 = "https://www.apache.org/licenses/LICENSE-2.0.txt",
                LOTTIES = "https://lottiefiles.com/page/license",
                MIT = "https://mit-license.org",
                BSD3_CLAUSE = "https://opensource.org/license/bsd-3-clause",
                OPEN_FONT = "https://openfontlicense.org/";
        }

        public static class App {
            public static final String
                REPOSITORY = "https://github.com/hms-douglas/kapture",
                LATEST_VERSION_FILE = REPOSITORY + "/raw/master/dist/latest.json";

            public static class KeyTag {
                public static final String
                    LATEST_VERSION_VERSION_CODE = "versionCode",
                    LATEST_VERSION_VERSION_NAME = "versionName",
                    LATEST_VERSION_LINK = "link";
            }
        }
    }

    public static class Notification {
        public static class Action {
            public static final String
                STOP = Constants.Action.STOP,
                PAUSE = Constants.Action.PAUSE,
                RESUME = Constants.Action.RESUME,
                SHARE = Constants.Action.SHARE,
                DELETE = Constants.Action.DELETE,
                EXTRA = Constants.Action.EXTRA;
        }

        public static class Id {
            public static final int
                WIFI_SHARE = -1,
                PROCESSING = -2,
                CAPTURING = -3,
                RECEIVING = -4;
        }

        public static class Channel {
            public static final String
                CAPTURING = "channel1",
                CAPTURED = "channel2",
                PROCESSING = "channel3",
                WIFI_SHARE = "channel4",
                RECEIVING = "channel5";
        }
    }

    public static class DataKey {
        public static final String ACTION = "ACTION",
                                   FILE_ASSET = "F_ASSET",
                                   FILE_NAME = "F_NAME",
                                   FILE_AMOUNT = "F_AMOUNT",
                                   TIMESTAMP = "TIMESTAMP",
                                   DATA_PATH = "/kapture";

        public static class Action {
            public static final String FILE = "FILE",
                                       INFORM_RECEIVED_SUCCESS = "I_R_SUCCESS",
                                       INFORM_RECEIVED_ERROR = "I_R_ERROR",
                                       INFORM_IS_SENDING_FILEs = "I_SENDING";
        }
    }
}
