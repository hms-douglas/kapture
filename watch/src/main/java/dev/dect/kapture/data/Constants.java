package dev.dect.kapture.data;

public class Constants {
    public static final String
        HOME_PACKAGE_NAME = "_home",
        EXT_VIDEO_FORMAT = "mp4",
        EXT_AUDIO_FORMAT = "wav",
        NO_PROFILE = "no_profile";

    public static final int
        BEZEL_SCROLL_BY = 90;

    public static class Sp {
       public static final String
                CLONED = "is_cloned";

        public static class App {
            public static final String
                SP = "kapture",
                LAST_FILE_ID = "k0",
                FILE_SAVING_PATH = "k7",
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
                PROFILE_NAMES = "k87";
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
                VIDEO_RESOLUTION = "k4",
                VIDEO_FRAME_RATE = "k5",
                IS_TO_RECORD_SOUND_IN_STEREO = "k10",
                VIDEO_QUALITY_bitRate = "k29",
                SECONDS_TO_START_RECORDING = "k39",
                IS_TO_DELAY_START_RECORDING = "k40",
                IS_TO_USE_TIME_LIMIT = "k41",
                SECONDS_TIME_LIMIT = "k42",
                IS_TO_STOP_ON_BATTERY_LEVEL = "k74",
                STOP_ON_BATTERY_LEVEL_LEVEL = "k75",
                IS_TO_BEFORE_START_SET_MEDIA_VOLUME = "k88",
                BEFORE_START_MEDIA_VOLUME_PERCENTAGE = "k89",
                IS_TO_BEFORE_START_LAUNCH_APP = "k90",
                BEFORE_START_LAUNCH_APP_PACKAGE = "k91",
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

    public static class Tile {
        public static class Action {
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
                                       MIT = "https://mit-license.org",
                                       BSD3_CLAUSE = "https://opensource.org/license/bsd-3-clause";
        }

        public static class App {
            public static final String
                REPOSITORY = "https://github.com/hms-douglas/kapture",
                LATEST_VERSION_FILE_WATCH = REPOSITORY + "/raw/master/dist/latest_watch.json";

            public static class KeyTag {
                public static final String
                    LATEST_VERSION_VERSION_CODE = "versionCode",
                    LATEST_VERSION_VERSION_NAME = "versionName",
                    LATEST_VERSION_LINK = "link";
            }
        }
    }

    public static class Notification {
        public static class Id {
            public static final int
                CAPTURING = -3;
        }

        public static class Channel {
            public static final String
                CAPTURING = "channel1";
        }
    }

    public static class DataKey {
        public static final String ACTION = "ACTION",
                                   FILE_ASSET = "F_ASSET",
                                   FILE_NAME = "F_NAME",
                                   FILE_AMOUNT = "F_AMOUNT",
                                   TIMESTAMP = "TIMESTAMP",
                                   DATA_PATH = "/kapture" ;

        public static class Action {
            public static final String FILE = "FILE",
                                       INFORM_RECEIVED_SUCCESS = "I_R_SUCCESS",
                                       INFORM_RECEIVED_ERROR = "I_R_ERROR",
                                       INFORM_IS_SENDING_FILEs = "I_SENDING";
        }
    }
}
