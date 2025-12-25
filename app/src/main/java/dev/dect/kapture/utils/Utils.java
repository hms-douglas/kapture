package dev.dect.kapture.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.json.JSONArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SplittableRandom;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.ActionActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.widget.BasicWidget;
import dev.dect.kapture.widget.FullWidget;
import dev.dect.kapture.widget.ProfileWidget;

public class Utils {
    public static void updateStatusBarColor(Activity activity) {
        boolean isLightMode = activity.getResources().getConfiguration().isNightModeActive();

        new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(!isLightMode);
    }

    public static boolean hasWriteSecureSettings(Context ctx) {
        return ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }

    public static int generateTimeId() {
        return (int) new Date().getTime();
    }

    public static void drawTransparencyBackgroundOnImageView(ImageView imageView) {
        final Bitmap b = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas c = new Canvas(b);

        Utils.drawTransparencyBackgroundOnCanvas(c);

        imageView.setImageBitmap(b);
    }

    public static void drawTransparencyBackgroundOnCanvas(Canvas canvas) {
        final float height = canvas.getHeight(),
                    width = canvas.getWidth();

        final Paint paint = new Paint();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#FDFDFD"));

        canvas.drawRect(new RectF(0, 0, width, height), paint);

        paint.setColor(Color.parseColor("#808080"));

        final float size = 16.5f,
                amountLines = height / size;

        for(float i = 0; i < amountLines; i++) {
            final float startXat = i % 2 == 0 ? 0 : size;

            for(float j = 0; j < ((width - startXat) / size); j += 2) {
                final float x = (size * j) + startXat,
                        y = size * i;

                canvas.drawRect(new RectF(x, y, x + size, y + size), paint);
            }
        }
    }

    public static void drawColorSampleOnImageView(ImageView imageView, int color) {
        final int colorSize = 100;

        final Bitmap bitmap = Bitmap.createBitmap(colorSize, colorSize, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap);

        Utils.drawTransparencyBackgroundOnCanvas(canvas);

        final Paint paintColor = new Paint();

        paintColor.setStyle(Paint.Style.FILL);
        paintColor.setColor(color);

        canvas.drawRect(new RectF(0, 0, colorSize, colorSize), paintColor);

        final Bitmap cuttedBitmap = Bitmap.createBitmap(colorSize, colorSize, Bitmap.Config.ARGB_8888);

        final Canvas canvasCut = new Canvas(cuttedBitmap);

        final Rect rect = new Rect(0, 0, colorSize, colorSize);

        final Paint paintCut = new Paint();

        paintCut.setAntiAlias(true);

        canvasCut.drawARGB(0, 0, 0, 0);

        canvasCut.drawCircle(colorSize / 2f, colorSize / 2f, colorSize / 2f, paintCut);

        paintCut.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvasCut.drawBitmap(bitmap, rect, rect, paintCut);

        imageView.setImageBitmap(cuttedBitmap);
    }

    public static Bitmap cropWatchThumbnail(Bitmap thumbnail) {
        final int size = thumbnail.getWidth();

        final Bitmap cropped = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(cropped);

        final Paint paint = new Paint();

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);

        paint.setColor(Color.BLACK);

        canvas.drawOval(0, 0, size, size, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(thumbnail, 0, 0, paint);

        return cropped;
    }

    public static boolean isInRange(int value, int min, int max) {
        return (max > min) ? (value >= min && value <= max) : (value >= max && value <= min);
    }

    public static Drawable getDrawable(Context ctx, int id) {
        return ResourcesCompat.getDrawable(ctx.getResources(), id, ctx.getTheme());
    }

    public static Drawable colorDrawable(Drawable drawable, String color) {
        return colorDrawable(drawable, Utils.Converter.hexColorToInt(color));
    }

    public static Drawable colorDrawable(Drawable drawable, int color) {
        DrawableCompat.setTint(drawable, color);

        return drawable;
    }

