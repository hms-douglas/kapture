package dev.dect.kapture.overlay;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.slider.Slider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.ColorPickerPopup;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.Utils;

@SuppressLint({"InflateParams", "ClickableViewAccessibility"})
public class DrawOverlay {
    private final KSettings KSETTINGS;

    private final Context CONTEXT;

    private final WindowManager WINDOW_MANAGER;

    private final SharedPreferences.Editor EDITOR;

    private final Float BUTTON_ALPHA_OFF = 0.5f;

    private View VIEW;

    private ImageView IMAGE,
                      IMAGE_DRAWING;

    private ImageButton BTN_UNDO,
                        BTN_REDO;

    private ConstraintLayout PEN_EXAMPLE;

    private LinearLayout CONTROLS,
                         CONTROLS_PEN,
                         PREVIOUS_COLORS;

    private Slider SIZE_SLIDER;

    private Bitmap BITMAP,
                   BITMAP_DRAWING;

    private Paint PAINT,
                  PAINT_DRAWING;

    private Canvas CANVAS,
                   CANVAS_DRAWING;

    private Path PATH;

    private final ArrayList<Path> PATHS_DRAWN = new ArrayList<>(),
                                  PATHS_UNDONE = new ArrayList<>();

    private final ArrayList<Paint> PAINTS_DRAWN = new ArrayList<>(),
                                   PAINTS_UNDONE = new ArrayList<>();

    private BroadcastReceiver RECEIVER_ORIENTATION;

    private int X_CONTROLS,
                Y_CONTROLS,
                X_LIMIT,
                Y_LIMIT;

    private long TIME_CONTROLS_TOUCH;

    private RelativeLayout.LayoutParams CONTROLS_LAYOUT_PARAMETERS;

    private int PEN_SIZE;

    private String PEN_COLOR;

    private final String[] PEN_PREVIOUS_COLORS;

    private final MenuOverlay MENU_OVERLAY;

    public DrawOverlay(Context ctx, KSettings ks, WindowManager wm, MenuOverlay mo) {
        this.KSETTINGS = ks;
        this.CONTEXT = ctx;
        this.WINDOW_MANAGER = wm;
        this.MENU_OVERLAY = mo;

        final SharedPreferences sp = ctx.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE);

        this.EDITOR = sp.edit();

