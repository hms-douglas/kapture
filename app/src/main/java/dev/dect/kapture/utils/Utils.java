package dev.dect.kapture.utils;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.dect.kapture.R;

public class Utils {
    public static boolean hasWriteSecureSettings(Context ctx) {
        return ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }

    public static int generateTimeId() {
        return (int) new Date().getTime();
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

        public static String time(long milli) {
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

        public static void setInAnimation(Dialog dialog, ConstraintLayout container, ConstraintLayout view) {
            dialog.setOnShowListener((d) -> {
                final Animation popupAnimationIn = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.popup_in),
                                popupContainerIn = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.popup_background_in);

                container.startAnimation(popupContainerIn);
                view.startAnimation(popupAnimationIn);
            });
        }

        public static void setInAnimation(Dialog dialog, ConstraintLayout container, ConstraintLayout view, Runnable onShow) {
            dialog.setOnShowListener((d) -> {
                final Animation popupAnimationIn = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.popup_in),
                        popupContainerIn = AnimationUtils.loadAnimation(dialog.getContext(), R.anim.popup_background_in);

                container.startAnimation(popupContainerIn);
                view.startAnimation(popupAnimationIn);

                onShow.run();
            });
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
}
