package dev.dect.kapture.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.wear.remote.interactions.RemoteActivityHelper;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;

@SuppressLint("WearRecents")
public class Utils {
    public static boolean hasWriteSecureSettings(Context ctx) {
        return ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
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

    public static boolean isInRange(int value, int min, int max) {
        return (max > min) ? (value >= min && value <= max) : (value >= max && value <= min);
    }

    public static Drawable getDrawable(Context ctx, int id) {
        return ResourcesCompat.getDrawable(ctx.getResources(), id, ctx.getTheme());
    }

    public static class Remote {
        public static void getPhoneNodeAsync(Context ctx, Node[] nodeRef) {
            new Thread(() -> {
                try {
                    final Task<List<Node>> nodeListTask = Wearable.getNodeClient(ctx).getConnectedNodes();

                    nodeRef[0] = Tasks.await(nodeListTask).get(0);
                } catch (Exception ignore) {}
            }).start();
        }

        public static void openLinkOnPhone(Context ctx, Node[] nodeRef, String link, boolean forceHttpsProtocol) {
            try {
                if(forceHttpsProtocol && !link.startsWith("https")) {
                    if(link.startsWith("http:")) {
                        link = link.replaceFirst("http:", "https:");
                    } else {
                        link = "https://" + link;
                    }
                }

                final Intent i = new Intent(Intent.ACTION_VIEW);

                i.addCategory(Intent.CATEGORY_BROWSABLE);

                i.setData(Uri.parse(link));

                final RemoteActivityHelper remoteActivityHelper = new RemoteActivityHelper(ctx, Executors.newSingleThreadExecutor());

                remoteActivityHelper.startRemoteActivity(i, nodeRef[0].getId());

                Toast.makeText(ctx, ctx.getString(R.string.toast_info_check_phone), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                try {
                    ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));

                    return;
                } catch (Exception ignore) {}

                Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
            }
        }

        public static void openLinkOnPhone(Context ctx, Node[] nodeRef, String link) {
            openLinkOnPhone(ctx, nodeRef, link, true);
        }
    }

    public static class Converter {
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
    }

    public static class ExternalActivity {
        public static void requestAccessibility(Context ctx) {
            try {
                final Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ctx.startActivity(i);
            } catch (Exception ignore) {
                Toast.makeText(ctx, ctx.getString(R.string.toast_info_enable_accessibility_manually), Toast.LENGTH_SHORT).show();
            }
        }

        public static void requestSettings(Context ctx) {
            try {
                ctx.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ctx.getPackageName())));
            } catch (Exception ignore) {
                Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
            }
        }

        public static void requestApp(Context ctx, String packageName, boolean popup) {
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
            } catch (Exception ignore) {
                Toast.makeText(ctx, ctx.getString(R.string.toast_error_launch_app), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class Package {
        public static String getAppName(Context ctx, String packageName) {
            if(packageName.equals(Constants.HOME_PACKAGE_NAME)) {
                return ctx.getString(R.string.package_launch_home);
            }

            try {
                final PackageManager packageManager = ctx.getPackageManager();

                return packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0)).loadLabel(packageManager).toString();
            } catch (Exception ignore) {
                return "?";
            }
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
    }

    public static class Formatter {
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
            } catch (Exception ignore) {
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
    }

    public static class Keyboard {
        public static void requestShowForInput(EditText editText) {
            editText.requestFocus();

            editText.postDelayed(() -> {
                InputMethodManager keyboard = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(editText, 0);
            }, 100);
        }
    }
}
