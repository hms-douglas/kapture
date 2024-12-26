package dev.dect.kapture.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.ActionActivity;
import dev.dect.kapture.data.Constants;

public class WifiShareWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds) {
            renderUI(context, appWidgetId, appWidgetManager);
        }
    }

    private static void renderUI(Context ctx, int appWidgetId, AppWidgetManager appWidgetManager) {
        final RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_wifi_share);

        final Intent intentWifiShare = new Intent(ctx, ActionActivity.class);

        intentWifiShare.putExtra(ActionActivity.INTENT_ACTION, Constants.SHORTCUT_STATIC_ACTION_WIFI_SHARE);

        final PendingIntent pendingIntentWifiShare = PendingIntent.getActivity(
            ctx,
            appWidgetId,
            intentWifiShare,
            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.btnWifiShare, pendingIntentWifiShare);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
