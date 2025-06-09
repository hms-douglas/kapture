package dev.dect.kapture.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.io.File;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.activity.TokenActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.notification.CapturingNotification;
import dev.dect.kapture.recorder.InternalAudioRecorder;
import dev.dect.kapture.recorder.ScreenMicRecorder;
import dev.dect.kapture.recorder.utils.BeforeStartOption;
import dev.dect.kapture.recorder.utils.StopOption;
import dev.dect.kapture.service.utils.CountdownHelper;
import dev.dect.kapture.tile.FullTile;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.KMediaProjection;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.KTimer;
import dev.dect.kapture.utils.Utils;

/** @noinspection resource*/
public class CapturingService extends AccessibilityService {
    private static boolean IS_SERVICE_RUNNING = false,
                           IS_RECORDING = false,
                           IS_PROCESSING = false,
                           IS_PAUSED = false,
                           IS_IN_COUNTDOWN = false;

    private static CapturingService CAPTURING_SERVICE;

    private CapturingNotification NOTIFICATION_CAPTURING;

    private ScreenMicRecorder SCREEN_MIC_RECORDER;

    private InternalAudioRecorder INTERNAL_AUDIO_RECORDER;

    private KSettings KSETTINGS;

    private StopOption STOP_OPTION;

    private BeforeStartOption BEFORE_START_OPTION;

    private Kapture KAPTURE;

    private KTimer KTIMER;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public void onCreate() {
        super.onCreate();

        IS_SERVICE_RUNNING = true;

        IS_RECORDING = false;

        IS_PROCESSING = false;

        IS_PAUSED = false;

        CAPTURING_SERVICE = this;

        if(Utils.hasWriteSecureSettings(this)) {
            startRecording();
        }
    }

    @Override
    public void onDestroy() {
        IS_SERVICE_RUNNING = false;

        IS_RECORDING = false;

        IS_PAUSED = false;

        CAPTURING_SERVICE = null;

        KAPTURE = null;

        super.onDestroy();
    }

