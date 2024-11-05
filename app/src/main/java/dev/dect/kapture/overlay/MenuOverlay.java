package dev.dect.kapture.overlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;
import dev.dect.kapture.view.KChronometer;

@SuppressLint({"InflateParams", "SetTextI18n", "ClickableViewAccessibility"})
public class MenuOverlay {
    private final KSettings KSETTINGS;

    private final Context CONTEXT;

    private final WindowManager WINDOW_MANAGER;

    private final SharedPreferences.Editor EDITOR;

    private final CameraOverlay CAMERA_OVERLAY;

    private final DrawOverlay DRAW_OVERLAY;

    private final int[] COORDINATES_HELPER = new int[4];

    private final long[] TIME_HELPER = new long[1];

    private View VIEW;

    private WindowManager.LayoutParams LAYOUT_PARAMETERS;

    private KChronometer CHRONOMETER;

    private LinearLayout LINEAR_LAYOUT;

    private Surface MEDIA_RECORDER_SURFACE;

    private boolean IS_MINIMIZED;

    private ImageButton BTN_PAUSE_RESUME;

    public MenuOverlay(Context ctx, KSettings ks, WindowManager wm, CameraOverlay co) {
        this.KSETTINGS = ks;
        this.CONTEXT = ctx;
        this.WINDOW_MANAGER = wm;
        this.CAMERA_OVERLAY = co;
        this.DRAW_OVERLAY = new DrawOverlay(ctx, ks, wm);

        this.EDITOR = ctx.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE).edit();
    }

    public void render() {
        if(!KSETTINGS.isToShowFloatingMenu()) {
            return;
        }

        VIEW = LayoutInflater.from(CONTEXT).inflate(KSETTINGS.getMenuStyle() == 0 ? R.layout.overlay_recording_menu_horizontal : R.layout.overlay_recording_menu_vertical, null, false);

        LAYOUT_PARAMETERS = new WindowManager.LayoutParams();

        LINEAR_LAYOUT = VIEW.findViewById(R.id.linearLayout);

        Utils.Overlay.setBasicLayoutParameters(LAYOUT_PARAMETERS);

        Utils.Overlay.setLayoutParametersPosition(
            LAYOUT_PARAMETERS,
            CONTEXT,
            Constants.SP_KEY_OVERLAY_MENU_X_POS,
            Constants.SP_KEY_OVERLAY_MENU_Y_POS
        );

        setViewDraggable();

        VIEW.findViewById(R.id.btnStop).setOnClickListener((v) -> CapturingService.requestStopRecording());

        setDraggableHelper(VIEW.findViewById(R.id.btnStop));

        boolean allOptionalIsHidden = true;

        BTN_PAUSE_RESUME = VIEW.findViewById(R.id.btnPauseResume);

        if(KSETTINGS.isToShowPauseResumeButtonOnMenu()) {
            BTN_PAUSE_RESUME.setOnClickListener((v) -> CapturingService.requestTogglePauseResumeRecording());

            setDraggableHelper(BTN_PAUSE_RESUME);

            allOptionalIsHidden = false;
        } else {
            BTN_PAUSE_RESUME.setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowCameraButtonOnMenu()) {
            VIEW.findViewById(R.id.btnCamera).setOnClickListener((v) -> {
                if(!CAMERA_OVERLAY.isRendering()) {
                    CAMERA_OVERLAY.forceRender();
                } else {
                    CAMERA_OVERLAY.toggleVisibility();
                }
            });

            setDraggableHelper(VIEW.findViewById(R.id.btnCamera));

            allOptionalIsHidden = false;
        } else {
            VIEW.findViewById(R.id.btnCamera).setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowScreenshotButtonOnMenu()) {
            VIEW.findViewById(R.id.btnScreenshot).setOnClickListener((v) -> takeScreenshot());

            setDraggableHelper(VIEW.findViewById(R.id.btnScreenshot));

            allOptionalIsHidden = false;
        } else {
            VIEW.findViewById(R.id.btnScreenshot).setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowDrawButtonOnMenu()) {
            VIEW.findViewById(R.id.btnDraw).setOnClickListener((v) -> {
                minimize();

                DRAW_OVERLAY.render();
            });

            setDraggableHelper(VIEW.findViewById(R.id.btnDraw));

            allOptionalIsHidden = false;
        } else {
            VIEW.findViewById(R.id.btnDraw).setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowTimeLimitOnMenu()) {
            ((TextView) VIEW.findViewById(R.id.timeLimit)).setText((KSETTINGS.getMenuStyle() == 0 ? "/ " : "") + Utils.Formatter.timeInSeconds(KSETTINGS.getSecondsTimeLimit()));

            allOptionalIsHidden = false;
        } else {
            VIEW.findViewById(R.id.timeLimit).setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowMinimizeButtonOnMenu()) {
            final ImageButton btnMinimize = VIEW.findViewById(R.id.btnMinimize);

            btnMinimize.setOnClickListener((v) -> minimize());

            btnMinimize.setImageResource(KSETTINGS.getMinimizingSide() == 0 ? R.drawable.overlay_menu_icon_minimize_right : R.drawable.overlay_menu_icon_minimize_left);

            setDraggableHelper(btnMinimize);

            allOptionalIsHidden = false;
        } else {
            VIEW.findViewById(R.id.btnMinimize).setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowCloseButtonOnMenu()) {
            VIEW.findViewById(R.id.btnClose).setOnClickListener((v) -> destroy());

            setDraggableHelper(VIEW.findViewById(R.id.btnClose));

            allOptionalIsHidden = false;
        } else {
            VIEW.findViewById(R.id.btnClose).setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowTimeOnMenu()) {
            CHRONOMETER = VIEW.findViewById(R.id.time);

            allOptionalIsHidden = false;

            CHRONOMETER.start();
        } else {
            VIEW.findViewById(R.id.time).setVisibility(View.GONE);
        }

        if(allOptionalIsHidden) {
            LINEAR_LAYOUT.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            WINDOW_MANAGER.addView(VIEW, LAYOUT_PARAMETERS);

            if(KSETTINGS.isToStartMenuMinimized()) {
                minimize();
            }
        }, 300);
    }

    public void destroy() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            if(CHRONOMETER != null) {
                CHRONOMETER.stop();
            }

            WINDOW_MANAGER.removeViewImmediate(VIEW);
        }

        DRAW_OVERLAY.destroy();
    }

    public void setMediaRecorderSurface(Surface s) {
        MEDIA_RECORDER_SURFACE = s;
    }

    private void takeScreenshot() {
        if(MEDIA_RECORDER_SURFACE != null) {
            VIEW.setVisibility(View.GONE);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                final Bitmap bitmap = Bitmap.createBitmap(KSETTINGS.getVideoWidth(), KSETTINGS.getVideoHeight(), Bitmap.Config.ARGB_8888);

                final HandlerThread handlerThread = new HandlerThread("PixelCopier");

                handlerThread.start();

                PixelCopy.request(
                    MEDIA_RECORDER_SURFACE,
                    bitmap,
                    (result) -> {
                        if(result == PixelCopy.SUCCESS) {
                            new Handler(Looper.getMainLooper()).post(() -> VIEW.setVisibility(View.VISIBLE));

                            final String fileName =
                                    KFile.getFileIdNotGenerated(CONTEXT)
                                    + KFile.FILE_SEPARATOR
                                    + KFile.getDefaultScreenshotName(CONTEXT);

                            final File screenshot = KFile.generateFileIncrementalName(KSETTINGS.getSavingScreenshotLocationFile(), fileName, ".jpg");

                            try {
                                final FileOutputStream out = new FileOutputStream(screenshot);

                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

                                out.flush();
                                out.close();

                            } catch (Exception ignore) {}
                        }

                        handlerThread.quitSafely();
                    },
                    new Handler(handlerThread.getLooper())
                );
            }, 50);
        }
    }

    private void setDraggableHelper(View view) {
        view.setOnTouchListener((v, e) -> {
            switch(e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    COORDINATES_HELPER[0] = LAYOUT_PARAMETERS.x;
                    COORDINATES_HELPER[1] = LAYOUT_PARAMETERS.y;
                    COORDINATES_HELPER[2] = (int) e.getRawX();
                    COORDINATES_HELPER[3] = (int) e.getRawY();

                    TIME_HELPER[0] = System.currentTimeMillis();
                    break;

                case MotionEvent.ACTION_MOVE:
                    LAYOUT_PARAMETERS.x = (int) (COORDINATES_HELPER[0] + (e.getRawX() - COORDINATES_HELPER[2]));
                    LAYOUT_PARAMETERS.y = (int) (COORDINATES_HELPER[1] + (e.getRawY() - COORDINATES_HELPER[3]));

                    WINDOW_MANAGER.updateViewLayout(VIEW, LAYOUT_PARAMETERS);
                    break;

                case MotionEvent.ACTION_UP:
                    if(new Date().getTime() - TIME_HELPER[0] <= 200) {
                        v.performClick();
                    } else {
                        EDITOR.putInt(Constants.SP_KEY_OVERLAY_MENU_X_POS, LAYOUT_PARAMETERS.x);
                        EDITOR.putInt(Constants.SP_KEY_OVERLAY_MENU_Y_POS, LAYOUT_PARAMETERS.y);

                        EDITOR.apply();
                    }
            }

            return true;
        });
    }

    private void setViewDraggable() {
        Utils.Overlay.setDefaultDraggableView(
            VIEW,
            LAYOUT_PARAMETERS,
            WINDOW_MANAGER,
            Constants.SP_KEY_OVERLAY_MENU_X_POS,
            Constants.SP_KEY_OVERLAY_MENU_Y_POS
        );
    }

    private void minimize() {
        if(IS_MINIMIZED) {
            return;
        }

        IS_MINIMIZED = true;

        VIEW.setPadding(12,18,12,18);

        final ValueAnimator move = ValueAnimator.ofInt(LAYOUT_PARAMETERS.x, WINDOW_MANAGER.getCurrentWindowMetrics().getBounds().width() / (KSETTINGS.getMinimizingSide() == 0 ? 2 : -2));

        move.addUpdateListener(valueAnimator -> {
            LAYOUT_PARAMETERS.x = (Integer) valueAnimator.getAnimatedValue();

            WINDOW_MANAGER.updateViewLayout(VIEW, LAYOUT_PARAMETERS);
        });

        move.setDuration(350);

        move.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                VIEW.setAlpha(0.6f);
            }
        });

        move.start();

        if(KSETTINGS.getMenuStyle() == 0) {
            minimizeHorizontal();
        } else {
            minimizeVertical();
        }
    }

    private void minimizeHorizontal() {
        final int l = LINEAR_LAYOUT.getChildCount();

        for(int i = 0; i < l; i++) {
            final View view = LINEAR_LAYOUT.getChildAt(i);

            if(view.getVisibility() == View.VISIBLE) {
                final ValueAnimator close = ValueAnimator.ofInt(view.getWidth(), 0);

                final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

                view.setTag(layoutParams.width);

                close.addUpdateListener(valueAnimator -> {
                    layoutParams.width = (Integer) valueAnimator.getAnimatedValue();

                    view.setLayoutParams(layoutParams);
                });

                close.setDuration(350);

                close.start();
            }
        }

        LINEAR_LAYOUT.setTag(LINEAR_LAYOUT.getShowDividers());

        LINEAR_LAYOUT.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);

        final float[] x = new float[1];

        VIEW.setOnTouchListener((view, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                x[0] = event.getRawX();
            } else if(event.getAction() == MotionEvent.ACTION_UP) {
                if(Math.abs(x[0] - event.getRawX()) > 90) {
                    maximizeHorizontal();
                }
            }

            return true;
        });
    }

    private void maximizeHorizontal() {
        final int l = LINEAR_LAYOUT.getChildCount();

        for(int i = 0; i < l; i++) {
            final View view2 = LINEAR_LAYOUT.getChildAt(i);

            if(view2.getVisibility() == View.VISIBLE) {
                final ValueAnimator open = ValueAnimator.ofInt(0, Integer.parseInt(view2.getTag().toString()));

                open.addUpdateListener(valueAnimator -> {
                    final ViewGroup.LayoutParams layoutParams = view2.getLayoutParams();

                    layoutParams.width = (Integer) valueAnimator.getAnimatedValue();

                    view2.setLayoutParams(layoutParams);
                });

                open.setDuration(350);

                open.start();

                IS_MINIMIZED = false;
            }
        }

        VIEW.setPadding(0,0,0,0);

        VIEW.setAlpha(1f);

        LINEAR_LAYOUT.setShowDividers(Integer.parseInt(LINEAR_LAYOUT.getTag().toString()));

        setViewDraggable();
    }

    private void minimizeVertical() {
        final int l = LINEAR_LAYOUT.getChildCount();

        for(int i = 0; i < l; i++) {
            final View view = LINEAR_LAYOUT.getChildAt(i);

            if(view.getVisibility() == View.VISIBLE) {
                final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

                view.setTag(R.id.width, layoutParams.width);
                view.setTag(R.id.height, layoutParams.height);

                final ValueAnimator closeWidth = ValueAnimator.ofInt(view.getWidth(), 0);

                closeWidth.addUpdateListener(valueAnimator -> {
                    layoutParams.width = (Integer) valueAnimator.getAnimatedValue();

                    view.setLayoutParams(layoutParams);
                });

                final ValueAnimator closeHeight = ValueAnimator.ofInt(view.getHeight(), 0);

                closeWidth.addUpdateListener(valueAnimator -> {
                    layoutParams.height = (Integer) valueAnimator.getAnimatedValue();

                    view.setLayoutParams(layoutParams);
                });

                closeWidth.setDuration(350);
                closeHeight.setDuration(350);

                closeWidth.start();
                closeHeight.start();
            }
        }

        LINEAR_LAYOUT.setTag(LINEAR_LAYOUT.getShowDividers());

        LINEAR_LAYOUT.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);

        final float[] x = new float[1];

        VIEW.setOnTouchListener((view, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                x[0] = event.getRawX();
            } else if(event.getAction() == MotionEvent.ACTION_UP) {
                if(Math.abs(x[0] - event.getRawX()) > 90) {
                    maximizeVertical();
                }
            }

            return true;
        });
    }

    private void maximizeVertical() {
        final int l = LINEAR_LAYOUT.getChildCount();

        for(int i = 0; i < l; i++) {
            final View view2 = LINEAR_LAYOUT.getChildAt(i);

            if(view2.getVisibility() == View.VISIBLE) {
                final ValueAnimator openWidth = ValueAnimator.ofInt(0, Integer.parseInt(view2.getTag(R.id.width).toString()));

                openWidth.addUpdateListener(valueAnimator -> {
                    final ViewGroup.LayoutParams layoutParams = view2.getLayoutParams();

                    layoutParams.width = (Integer) valueAnimator.getAnimatedValue();

                    view2.setLayoutParams(layoutParams);
                });

                final ValueAnimator openHeight = ValueAnimator.ofInt(0, Integer.parseInt(view2.getTag(R.id.height).toString()));

                openWidth.addUpdateListener(valueAnimator -> {
                    final ViewGroup.LayoutParams layoutParams = view2.getLayoutParams();

                    layoutParams.height = (Integer) valueAnimator.getAnimatedValue();

                    view2.setLayoutParams(layoutParams);
                });

                openWidth.setDuration(350);
                openHeight.setDuration(350);

                openWidth.start();
                openHeight.start();

                IS_MINIMIZED = false;
            }
        }

        VIEW.setPadding(0,0,0,0);

        VIEW.setAlpha(1f);

        LINEAR_LAYOUT.setShowDividers(Integer.parseInt(LINEAR_LAYOUT.getTag().toString()));

        setViewDraggable();
    }

    public void refreshRecordingState() {
        if(CapturingService.isPaused()) {
            BTN_PAUSE_RESUME.setImageResource(R.drawable.overlay_menu_icon_resume);

            if(CHRONOMETER != null) {
                CHRONOMETER.pause();
            }
        } else {
            BTN_PAUSE_RESUME.setImageResource(R.drawable.overlay_menu_icon_pause);

            if(CHRONOMETER != null) {
                CHRONOMETER.resume();
            }
        }
    }
}
