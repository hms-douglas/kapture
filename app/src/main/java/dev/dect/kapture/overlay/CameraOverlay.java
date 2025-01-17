package dev.dect.kapture.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class CameraOverlay {
    private final KSettings KSETTINGS;

    private final Context CONTEXT;

    private final WindowManager WINDOW_MANAGER;

    private final WindowManager.LayoutParams LAYOUT_PARAMETERS;


    private final CameraManager CAMERA_MANAGER;

    private final int SIZE;

    private View VIEW;

    private TextureView TEXTURE_VIEW;

    private CameraCaptureSession CAMERA_CAPTURE_SESSION;

    private String CAMERA_ID_FRONT = null,
                   CAMERA_ID_BACK = null;

    private boolean IS_CURRENTLY_SHOWING_FRONT;

    private ScaleGestureDetector SCALE_GESTURE_DETECTOR;

    private float SCALE_FACTOR = 1f;

    private int X,
                Y,
                RAW_X,
                RAW_Y;

    private long TIME;

    private SharedPreferences.Editor EDITOR_PROFILE;

    public CameraOverlay(Context ctx, KSettings ks, WindowManager wm) {
        this.KSETTINGS = ks;
        this.CONTEXT = ctx;
        this.WINDOW_MANAGER = wm;
        this.LAYOUT_PARAMETERS = new WindowManager.LayoutParams();

        this.SIZE = Utils.Converter.dpToPx(ctx, ks.getCameraSize());

        this.CAMERA_MANAGER = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);

        this.IS_CURRENTLY_SHOWING_FRONT = ks.getCameraFacingLens() == CameraCharacteristics.LENS_FACING_FRONT;
    }

    public void render() {
        if(!KSETTINGS.isToShowFloatingCamera()) {
            return;
        }

        forceRender();
    }

    public void forceRender() {
        VIEW = LayoutInflater.from(CONTEXT).inflate(R.layout.overlay_recording_camera, null, false);

        if(KSETTINGS.isToToggleCameraOrientation()) {
            VIEW.setOnClickListener((l) -> toggleCamera());
        }

        TEXTURE_VIEW = VIEW.findViewById(R.id.camera);

        VIEW.findViewById(R.id.frame).setBackgroundResource(KSETTINGS.getCameraShapeResource());

        Utils.Overlay.setBasicLayoutParameters(LAYOUT_PARAMETERS);

        Utils.Overlay.setLayoutParametersPosition(
            LAYOUT_PARAMETERS,
            CONTEXT,
            Constants.Sp.Profile.OVERLAY_CAMERA_X_POS,
            Constants.Sp.Profile.OVERLAY_CAMERA_Y_POS
        );

        if(KSETTINGS.isCameraScalable()) {
            SCALE_GESTURE_DETECTOR =  new ScaleGestureDetector(CONTEXT, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(@NonNull ScaleGestureDetector scaleGestureDetector) {
                SCALE_FACTOR *= scaleGestureDetector.getScaleFactor();

                TEXTURE_VIEW.setScaleX(SCALE_FACTOR);
                TEXTURE_VIEW.setScaleY(SCALE_FACTOR);

                LAYOUT_PARAMETERS.width = (int) (SIZE * SCALE_FACTOR);
                LAYOUT_PARAMETERS.height = (int) (SIZE * SCALE_FACTOR);

                WINDOW_MANAGER.updateViewLayout(VIEW, LAYOUT_PARAMETERS);

                return true;
                }
            });

            EDITOR_PROFILE = KSharedPreferences.getActiveProfileSp(CONTEXT).edit();

            VIEW.setOnTouchListener((view, e) -> {
                switch(e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        X = LAYOUT_PARAMETERS.x;
                        Y = LAYOUT_PARAMETERS.y;
                        RAW_X = (int) e.getRawX();
                        RAW_Y = (int) e.getRawY();

                        TIME = new Date().getTime();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        LAYOUT_PARAMETERS.x = (int) (X + (e.getRawX() - RAW_X));
                        LAYOUT_PARAMETERS.y = (int) (Y + (e.getRawY() - RAW_Y));

                        WINDOW_MANAGER.updateViewLayout(view, LAYOUT_PARAMETERS);
                        break;

                    case MotionEvent.ACTION_UP:
                        if(new Date().getTime() - TIME <= 200) {
                            VIEW.performClick();
                        } else {
                            EDITOR_PROFILE.putInt(Constants.Sp.Profile.OVERLAY_CAMERA_X_POS, LAYOUT_PARAMETERS.x);
                            EDITOR_PROFILE.putInt(Constants.Sp.Profile.OVERLAY_CAMERA_Y_POS, LAYOUT_PARAMETERS.y);

                            EDITOR_PROFILE.apply();
                        }
                }

                return SCALE_GESTURE_DETECTOR.onTouchEvent(e);
            });
        } else {
            Utils.Overlay.setDefaultDraggableView(
                VIEW,
                LAYOUT_PARAMETERS,
                WINDOW_MANAGER,
                Constants.Sp.Profile.OVERLAY_CAMERA_X_POS,
                Constants.Sp.Profile.OVERLAY_CAMERA_Y_POS
            );
        }

        setLayoutParametersSize(LAYOUT_PARAMETERS);

        initCamerasIds();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            WINDOW_MANAGER.addView(VIEW, LAYOUT_PARAMETERS);

            startCamera();
        }, 300);
    }

    public void destroy() {
        if(isRendering()) {
            WINDOW_MANAGER.removeViewImmediate(VIEW);

            stopCamera();
        }
    }

    public boolean isRendering() {
        return VIEW != null && VIEW.isAttachedToWindow();
    }

    public void toggleVisibility() {
        VIEW.setVisibility(VIEW.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    /** @noinspection DataFlowIssue*/
    private void initCamerasIds() {
        boolean hasFront = false,
                hasBack = false;
        try {
            for(String id : CAMERA_MANAGER.getCameraIdList()) {
                final CameraCharacteristics cameraCharacteristics = CAMERA_MANAGER.getCameraCharacteristics(id);

                if(!hasFront && cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    hasFront = true;

                    CAMERA_ID_FRONT = id;
                } else if(!hasBack && cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    hasBack = true;

                    CAMERA_ID_BACK = id;
                }
            }
        } catch (Exception ignore) {}
    }

    private void setLayoutParametersSize(WindowManager.LayoutParams layoutParams) {
        layoutParams.height = SIZE;
        layoutParams.width = SIZE;

        final ViewGroup.LayoutParams layoutParamsTextureView = TEXTURE_VIEW.getLayoutParams();

        layoutParamsTextureView.width = layoutParams.width;
        layoutParamsTextureView.height = (int) (layoutParams.height / 0.75f);

        TEXTURE_VIEW.setLayoutParams(layoutParamsTextureView);
    }

    @SuppressLint("MissingPermission")
    private void startCamera() {
        try {
            CAMERA_MANAGER.openCamera(
                IS_CURRENTLY_SHOWING_FRONT ? CAMERA_ID_FRONT : CAMERA_ID_BACK,
                new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice cameraDevice) {
                        new Handler(Looper.getMainLooper()).post(() -> onCameraOpened(TEXTURE_VIEW, cameraDevice));
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                        cameraDevice.close();
                    }

                    @Override
                    public void onError(@NonNull CameraDevice cameraDevice, int i) {
                        cameraDevice.close();
                    }
                },
                null
            );
        } catch (CameraAccessException ignore) {}
    }

    private void stopCamera() {
        try {
            CAMERA_CAPTURE_SESSION.abortCaptures();
            CAMERA_CAPTURE_SESSION.stopRepeating();
            CAMERA_CAPTURE_SESSION.close();
        } catch (Exception ignore) {}
    }

    private void toggleCamera() {
        stopCamera();

        IS_CURRENTLY_SHOWING_FRONT = !IS_CURRENTLY_SHOWING_FRONT;

        new Handler(Looper.getMainLooper()).postDelayed(this::startCamera, 300);
    }

    private void onCameraOpened(TextureView textureView, CameraDevice cameraDevice) {
        final SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();

        final Surface surface = new Surface(surfaceTexture);

        try {
            final CaptureRequest.Builder captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            captureRequest.addTarget(surface);

            final OutputConfiguration outputConfiguration = new OutputConfiguration(surface);

            final SessionConfiguration sessionConfiguration = new SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    Collections.singletonList(outputConfiguration),
                    CONTEXT.getMainExecutor(),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession ccs) {
                            CAMERA_CAPTURE_SESSION = ccs;

                            captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                            try {
                                CAMERA_CAPTURE_SESSION.setRepeatingRequest(captureRequest.build(), null, null);
                            } catch (CameraAccessException ignore) {}
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {}
                    }
            );

            cameraDevice.createCaptureSession(sessionConfiguration);

        } catch (CameraAccessException ignore) {}
    }
}
