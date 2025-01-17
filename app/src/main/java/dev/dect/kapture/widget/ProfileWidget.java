package dev.dect.kapture.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.profile.ProfileManagerActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.service.ProfileWidgetService;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.Utils;

public class ProfileWidget extends AppWidgetProvider {
    public static final String INTENT_PROFILE_NAME = "name";

    @Override
    public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds) {
            renderUI(ctx, appWidgetId, appWidgetManager);
            addProfilesAdapter(ctx, appWidgetId, appWidgetManager);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Constants.Widget.Action.SET_PROFILE)) {
            KProfile.setActiveProfile(context, intent.getStringExtra(INTENT_PROFILE_NAME), false);
        }

        super.onReceive(context, intent);
    }

    public void addProfilesAdapter(Context ctx, int appWidgetId, AppWidgetManager appWidgetManager) {
        final RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_profile);

        final Intent intentService = new Intent(ctx, ProfileWidgetService.class);

        intentService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        views.setRemoteAdapter(R.id.profiles, intentService);

        final Intent intentAction = new Intent(ctx, ProfileWidget.class);

        intentAction.setAction(Constants.Widget.Action.SET_PROFILE);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intentAction, PendingIntent.FLAG_MUTABLE);

        views.setPendingIntentTemplate(R.id.profiles, pendingIntent);

        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
    }

    public static void requestUpdateAllFromType(Context ctx) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);

        for(int appWidgetId : appWidgetManager.getAppWidgetIds(new ComponentName(ctx, ProfileWidget.class))) {
            renderUI(ctx, appWidgetId, appWidgetManager);
        }
    }

    public static void renderUI(Context ctx, int appWidgetId, AppWidgetManager appWidgetManager) {
        final RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_profile);

        Utils.Widget.renderUiBtnsFull(ctx, views, appWidgetId);

        views.setTextViewText(R.id.label, ctx.getString(R.string.widget_profile));

        final Intent intentManage = new Intent(ctx, ProfileManagerActivity.class);

        intentManage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        final PendingIntent pendingIntentManage = PendingIntent.getActivity(
            ctx,
            appWidgetId,
            intentManage,
            PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.btnManage, pendingIntentManage);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.profiles);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