        this.PEN_SIZE = sp.getInt(Constants.SP_KEY_DRAW_PEN_SIZE, ctx.getResources().getDimensionPixelSize(R.dimen.overlay_menu_draw_size_color_default));
        this.PEN_COLOR = sp.getString(Constants.SP_KEY_DRAW_PEN_COLOR, DefaultSettings.PEN_COLOR);
        this.PEN_PREVIOUS_COLORS = sp.getString(Constants.SP_KEY_DRAW_PEN_PREVIOUS_COLORS, DefaultSettings.PEN_PREVIOUS_COLORS).split(",");
    }

    public void render() {
        initVariables();

        initStyle();

        initPenControls();

        refreshPenExample();

        IMAGE_DRAWING.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initCanvas();

                Utils.drawTransparencyBackgroundOnImageView(VIEW.findViewById(R.id.penAlphaBackground));

                IMAGE_DRAWING.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        IMAGE_DRAWING.setOnTouchListener(this::draw);

        IMAGE_DRAWING.setOnTouchListener(this::draw);

        VIEW.findViewById(R.id.btnClose).setOnClickListener((v) -> destroy());

        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Utils.Overlay.setBasicLayoutParameters(layoutParams);

        setBasicLayoutParametersHelper(layoutParams);

        setControlsDraggable();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            WINDOW_MANAGER.addView(VIEW, layoutParams);

            registerOrientationReceiver();
        }, 300);
    }

    public void destroy() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            CONTEXT.unregisterReceiver(RECEIVER_ORIENTATION);

            WINDOW_MANAGER.removeViewImmediate(VIEW);
        }
    }

    private void initVariables() {
        VIEW = LayoutInflater.from(CONTEXT).inflate(R.layout.overlay_recording_draw, null, false);

        CONTROLS = VIEW.findViewById(R.id.controls);

        CONTROLS_PEN = VIEW.findViewById(R.id.controlsPen);

        PREVIOUS_COLORS = VIEW.findViewById(R.id.previousColors);

        PEN_EXAMPLE = VIEW.findViewById(R.id.penExample);

        SIZE_SLIDER = VIEW.findViewById(R.id.size);

        IMAGE = VIEW.findViewById(R.id.image);

        IMAGE_DRAWING = VIEW.findViewById(R.id.imageDrawing);

        BTN_UNDO = VIEW.findViewById(R.id.btnUndo);
        BTN_REDO = VIEW.findViewById(R.id.btnRedo);
    }

    private void initPenControls() {
        SIZE_SLIDER.setValueFrom(CONTEXT.getResources().getDimensionPixelSize(R.dimen.overlay_menu_draw_size_color_min));
        SIZE_SLIDER.setValueTo(CONTEXT.getResources().getDimensionPixelSize(R.dimen.overlay_menu_draw_size_color_max));
        SIZE_SLIDER.setValue(PEN_SIZE);

        SIZE_SLIDER.addOnChangeListener((slider, value, fromUser) -> {
            if(fromUser) {
                try {
                    PEN_SIZE = (int) value;

                    PAINT.setStrokeWidth(PEN_SIZE);
                    PAINT_DRAWING.setStrokeWidth(PEN_SIZE);

                    refreshPenExample();

                    EDITOR.putInt(Constants.SP_KEY_DRAW_PEN_SIZE, PEN_SIZE).commit();
                } catch (Exception ignore){}
            }
        });

        VIEW.findViewById(R.id.btnControlsPen).setOnClickListener((v) -> {
            if(CONTROLS_PEN.getVisibility() == View.VISIBLE) {
                closeControlsPen();
            } else {
                openControlsPen();
            }
        });

        VIEW.findViewById(R.id.picker).setOnClickListener((v) -> {
            final ColorPickerPopup colorPickerPopup = new ColorPickerPopup(CONTEXT, PEN_COLOR, true, this::setColor);

            colorPickerPopup.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);

            colorPickerPopup.show();
        });

        for(int i = PEN_PREVIOUS_COLORS.length - 1; i >= 0; i--) {
            final String c = PEN_PREVIOUS_COLORS[i];

            final View btn = PREVIOUS_COLORS.getChildAt(i);

            btn.setBackgroundTintList(ColorStateList.valueOf(Utils.Converter.hexColorToInt(c)));
            btn.setOnClickListener((v) -> setColor(c));
        }
    }

    private void initStyle() {
        if(KSETTINGS.getMenuStyle() == 1) {
            ((LinearLayout) VIEW.findViewById(R.id.controls)).setOrientation(LinearLayout.HORIZONTAL);
            ((LinearLayout) VIEW.findViewById(R.id.controlsMenu)).setOrientation(LinearLayout.VERTICAL);
            CONTROLS_PEN.setOrientation(LinearLayout.HORIZONTAL);
            PREVIOUS_COLORS.setOrientation(LinearLayout.VERTICAL);

            final ConstraintLayout sizeContainer = VIEW.findViewById(R.id.sizeContainer);

            final ViewGroup.LayoutParams layoutParamsSizeContainer = sizeContainer.getLayoutParams();

            layoutParamsSizeContainer.width = CONTEXT.getResources().getDimensionPixelSize(R.dimen.overlay_menu_draw_size_height);
            layoutParamsSizeContainer.height = WindowManager.LayoutParams.MATCH_PARENT;

            sizeContainer.setLayoutParams(layoutParamsSizeContainer);

            SIZE_SLIDER.setRotation(-90);
        }

        if(KSETTINGS.isToShowClearButtonOnDrawMenu()) {
            VIEW.findViewById(R.id.btnClear).setOnClickListener((v) -> {
                clearCanvas();
                clearUndo();
                clearRedo();

                BTN_UNDO.setAlpha(BUTTON_ALPHA_OFF);
                BTN_REDO.setAlpha(BUTTON_ALPHA_OFF);
            });
        } else {
            VIEW.findViewById(R.id.btnClear).setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowUndoRedoButtonOnDrawMenu()) {
            BTN_UNDO.setOnClickListener((v) -> undo());
            BTN_REDO.setOnClickListener((v) -> redo());

            BTN_UNDO.setAlpha(BUTTON_ALPHA_OFF);
            BTN_REDO.setAlpha(BUTTON_ALPHA_OFF);
        } else  {
            BTN_UNDO.setVisibility(View.GONE);
            BTN_REDO.setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowScreenshotButtonOnDrawMenu()) {
            VIEW.findViewById(R.id.btnScreenshot).setOnClickListener((l) -> MENU_OVERLAY.takeScreenshot());
        } else  {
            VIEW.findViewById(R.id.btnScreenshot).setVisibility(View.GONE);
        }

        if(KSETTINGS.isToShowDrawScreenshotButtonOnDrawMenu()) {
            VIEW.findViewById(R.id.btnScreenshotDraw).setOnClickListener((l) -> takeDrawScreenshot());
        } else  {
            VIEW.findViewById(R.id.btnScreenshotDraw).setVisibility(View.GONE);
        }
    }

    private void setBasicLayoutParametersHelper(WindowManager.LayoutParams layoutParams) {
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    private void registerOrientationReceiver() {
        RECEIVER_ORIENTATION = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            IMAGE.setImageBitmap(null);

            IMAGE_DRAWING.setImageBitmap(null);

            new Handler(Looper.getMainLooper()).postDelayed(() -> initCanvas(), 350);
            }
        };

        CONTEXT.registerReceiver(RECEIVER_ORIENTATION, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }

    private void setControlsDraggable() {
        CONTROLS_LAYOUT_PARAMETERS = (RelativeLayout.LayoutParams) CONTROLS.getLayoutParams();

        setInitialPosition();

        CONTROLS.setOnTouchListener(this::dragControl);

        VIEW.findViewById(R.id.btnClose).setOnTouchListener(this::dragControl);
        VIEW.findViewById(R.id.btnClear).setOnTouchListener(this::dragControl);
        VIEW.findViewById(R.id.btnUndo).setOnTouchListener(this::dragControl);
        VIEW.findViewById(R.id.btnRedo).setOnTouchListener(this::dragControl);
        VIEW.findViewById(R.id.btnControlsPen).setOnTouchListener(this::dragControl);
    }

    private void setInitialPosition() {
        final SharedPreferences sp = CONTEXT.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE);

        CONTROLS_LAYOUT_PARAMETERS.leftMargin = sp.getInt(Constants.SP_KEY_OVERLAY_DRAW_X_POS, 0);
        CONTROLS_LAYOUT_PARAMETERS.topMargin = sp.getInt(Constants.SP_KEY_OVERLAY_DRAW_Y_POS, 0);

        CONTROLS.setLayoutParams(CONTROLS_LAYOUT_PARAMETERS);
    }

    private boolean dragControl(View v, MotionEvent e) {
        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                X_CONTROLS = (int) e.getRawX() - CONTROLS.getLeft();
                Y_CONTROLS = (int) e.getRawY() - CONTROLS.getTop();

                X_LIMIT = WINDOW_MANAGER.getCurrentWindowMetrics().getBounds().width() - CONTROLS.getWidth();
                Y_LIMIT = WINDOW_MANAGER.getCurrentWindowMetrics().getBounds().height() - CONTROLS.getHeight();

                TIME_CONTROLS_TOUCH = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_MOVE:
                final int x = (int) e.getRawX() - X_CONTROLS,
                          y = (int) e.getRawY() - Y_CONTROLS;

                if(x < X_LIMIT && x > 0) {
                    CONTROLS_LAYOUT_PARAMETERS.leftMargin = x;
                }

                if(y < Y_LIMIT && y > 0) {
                    CONTROLS_LAYOUT_PARAMETERS.topMargin = y;
                }

                CONTROLS.setLayoutParams(CONTROLS_LAYOUT_PARAMETERS);
                break;

            case MotionEvent.ACTION_UP:
                if(System.currentTimeMillis() - TIME_CONTROLS_TOUCH <= 200) {
                    v.callOnClick();
                } else {
                    EDITOR.putInt(Constants.SP_KEY_OVERLAY_DRAW_X_POS, CONTROLS_LAYOUT_PARAMETERS.leftMargin);
                    EDITOR.putInt(Constants.SP_KEY_OVERLAY_DRAW_Y_POS, CONTROLS_LAYOUT_PARAMETERS.topMargin);

                    EDITOR.apply();
                }
                break;
        }

        return true;
    }

    private void initCanvas() {
        BITMAP = Bitmap.createBitmap(IMAGE.getWidth(), IMAGE.getHeight(), Bitmap.Config.ARGB_8888);

        BITMAP_DRAWING = Bitmap.createBitmap(IMAGE_DRAWING.getWidth(), IMAGE_DRAWING.getHeight(), Bitmap.Config.ARGB_8888);

        CANVAS = new Canvas(BITMAP);

        CANVAS_DRAWING = new Canvas(BITMAP_DRAWING);

        IMAGE.setImageBitmap(BITMAP);

        IMAGE_DRAWING.setImageBitmap(BITMAP_DRAWING);

        PATH = new Path();

        setNewBasicPaint();

        clearUndo();
    }

    private void setNewBasicPaint() {
        PAINT = generateBasicPaint();

        PAINT_DRAWING = generateBasicPaint();

        setColor(PEN_COLOR);
    }

    private Paint generateBasicPaint() {
        final Paint p = new Paint();

        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);

        p.setStrokeWidth(PEN_SIZE);

        return p;
    }

    private void clearCanvas() {
        CANVAS.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        IMAGE.setImageBitmap(BITMAP);

        clearDrawingCanvas();
    }

    private void clearDrawingCanvas() {
        CANVAS_DRAWING.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        IMAGE_DRAWING.setImageBitmap(BITMAP_DRAWING);
    }

    private boolean draw(View v, MotionEvent event) {
        final float x = event.getX(),
                    y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                closeControlsPen();

                clearUndo();

                PATH.moveTo(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                PATH.lineTo(x, y);

                CANVAS_DRAWING.drawPath(PATH, PAINT_DRAWING);

                IMAGE_DRAWING.setImageBitmap(BITMAP_DRAWING);
                break;

            case MotionEvent.ACTION_UP:
                CANVAS.drawPath(PATH, PAINT);

                IMAGE.setImageBitmap(BITMAP);

                clearDrawingCanvas();

                addUndo();
                break;
        }

        return true;
    }

    private void clearUndo() {
        PATHS_UNDONE.clear();
        PAINTS_UNDONE.clear();
        PATH.reset();
    }

    private void addUndo() {
        PATHS_DRAWN.add(PATH);
        PAINTS_DRAWN.add(PAINT);

        PATH = new Path();

        setNewBasicPaint();

        BTN_UNDO.setAlpha(1f);
    }

    private void undo() {
        clearCanvas();

        if(!PATHS_DRAWN.isEmpty()) {
            PATHS_UNDONE.add(PATHS_DRAWN.remove(PATHS_DRAWN.size() - 1));
            PAINTS_UNDONE.add(PAINTS_DRAWN.remove(PAINTS_DRAWN.size() - 1));

            for(int i = 0; i < PATHS_DRAWN.size(); i++) {
                CANVAS.drawPath(PATHS_DRAWN.get(i), PAINTS_DRAWN.get(i));
            }

            IMAGE.setImageBitmap(BITMAP);

            BTN_REDO.setAlpha(1f);

            if(PATHS_DRAWN.isEmpty()) {
                BTN_UNDO.setAlpha(BUTTON_ALPHA_OFF);
            }
        }
    }

    private void clearRedo() {
        PATHS_DRAWN.clear();
        PAINTS_DRAWN.clear();
    }

    private void redo() {
        if(!PATHS_UNDONE.isEmpty()) {
            final Path path = PATHS_UNDONE.remove(PATHS_UNDONE.size() - 1);

            final Paint paint = PAINTS_UNDONE.remove(PAINTS_UNDONE.size() - 1);

            PATHS_DRAWN.add(path);
            PAINTS_DRAWN.add(paint);

            CANVAS.drawPath(path, paint);

            IMAGE.setImageBitmap(BITMAP);

            BTN_UNDO.setAlpha(1f);

            if(PATHS_UNDONE.isEmpty()) {
                BTN_REDO.setAlpha(BUTTON_ALPHA_OFF);
            }
        }
    }

    private void refreshPenExample() {
        final ViewGroup.LayoutParams layoutParams = PEN_EXAMPLE.getLayoutParams();

        layoutParams.width = PEN_SIZE;
        layoutParams.height = PEN_SIZE;

        PEN_EXAMPLE.setLayoutParams(layoutParams);

        PEN_EXAMPLE.setBackgroundTintList(ColorStateList.valueOf(Utils.Converter.hexColorToInt(PEN_COLOR)));
    }

    private void setColor(String color) {
        PEN_COLOR = color;

        final int[] argb = Utils.Converter.hexColorToArgb(color);

        PAINT.setARGB(argb[0], argb[1], argb[2], argb[3]);

        PAINT_DRAWING.setARGB(255, argb[1], argb[2], argb[3]);

        IMAGE_DRAWING.setImageAlpha(argb[0]);

        refreshPenExample();

        EDITOR.putString(Constants.SP_KEY_DRAW_PEN_COLOR, PEN_COLOR).commit();

        for(String s : PEN_PREVIOUS_COLORS) {
            if(s.equals(PEN_COLOR)) {
                return;
            }
        }

        for(int i = PEN_PREVIOUS_COLORS.length - 1; i > 0; i--) {
            final String c = PEN_PREVIOUS_COLORS[i - 1];

            PEN_PREVIOUS_COLORS[i] = c;

            final View btn = PREVIOUS_COLORS.getChildAt(i);

            btn.setBackgroundTintList(ColorStateList.valueOf(Utils.Converter.hexColorToInt(c)));
            btn.setOnClickListener((v) -> setColor(c));
        }

        PEN_PREVIOUS_COLORS[0] = color;

        final View btn = PREVIOUS_COLORS.getChildAt(0);

        btn.setBackgroundTintList(ColorStateList.valueOf(Utils.Converter.hexColorToInt(color)));
        btn.setOnClickListener((v) -> setColor(color));

        EDITOR.putString(Constants.SP_KEY_DRAW_PEN_PREVIOUS_COLORS, String.join(",", PEN_PREVIOUS_COLORS)).commit();
    }

    private void closeControlsPen() {
        CONTROLS_PEN.setVisibility(View.GONE);
    }

    private void openControlsPen() {
        CONTROLS_PEN.setVisibility(View.VISIBLE);

        if(KSETTINGS.getMenuStyle() == 1) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                final ViewGroup.LayoutParams layoutParamsSizeSlider = SIZE_SLIDER.getLayoutParams();

                layoutParamsSizeSlider.width = VIEW.findViewById(R.id.sizeContainer).getMeasuredHeight();

                SIZE_SLIDER.setLayoutParams(layoutParamsSizeSlider);
            }, 50);
        }
    }

    public void hideMenu() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            CONTROLS.setVisibility(View.GONE);
        }
    }

    public void showMenu() {
        if(VIEW != null && VIEW.isAttachedToWindow()) {
            CONTROLS.setVisibility(View.VISIBLE);
        }
    }

    private void takeDrawScreenshot() {
        hideMenu();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                final File screenshot = KFile.generateScreenshotFile(CONTEXT, KSETTINGS);

                final FileOutputStream fileOutputStream = new FileOutputStream(screenshot);

                BITMAP.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                fileOutputStream.close();

                CapturingService.screenshotTaken(new Kapture.Screenshot(screenshot));

                KFile.notifyMediaScanner(CONTEXT, screenshot);
            } catch (Exception ignore) {
                Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
            }

            showMenu();
        }, 50);
    }
}
