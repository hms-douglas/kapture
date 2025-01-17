package dev.dect.kapture.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.widget.RemoteViews;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;

public class FullWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds) {
            renderUI(context, appWidgetId, appWidgetManager);
        }
    }

    public static void requestUpdateAllFromType(Context ctx) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);

        for(int appWidgetId : appWidgetManager.getAppWidgetIds(new ComponentName(ctx, FullWidget.class))) {
            renderUI(ctx, appWidgetId, appWidgetManager);
        }
    }

    private static void renderUI(Context ctx, int appWidgetId, AppWidgetManager appWidgetManager) {
        final Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        final int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),
                  minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        final boolean isVertical = minWidth <= minHeight;

        final RemoteViews views = new RemoteViews(ctx.getPackageName(), isVertical? R.layout.widget_full_vertical : R.layout.widget_full_horizontal);

        Utils.Widget.renderUiBtnsFull(ctx, views, appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        renderUI(context, appWidgetId, appWidgetManager);
    }
}
