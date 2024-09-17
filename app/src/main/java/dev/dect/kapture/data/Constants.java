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
        SP = "kapture",
        SP_KEY_LAST_FILE_ID = "k0",
        SP_KEY_IS_TO_RECORD_MIC = "k1",
        SP_KEY_IS_TO_RECORD_INTERNAL_AUDIO = "k2",
        SP_KEY_IS_TO_SHOW_FLOATING_BUTTON = "k3",
        SP_KEY_VIDEO_RESOLUTION = "k4",
        SP_KEY_VIDEO_QUALITY_bitRate = "k29",
        SP_KEY_VIDEO_FRAME_RATE = "k5",
        SP_KEY_FILE_SAVING_PATH = "k7",
        SP_KEY_OVERLAY_X_POS = "k8",
        SP_KEY_OVERLAY_Y_POS = "k9",
        SP_KEY_IS_TO_RECORD_SOUND_IN_STEREO = "k10",
        SP_KEY_INTERNAL_AUDIO_SAMPLE_RATE = "k11",
        SP_KEY_MIC_AUDIO_SAMPLE_RATE = "k12",
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
        //29 in use
        NOTIFICATION_CHANNEL_ID_CAPTURING = "channel1",
        NOTIFICATION_CHANNEL_ID_CAPTURED = "channel2",
        NOTIFICATION_CHANNEL_ID_PROCESSING = "channel3",
        NOTIFICATION_CAPTURING_ACTION_STOP = "ACTION.STOP",
        NOTIFICATION_CAPTURED_ACTION_SHARE = "ACTION.SHARE",
        NOTIFICATION_CAPTURED_ACTION_DELETE = "ACTION.DELETE",
        NOTIFICATION_CAPTURED_ACTION_EXTRA = "ACTION.EXTRA",
        QUICK_SETTINGS_TILE_ACTION_UPDATE = "ACTION.UPDATE";

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