    public static void requestStartRecording(Context ctx) {
        if(!IS_RECORDING && !IS_PROCESSING && !IS_PAUSED) {
            if(IS_SERVICE_RUNNING) {
                CAPTURING_SERVICE.startRecording();
            } else if(Utils.hasWriteSecureSettings(ctx)) {
                Settings.Secure.putString(ctx.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, CapturingService.class.getName().replaceAll(ctx.getPackageName(), ctx.getPackageName() + "/"));
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.toast_info_accessibility_manually), Toast.LENGTH_SHORT).show();

                Utils.ExternalActivity.requestAccessibility(ctx);
            }
        }
    }

    public static void requestStopRecording() {
        if(IS_SERVICE_RUNNING && !IS_PROCESSING) {
            CAPTURING_SERVICE.stopRecording();
        }
    }

    public static void requestToggleRecording(Context ctx) {
        if(IS_RECORDING) {
            CapturingService.requestStopRecording();
        } else {
            CapturingService.requestStartRecording(ctx);
        }
    }

    public static void requestPauseRecording() {
        if(!IS_PAUSED && IS_RECORDING && !IS_PROCESSING) {
            CAPTURING_SERVICE.pauseRecording();
        }
    }

    public static void requestResumeRecording() {
        if(IS_PAUSED && IS_RECORDING && !IS_PROCESSING) {
            CAPTURING_SERVICE.resumeRecording();
        }
    }

    public static void requestTogglePauseResumeRecording() {
        if(IS_PAUSED) {
            CapturingService.requestResumeRecording();
        } else {
            CapturingService.requestPauseRecording();
        }
    }

    public static boolean isRecording() {
        return IS_RECORDING;
    }

    public static boolean isProcessing() {
        return IS_PROCESSING;
    }

    public static boolean isPaused() {
        return IS_PAUSED;
    }

    public static boolean isInCountdown() {
        return IS_IN_COUNTDOWN;
    }

    private void startRecording() {
        if(TokenActivity.hasToken()) {
            initVariables();

            BEFORE_START_OPTION.start();

            IS_IN_COUNTDOWN = true;

            requestUIsUpdate(null);

            new CountdownHelper(
                KSETTINGS,
                () -> {
                    initRecorders();

                    SCREEN_MIC_RECORDER.start();

                    INTERNAL_AUDIO_RECORDER.start();

                    KTIMER.start();

                    STOP_OPTION.start();

                    IS_RECORDING = true;
                    IS_PAUSED = false;
                    IS_PROCESSING = false;

                    IS_IN_COUNTDOWN = false;

                    requestUIsUpdate(null);
                }
            ).start();
        } else {
            TokenActivity.requestToken(this);
        }
    }

    private void stopRecording() {
        if(!TokenActivity.isToRecycle(this)) {
            TokenActivity.clearToken();
        }

        STOP_OPTION.destroy();

        NOTIFICATION_CAPTURING.destroy();

        BEFORE_START_OPTION.destroy();

        stopForeground(STOP_FOREGROUND_REMOVE);

        IS_RECORDING = false;
        IS_PAUSED = false;
        IS_PROCESSING = true;

        KTIMER.stop();

        requestUIsProcessing();

        SCREEN_MIC_RECORDER.stop();

        INTERNAL_AUDIO_RECORDER.stop();

        KMediaProjection.destroy();

        processAndSave(() -> {
            SCREEN_MIC_RECORDER.destroy();

            INTERNAL_AUDIO_RECORDER.destroy();

            IS_PROCESSING = false;

            requestUIsUpdate(KAPTURE);

            if(Utils.hasWriteSecureSettings(this)) {
                disableSelf();
            }
        });
    }

    private void pauseRecording() {
        IS_PAUSED = true;

        KTIMER.pause();

        SCREEN_MIC_RECORDER.pause();
        INTERNAL_AUDIO_RECORDER.pause();

        requestUIsStateChange();
    }

    private void resumeRecording() {
        IS_PAUSED = false;

        KTIMER.resume();

        SCREEN_MIC_RECORDER.resume();
        INTERNAL_AUDIO_RECORDER.resume();

        requestUIsStateChange();
    }

    private void initVariables() {
        NOTIFICATION_CAPTURING = new CapturingNotification(this);

        KSETTINGS = new KSettings(this);

        SCREEN_MIC_RECORDER = new ScreenMicRecorder(this, KSETTINGS);

        INTERNAL_AUDIO_RECORDER = new InternalAudioRecorder(this, KSETTINGS);

        STOP_OPTION = new StopOption(this, KSETTINGS, this::stopRecording);

        BEFORE_START_OPTION = new BeforeStartOption(this, KSETTINGS);

        KAPTURE = new Kapture(this);
        KAPTURE.setFrom(Kapture.FROM_WATCH);

        KAPTURE.setProfileId(KProfile.getActiveProfileName(this));

        KTIMER = new KTimer();
    }

    private void initRecorders() {
        startForeground(Constants.Notification.Id.CAPTURING, NOTIFICATION_CAPTURING.create());

        KMediaProjection.generate(this);

        SCREEN_MIC_RECORDER.init();

        INTERNAL_AUDIO_RECORDER.init();
    }

    private void requestUIsProcessing() {
        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().requestUIUpdate();
        }

        FullTile.requestUpdate(this);

        Toast.makeText(this, getString(R.string.notification_processing_message), Toast.LENGTH_SHORT).show();
    }

    private void requestUIsStateChange() {
        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().requestUIUpdate();
        }

        FullTile.requestUpdate(this);
    }

    private void requestUIsUpdate(Kapture kapture) {
        FullTile.requestUpdate(this);

        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().requestUiUpdate(kapture);
        }
    }

    private void processAndSave(Runnable onComplete) {
        final File kaptureFile = KFile.generateNewEmptyKaptureFile(this, KSETTINGS);

        KAPTURE.setFile(kaptureFile);

        if(KSETTINGS.isToRecordInternalAudio()) {
            try {
                KFile.combineAudioAndVideo(
                    this,
                    INTERNAL_AUDIO_RECORDER.getFile(),
                    SCREEN_MIC_RECORDER.getFile(),
                    kaptureFile,
                    () -> processAndSaveHelper(onComplete),
                    () -> {
                        IS_PROCESSING = false;
                    }
                );

                return;
            } catch (Exception ignore) {}
        }

        KFile.copyFile(SCREEN_MIC_RECORDER.getFile(), kaptureFile);

        processAndSaveHelper(onComplete);
    }

    private void processAndSaveHelper(Runnable onComplete) {
        KAPTURE.notifyAllMediaScanner();

        new DB(this).insertKapture(KAPTURE);

        onComplete.run();
    }
}