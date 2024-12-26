package dev.dect.kapture.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.util.SplittableRandom;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.ActionActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.service.CapturingService;

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

        final Intent intentStartStop = new Intent(ctx, ActionActivity.class);

        intentStartStop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if(CapturingService.isRecording()) {
            intentStartStop.putExtra(ActionActivity.INTENT_ACTION, Constants.SHORTCUT_STATIC_ACTION_STOP);

            views.setTextViewCompoundDrawables(
                R.id.startStopCapturing,
                R.drawable.icon_capture_stop,
                0,
                0,
                0
            );

            final Intent intentPauseResume = new Intent(ctx, ActionActivity.class);

            intentPauseResume.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            views.setInt(R.id.pauseResumeCapturing, "setBackgroundResource", R.drawable.btn_floating_background_circle);

            if(CapturingService.isPaused()) {
                intentPauseResume.putExtra(ActionActivity.INTENT_ACTION, Constants.SHORTCUT_STATIC_ACTION_RESUME);

                views.setTextViewCompoundDrawables(
                    R.id.pauseResumeCapturing,
                    R.drawable.icon_capture_resume,
                    0,
                    0,
                    0
                );
            } else {
                intentPauseResume.putExtra(ActionActivity.INTENT_ACTION, Constants.SHORTCUT_STATIC_ACTION_PAUSE);

                views.setTextViewCompoundDrawables(
                    R.id.pauseResumeCapturing,
                    R.drawable.icon_capture_pause,
                    0,
                    0,
                    0
                );
            }

            final PendingIntent pendingIntentPauseResume = PendingIntent.getActivity(
                ctx,
                appWidgetId + new SplittableRandom().nextInt(90, 999),
                intentPauseResume,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            views.setOnClickPendingIntent(R.id.pauseResumeCapturing, pendingIntentPauseResume);
        } else {
            intentStartStop.putExtra(ActionActivity.INTENT_ACTION, Constants.SHORTCUT_STATIC_ACTION_START);

            views.setTextViewCompoundDrawables(
                R.id.startStopCapturing,
                    R.drawable.icon_capture_start,
                0,
                0,
                0
            );

            views.setInt(R.id.pauseResumeCapturing, "setBackgroundResource", R.drawable.btn_floating_background_circle_widget_disabled);

            views.setTextViewCompoundDrawables(
                R.id.pauseResumeCapturing,
                R.drawable.icon_capture_pause_widget_disabled,
                0,
                0,
                0
            );
        }

        final PendingIntent pendingIntentStartStop = PendingIntent.getActivity(
            ctx,
            appWidgetId,
            intentStartStop,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.startStopCapturing, pendingIntentStartStop);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        renderUI(context, appWidgetId, appWidgetManager);
    }
}