    public static class Converter {
        private static final String TAG = Utils.class.getSimpleName() + "." + Converter.class.getSimpleName();

        public static int dpToPx(Context ctx, int dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
        }

        public static int dpToPx(Context ctx, float dp) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
        }

        public static int[] secondsToHMS(int seconds) {
            final int hour = seconds / 3600,
                      minute = (seconds % 3600) / 60;

            seconds %= 60;

            return new int[]{hour, minute, seconds};
        }

        public static int timeToSeconds(String s) {
            final String[] time = s.split(":");

            int seconds = 0;

            int j = 0;

            for(int i = time.length - 1; i >= 0; i--) {
                seconds += (int) (Integer.parseInt(time[i]) * Math.pow(60, j++));
            }

            return seconds;
        }

        public static int hexColorToInt(String hex) {
            final int[] argb = hexColorToArgb(hex);

            return Color.argb(argb[0], argb[1], argb[2], argb[3]);
        }

        public static String intColorToHex(int i) {
            return intColorToHex(i, Color.alpha(i));
        }

        public static String intColorToHex(int i, int a) {
            final int r = Color.red(i),
                      g = Color.green(i),
                      b = Color.blue(i);

            final String rH = Integer.toHexString(r),
                         gH = Integer.toHexString(g),
                         bH = Integer.toHexString(b),
                         aH = Integer.toHexString(a);

            return "#" + Formatter.hexIndividualNumber(rH) + Formatter.hexIndividualNumber(gH) + Formatter.hexIndividualNumber(bH) + Formatter.hexIndividualNumber(aH);
        }

        public static String intColorToHex(Context ctx, int id) {
            return intColorToHex(ctx.getColor(id));
        }

        public static int[] hexColorToArgb(String hex) {
            final int color = Color.parseColor(hex.substring(0, 7)),
                      alpha = Integer.valueOf(hex.substring(7), 16);

            return new int[] {alpha, Color.red(color), Color.green(color), Color.blue(color)};
        }

