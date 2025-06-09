package dev.dect.kapture.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KProfile;


@SuppressLint("ApplySharedPref")
public class KSharedPreferences {
    private static SharedPreferences SP_ACTIVE_PROFILE = null;

    public static SharedPreferences getActiveProfileSp(Context ctx) {
        if(SP_ACTIVE_PROFILE == null) {
            fixFilesIfOldVersion(ctx);

            SP_ACTIVE_PROFILE = ctx.getSharedPreferences(getAppSp(ctx).getString(Constants.Sp.App.ACTIVE_PROFILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT), Context.MODE_PRIVATE);
        }

        return SP_ACTIVE_PROFILE;
    }

    public static SharedPreferences getAppSp(Context ctx) {
        fixFilesIfOldVersion(ctx);

        return ctx.getSharedPreferences(Constants.Sp.App.SP, Context.MODE_PRIVATE);
    }

    //If user is updating from versionCode <= 4
    private static void fixFilesIfOldVersion(Context ctx) {
        if(!exists(ctx, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT) && exists(ctx, Constants.Sp.App.SP)) {
            clone(ctx, Constants.Sp.App.SP, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT);

            final List<String> appKeys = Arrays.asList(
                Constants.Sp.App.LAST_FILE_ID,
                Constants.Sp.App.FILE_SAVING_PATH,
                Constants.Sp.App.SORT_BY,
                Constants.Sp.App.SORT_BY_ASC,
                Constants.Sp.App.LAYOUT_MANAGER_STYLE,
                Constants.Sp.App.APP_THEME,
                Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_PROCESSING,
                Constants.Sp.App.IS_TO_SHOW_NOTIFICATION_CAPTURED,
                Constants.Sp.App.SCREENSHOT_FILE_SAVING_PATH,
                Constants.Sp.App.IS_TO_RECYCLE_TOKEN,
                Constants.Sp.App.WIFI_SHARE_IS_TO_SHOW_PASSWORD,
                Constants.Sp.App.WIFI_SHARE_IS_TO_REFRESH_PASSWORD,
                Constants.Sp.App.ACTIVE_PROFILE_NAME
            );

            remove(ctx, Constants.Sp.App.SP, appKeys, true);
            remove(ctx, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT, appKeys, false);
        }
    }

    public static boolean exists(Context ctx, String name) {
        return !ctx.getSharedPreferences(name, Context.MODE_PRIVATE).getAll().isEmpty();
    }

    private static SharedPreferences clone(Context ctx, String original, String cloned) {
        return clone(ctx, ctx.getSharedPreferences(original, Context.MODE_PRIVATE), cloned);
    }

    private static SharedPreferences clone(Context ctx, SharedPreferences spOriginal, String cloned) {
        final SharedPreferences spCloned = ctx.getSharedPreferences(cloned, Context.MODE_PRIVATE);

        final SharedPreferences.Editor spClonedEditor = spCloned.edit();

        for(Map.Entry<String, ?> entry : spOriginal.getAll().entrySet()) {
            final Object value = entry.getValue();
            final String key = entry.getKey();

            if(value instanceof Boolean) {
                spClonedEditor.putBoolean(key, (Boolean) value);
            } else if(value instanceof Integer) {
                spClonedEditor.putInt(key, (Integer) value);
            } else if(value instanceof String) {
                spClonedEditor.putString(key, ((String) value));
            } else if (value instanceof Float) {
                spClonedEditor.putFloat(key, (Float) value);
            } else if (value instanceof Long) {
                spClonedEditor.putLong(key, (Long) value);
            } else if (value instanceof Set) {
                spClonedEditor.putStringSet(key, (Set<String>) value);
            }
        }

        spClonedEditor.putBoolean(Constants.Sp.CLONED, true);

        spClonedEditor.commit();

        return spCloned;
    }

    private static void remove(Context ctx, String name, List<String> set, boolean isToKeepSet) {
        final SharedPreferences sp = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = sp.edit();

        if(isToKeepSet) {
            for(Map.Entry<String, ?> entry : sp.getAll().entrySet()) {
                if(!set.contains(entry.getKey())) {
                    editor.remove(entry.getKey());
                }
            }
        } else {
            for(String key : set) {
                editor.remove(key);
            }
        }

        editor.commit();
    }

    public static void resetAppSettings(Context ctx) {
        final SharedPreferences spApp = KSharedPreferences.getAppSp(ctx);

        final int lastFileId = spApp.getInt(Constants.Sp.App.LAST_FILE_ID, 0);

        final String activeProfile = spApp.getString(Constants.Sp.App.ACTIVE_PROFILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT),
                     profiles = spApp.getString(Constants.Sp.App.PROFILE_NAMES, Constants.Sp.App.PROFILE_NAMES);

        final SharedPreferences.Editor editor = spApp.edit();

        editor.clear();

        editor.commit();

        editor.putInt(Constants.Sp.App.LAST_FILE_ID, lastFileId);
        editor.putString(Constants.Sp.App.ACTIVE_PROFILE_NAME, activeProfile);
        editor.putString(Constants.Sp.App.PROFILE_NAMES, profiles);

        editor.commit();
    }

    public static void resetActiveProfileSp(Context ctx) {
        final SharedPreferences sp = KSharedPreferences.getActiveProfileSp(ctx);

        boolean isDefaultProfile = KProfile.isUsingProfile(ctx, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT);

        if(isDefaultProfile) {
            final SharedPreferences.Editor editor = sp.edit();

            editor.clear();

            editor.commit();

            editor.putString(Constants.Sp.Profile.SP_FILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT);

            editor.commit();

            return;
        }

        final String name = sp.getString(Constants.Sp.Profile.SP_FILE_NAME, null),
                     userName = sp.getString(Constants.Sp.Profile.SP_FILE_USER_NAME, null),
                     iconColor = sp.getString(Constants.Sp.Profile.SP_FILE_ICON_COLOR, null);

        final int icon = sp.getInt(Constants.Sp.Profile.SP_FILE_ICON, -1);

        if(name == null || userName == null || iconColor == null || icon == -1) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            return;
        }

        final SharedPreferences.Editor editor = sp.edit();

        editor.clear();

        editor.commit();

        editor.putString(Constants.Sp.Profile.SP_FILE_NAME, name);
        editor.putString(Constants.Sp.Profile.SP_FILE_USER_NAME, userName);
        editor.putString(Constants.Sp.Profile.SP_FILE_ICON_COLOR, iconColor);
        editor.putInt(Constants.Sp.Profile.SP_FILE_ICON, icon);

        editor.commit();
    }

}
