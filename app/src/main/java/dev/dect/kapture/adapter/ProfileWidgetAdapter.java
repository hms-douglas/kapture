package dev.dect.kapture.adapter;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.Utils;
import dev.dect.kapture.widget.ProfileWidget;

public class ProfileWidgetAdapter implements RemoteViewsService.RemoteViewsFactory {
    private final Context CONTEXT;

    private String ACTIVE_PROFILE_NAME;

    private ArrayList<SharedPreferences> PROFILES;

    public ProfileWidgetAdapter(Context ctx) {
        this.CONTEXT = ctx;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final SharedPreferences sp = PROFILES.get(position);

        return buildProfile(
            sp.getString(Constants.Sp.Profile.SP_FILE_USER_NAME, CONTEXT.getString(R.string.profile_default)),
            sp.getString(Constants.Sp.Profile.SP_FILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT),
            KProfile.getIconResId(sp.getInt(Constants.Sp.Profile.SP_FILE_ICON, 0)),
            position == 0 ? CONTEXT.getColor(R.color.app_bar_profile) : Utils.Converter.hexColorToInt(sp.getString(Constants.Sp.Profile.SP_FILE_ICON_COLOR, Utils.Converter.intColorToHex(CONTEXT, R.color.app_bar_profile)))
        );
    }

    private RemoteViews buildProfile(String userName, String name, int iconResId, int iconColor) {
        final RemoteViews profile = new RemoteViews(CONTEXT.getPackageName(), R.layout.widget_profile_item);

        profile.setImageViewResource(R.id.icon, iconResId);
        profile.setInt(R.id.icon, "setColorFilter", iconColor);

        profile.setTextViewText(R.id.name, userName);

        profile.setImageViewResource(R.id.radio, ACTIVE_PROFILE_NAME.equals(name) ? R.drawable.radio_on : R.drawable.radio_off);

        final Intent fillInIntent = new Intent();

        fillInIntent.putExtra(ProfileWidget.INTENT_PROFILE_NAME, name);

        profile.setOnClickFillInIntent(R.id.profile, fillInIntent);

        return profile;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {
        this.ACTIVE_PROFILE_NAME = KSharedPreferences.getActiveProfileSp(CONTEXT).getString(Constants.Sp.Profile.SP_FILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT);
        this.PROFILES = KProfile.getAllProfilesSp(CONTEXT);

        this.PROFILES.add(0, KSharedPreferences.getProfileSp(CONTEXT, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT));
    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return PROFILES.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}