        public static ArrayList<String> jsonArrayToStringArrayList(JSONArray jsonArray) {
            final ArrayList<String> arrayList = new ArrayList<>();

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    arrayList.add(jsonArray.getString(i));
                }
            } catch (Exception e) {
                Log.e(TAG, "jsonArrayToStringArrayList: " + e.getMessage());
            }

            return arrayList;
        }

        public static ArrayList<String> jsonArrayStringToStringArrayList(String jsonArrayString) {
            try {
                return jsonArrayToStringArrayList(new JSONArray(jsonArrayString));
            } catch (Exception e) {
                Log.e(TAG, "jsonArrayStringToStringArrayList: " + e.getMessage());

                return new ArrayList<>();
            }
        }

        public static String jsonArrayPacksToStringAppNames(Context ctx, JSONArray packages) {
            String names = "";

            try {
                if(packages.length() > 1) {
                    for(int i = 0; i < packages.length() - 1; i++) {
                        names += Utils.Package.getAppName(ctx, packages.getString(i)) + " \u2022 ";
                    }
                }

                names += Utils.Package.getAppName(ctx, packages.getString(packages.length() - 1));
            } catch (Exception e) {
                Log.e(TAG, "jsonArrayPacksToStringAppNames: " + e.getMessage());
            }

            return names;
        }

        public static String arrayListPacksToStringAppNames(Context ctx, ArrayList<String> packages) {
            return jsonArrayPacksToStringAppNames(ctx, stringArrayListToJsonArray(packages));
        }

        public static JSONArray stringArrayListToJsonArray(ArrayList<String> list) {
            final JSONArray jsonArray = new JSONArray();

            for(String s : list) {
                jsonArray.put(s);
            }

            return jsonArray;
        }
    }

    public static class ExternalActivity {
        private static final String TAG = Utils.class.getSimpleName() + "." + ExternalActivity.class.getSimpleName();

        public static void requestAccessibility(Context ctx) {
            try {
                final Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ctx.startActivity(i);
            } catch (Exception e) {
                Log.e(TAG, "requestAccessibility: " + e.getMessage());

                Toast.makeText(ctx, ctx.getString(R.string.toast_info_enable_accessibility_manually), Toast.LENGTH_SHORT).show();
            }
        }

        public static void requestSettings(Context ctx) {
            try {
                ctx.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ctx.getPackageName())));
            } catch (Exception e) {
                Log.e(TAG, "requestSettings: " + e.getMessage());

                Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
            }
        }

        public static void requestLanguageSettings(Context ctx) {
            try {
                ctx.startActivity(new Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.parse("package:" + ctx.getPackageName())));
            } catch (Exception e) {
                Log.e(TAG, "requestLanguageSettings: " + e.getMessage());

                requestSettings(ctx);
            }
        }

        public static void requestHomeScreen(Context ctx) {
            try {
                final Intent i = new Intent(Intent.ACTION_MAIN);

                i.addCategory(Intent.CATEGORY_HOME);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ctx.startActivity(i);
            } catch (Exception e) {
                Log.e(TAG, "requestHomeScreen: " + e.getMessage());

                Toast.makeText(ctx, ctx.getString(R.string.toast_error_launch_homescreen), Toast.LENGTH_SHORT).show();
            }
        }

        public static void requestOpenUrlOnBrowser(Context ctx, String url) {
            final Intent iEmpty = new Intent();

            iEmpty.setAction(Intent.ACTION_VIEW);
            iEmpty.addCategory(Intent.CATEGORY_BROWSABLE);
            iEmpty.setData(Uri.fromParts("http", "", null));

            final Intent i = new Intent();

            i.setAction(Intent.ACTION_VIEW);
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.setData(Uri.parse(url));
            i.setSelector(iEmpty);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                ctx.startActivity(i);
            } catch (Exception e) {
                Log.e(TAG, "requestOpenUrlOnBrowser: " + e.getMessage());

                Toast.makeText(ctx, ctx.getString(R.string.toast_error_open_url), Toast.LENGTH_SHORT).show();
            }
        }

        public static void requestApp(Context ctx, String packageName, boolean popup) {
            if(packageName.equals(Constants.HOME_PACKAGE_NAME)) {
                requestHomeScreen(ctx);
            } else {
                try {
                    final Intent i = ctx.getPackageManager().getLaunchIntentForPackage(packageName);

                    if(popup) {

                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                        final Rect rect = ctx.getSystemService(WindowManager.class).getCurrentWindowMetrics().getBounds(),
                                   pos = new Rect(20, rect.height() / 4, rect.width() - 20, rect.height() - (rect.height() / 4));

                        final ActivityOptions activityOptions = ActivityOptions.makeBasic();

                        final ActivityOptions activityOptionsBound = activityOptions.setLaunchBounds(pos);

                        ctx.startActivity(i, activityOptionsBound.toBundle());
                    } else {

                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        ctx.startActivity(i);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "requestApp: " + e.getMessage());

                    Toast.makeText(ctx, ctx.getString(R.string.toast_error_launch_app), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static class Package {
        private static final String TAG = Utils.class.getSimpleName() + "." + Package.class.getSimpleName();

        public static String getAppName(Context ctx, String packageName) {
            if(packageName.equals(Constants.HOME_PACKAGE_NAME)) {
                return ctx.getString(R.string.package_launch_home);
            }

            try {
                final PackageManager packageManager = ctx.getPackageManager();

                return packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0)).loadLabel(packageManager).toString();
            } catch (Exception e) {
                Log.e(TAG, "getAppName: " + e.getMessage());

                return "?";
            }
        }

        public static Drawable getAppIcon(Context ctx, String packageName) {
            try {
                return ctx.getPackageManager().getApplicationIcon(packageName);
            } catch (Exception e) {
                Log.e(TAG, "getAppIcon: " + e.getMessage());
            }

            return getDrawable(ctx, R.drawable.icon_launch_app);
        }

        public static List<ApplicationInfo> getAllAppsInfo(Context ctx, boolean userOnly) {
            final List<ApplicationInfo> apps = new ArrayList<>();

            final String kapture = ctx.getPackageName();

            if(userOnly) {
                final PackageManager pm = ctx.getPackageManager();

                for(ApplicationInfo ai : pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))) {
                    if(pm.getLaunchIntentForPackage(ai.packageName) != null && ai.enabled && !ai.packageName.equals(kapture)) {
                        apps.add(ai);
                    }
                }
            } else {
                for(ApplicationInfo ai : ctx.getPackageManager().getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))) {
                    if(!ai.packageName.equals(kapture) && ai.enabled) {
                        apps.add(ai);
                    }
                }
            }

            return apps;
        }

        public static boolean isAppInstalledAndEnabled(Context ctx, String packageName) {
            if(packageName.equals(Constants.HOME_PACKAGE_NAME)) {
                return true;
            }

            try {
                return ctx.getPackageManager().getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0)).enabled;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "isAppInstalledAndEnabled: " + e.getMessage());

                return false;
            }
        }
    }

    public static class Formatter {
        private static final String TAG = Utils.class.getSimpleName() + "." + Formatter.class.getSimpleName();

        public static String date(long l) {
            try {
                String datePattern = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())).toLocalizedPattern();

                if(!datePattern.contains("dd")) {
                    datePattern = datePattern.replaceAll("d", "dd");
                }

                if(!datePattern.contains("MM")) {
                    datePattern = datePattern.replaceAll("M", "MM");
                }

                return new SimpleDateFormat(datePattern + " HH:mm", Locale.getDefault()).format(new Date(l));
            } catch (Exception e) {
                Log.e(TAG, "date: " + e.getMessage());

                return "?";
            }
        }

        public static String timeInMillis(long milli) {
            final long duration = milli / 1000,
                       hours = duration / 3600,
                       minutes = (duration / 60) - (hours * 60),
                       seconds = duration - (hours * 3600) - (minutes * 60);

            String r = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

            if(r.startsWith("00:")) {
                r = r.replaceFirst("00:", "");
            }

            return r;
        }

        public static String timeInSeconds(int seconds) {
            return timeInMillis(seconds * 1000L);
        }

        public static String hexIndividualNumber(String n) {
            return (n.length() == 1 ? "0" + n : n).toUpperCase(Locale.ROOT);
        }
    }

    public static class Keyboard {
        public static void requestShowForInput(EditText editText) {
            editText.requestFocus();

            editText.postDelayed(() -> {
                InputMethodManager keyboard = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(editText, 0);

                editText.setSelection(editText.getText().toString().length());
            }, 100);
        }

        public static void requestCloseFromInput(Context ctx, EditText editText) {
            ((InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static class Popup {
        public static void setMaxHeight(Context ctx, ConstraintLayout view) {
            final int screenHeight = ctx.getSystemService(WindowManager.class).getCurrentWindowMetrics().getBounds().height(),
                      maxHeightPercentage = ctx.getResources().getInteger(R.integer.popup_max_height_percentage);

            view.setMaxHeight((screenHeight * maxHeightPercentage) / 100);
        }

        public static void callOutAnimation(Dialog dialog, ConstraintLayout container, View view) {

            final Animation popupAnimationOut = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.popup_out),
                            popupContainerOut = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.popup_background_out);

            popupAnimationOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    dialog.dismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            container.startAnimation(popupContainerOut);
            view.startAnimation(popupAnimationOut);
        }

        public static void callOutAnimation(Dialog dialog, ConstraintLayout container, ConstraintLayout view, Runnable onAnimationEnd) {
            final Animation popupAnimationOut = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.popup_out),
                            popupContainerOut = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.popup_background_out);

            popupAnimationOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    dialog.dismiss();

                    onAnimationEnd.run();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            container.startAnimation(popupContainerOut);
            view.startAnimation(popupAnimationOut);
        }
    }

    public static class Overlay {
        public static void setBasicLayoutParameters(WindowManager.LayoutParams layoutParams) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        public static void setLayoutParametersPosition(WindowManager.LayoutParams layoutParams, Context ctx, String keyX, String keyY) {
            final SharedPreferences sp = KSharedPreferences.getActiveProfileSp(ctx);

            if(sp.contains(keyX)) {
                layoutParams.x = sp.getInt(keyX, 0);
                layoutParams.y = sp.getInt(keyY, 0);
            } else {
                layoutParams.gravity = Gravity.TOP;
            }
        }

        public static void setDefaultDraggableView(View view, WindowManager.LayoutParams layoutParams, WindowManager windowManager, String keyX, String keyY) {
            final int[] coordinates = new int[4];

            final long[] time = new long[1];

            final SharedPreferences.Editor editor = KSharedPreferences.getActiveProfileSp(view.getContext()).edit();

            view.setOnTouchListener((v, e) -> {
                switch(e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        coordinates[0] = layoutParams.x;
                        coordinates[1] = layoutParams.y;
                        coordinates[2] = (int) e.getRawX();
                        coordinates[3] = (int) e.getRawY();

                        time[0] = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        layoutParams.x = (int) (coordinates[0] + (e.getRawX() - coordinates[2]));
                        layoutParams.y = (int) (coordinates[1] + (e.getRawY() - coordinates[3]));

                        windowManager.updateViewLayout(view, layoutParams);
                        break;

                    case MotionEvent.ACTION_UP:
                        if(new Date().getTime() - time[0] <= 200) {
                            v.performClick();
                        } else {
                            editor.putInt(keyX, layoutParams.x);
                            editor.putInt(keyY, layoutParams.y);

                            editor.apply();
                        }

                        break;
                }

                return true;
            });
        }
    }

    public static class Widget {
        public static void updateWidgetsCapturingBtns(Context ctx) {
            BasicWidget.requestUpdateAllFromType(ctx);
            FullWidget.requestUpdateAllFromType(ctx);
            ProfileWidget.requestUpdateAllFromType(ctx);
        }

        public static void renderUiBtnsFull(Context ctx, RemoteViews views, int appWidgetId) {
            final Intent intentStartStop = new Intent(ctx, ActionActivity.class);

            intentStartStop.putExtra(ActionActivity.INTENT_FROM, ActionActivity.FROM_WIDGET);

            intentStartStop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            if(CapturingService.isRecording()) {
                intentStartStop.putExtra(ActionActivity.INTENT_ACTION, Constants.Widget.Action.STOP);

                views.setTextViewCompoundDrawables(
                    R.id.startStopCapturing,
                    R.drawable.icon_capture_stop,
                    0,
                    0,
                    0
                );

                final Intent intentPauseResume = new Intent(ctx, ActionActivity.class);

                intentPauseResume.putExtra(ActionActivity.INTENT_FROM, ActionActivity.FROM_WIDGET);

                intentPauseResume.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                views.setInt(R.id.pauseResumeCapturing, "setBackgroundResource", R.drawable.btn_floating_background_circle);

                if(CapturingService.isPaused()) {
                    intentPauseResume.putExtra(ActionActivity.INTENT_ACTION, Constants.Widget.Action.RESUME);

                    views.setTextViewCompoundDrawables(
                        R.id.pauseResumeCapturing,
                        R.drawable.icon_capture_resume,
                        0,
                        0,
                        0
                    );
                } else {
                    intentPauseResume.putExtra(ActionActivity.INTENT_ACTION, Constants.Widget.Action.PAUSE);

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
                intentStartStop.putExtra(ActionActivity.INTENT_ACTION, Constants.Widget.Action.START);

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
        }
    }
}
