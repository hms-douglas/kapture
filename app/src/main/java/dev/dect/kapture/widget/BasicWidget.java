package dev.dect.kapture.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.ActionActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.service.CapturingService;

public class BasicWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds) {
            renderUI(context, appWidgetId, appWidgetManager);
        }
    }

    public static void requestUpdateAllFromType(Context ctx) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);

        for(int appWidgetId : appWidgetManager.getAppWidgetIds(new ComponentName(ctx, BasicWidget.class))) {
            renderUI(ctx, appWidgetId, appWidgetManager);
        }
    }

    private static void renderUI(Context ctx, int appWidgetId, AppWidgetManager appWidgetManager) {
        final RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_basic);

        final Intent intentStartStop = new Intent(ctx, ActionActivity.class);

        intentStartStop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        intentStartStop.putExtra(ActionActivity.INTENT_FROM, ActionActivity.FROM_WIDGET);

        if(CapturingService.isRecording()) {
            intentStartStop.putExtra(ActionActivity.INTENT_ACTION, Constants.Widget.Action.STOP);

            views.setTextViewCompoundDrawables(
                R.id.startStopCapturing,
                R.drawable.icon_capture_stop,
                0,
                0,
                0
            );

        } else {
            intentStartStop.putExtra(ActionActivity.INTENT_ACTION, Constants.Widget.Action.START);

            views.setTextViewCompoundDrawables(
                R.id.startStopCapturing,
                    R.drawable.icon_capture_start,
                0,
                0,
                0
            );
        }

        final PendingIntent pendingIntentStartStop = PendingIntent.getActivity(
            ctx,
            appWidgetId,
            intentStartStop,
            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.startStopCapturing, pendingIntentStartStop);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
