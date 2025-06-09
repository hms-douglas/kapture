package dev.dect.kapture.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSharedPreferences;


@SuppressLint("ApplySharedPref")
public class KProfile {
    public static boolean isUsingProfile(Context ctx, String name) {
        return KSharedPreferences.getActiveProfileSp(ctx).getString(Constants.Sp.Profile.SP_FILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT).equals(name);
    }

    public static String getActiveProfileName(Context ctx) {
        return KSharedPreferences.getActiveProfileSp(ctx).getString(Constants.Sp.Profile.SP_FILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT);
    }
}
