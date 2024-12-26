package dev.dect.kapture.utils;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.Settings;
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
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.widget.BasicWidget;
import dev.dect.kapture.widget.FullWidget;

public class Utils {
    public static void updateWidgets(Context ctx) {
        BasicWidget.requestUpdateAllFromType(ctx);
        FullWidget.requestUpdateAllFromType(ctx);
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

    public static boolean isInRange(int value, int min, int max) {
        return (max > min) ? (value >= min && value <= max) : (value >= max && value <= min);
    }

    public static class Converter {
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

        public static int[] hexColorToArgb(String hex) {
            final int color = Color.parseColor(hex.substring(0, 7)),
                      alpha = Integer.valueOf(hex.substring(7), 16);

            return new int[] {alpha, Color.red(color), Color.green(color), Color.blue(color)};
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
            ctx.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ctx.getPackageName())));
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

        public static void callOutAnimation(Dialog dialog, ConstraintLayout container, ConstraintLayout view) {

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
            final SharedPreferences sp = ctx.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE);

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

            final SharedPreferences.Editor editor = view.getContext().getSharedPreferences(Constants.SP, Context.MODE_PRIVATE).edit();

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
                }

                return true;
            });
        }
    }
}
