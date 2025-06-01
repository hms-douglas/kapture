package dev.dect.kapture.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.notification.CapturedNotification;
import dev.dect.kapture.notification.ProcessingNotification;
import dev.dect.kapture.overlay.CountdownOverlay;
import dev.dect.kapture.recorder.ScreenMicRecorder;
import dev.dect.kapture.recorder.utils.BeforeStartOption;
import dev.dect.kapture.recorder.utils.StopOption;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.notification.CapturingNotification;
import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.activity.TokenActivity;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.recorder.InternalAudioRecorder;
import dev.dect.kapture.overlay.Overlay;
import dev.dect.kapture.utils.KMediaProjection;
import dev.dect.kapture.utils.KProfile;
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

    private ProcessingNotification NOTIFICATION_PROCESSING;

    private ScreenMicRecorder SCREEN_MIC_RECORDER;

    private InternalAudioRecorder INTERNAL_AUDIO_RECORDER;

    private Overlay OVERLAY_UI;

    private KSettings KSETTINGS;

    private StopOption STOP_OPTION;

    private BeforeStartOption BEFORE_START_OPTION;

    private Kapture KAPTURE;

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

    public static void screenshotTaken(Kapture.Screenshot screenshot) {
        if(CAPTURING_SERVICE.KAPTURE != null) {
            CAPTURING_SERVICE.KAPTURE.addScreenshot(screenshot);
        }
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

            new CountdownOverlay(
                this,
                KSETTINGS,
                () -> {
                    OVERLAY_UI.render();

                    initRecorders();

                    SCREEN_MIC_RECORDER.start();

                    INTERNAL_AUDIO_RECORDER.start();

                    OVERLAY_UI.setMediaRecorderSurface(SCREEN_MIC_RECORDER.getSurface());

                    STOP_OPTION.start();

                    IS_RECORDING = true;
                    IS_PAUSED = false;
                    IS_PROCESSING = false;

                    IS_IN_COUNTDOWN = false;

                    requestUIsUpdate(null);
                }
            ).renderAndStart();
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

        new Handler(Looper.getMainLooper()).post(() -> OVERLAY_UI.destroy());

        BEFORE_START_OPTION.destroy();

        stopForeground(STOP_FOREGROUND_REMOVE);

        IS_RECORDING = false;
        IS_PAUSED = false;
        IS_PROCESSING = true;

        requestUIsProcessing();

        SCREEN_MIC_RECORDER.stop();

        INTERNAL_AUDIO_RECORDER.stop();

        KMediaProjection.destroy();

        processAndSave(() -> {
            SCREEN_MIC_RECORDER.destroy();

            INTERNAL_AUDIO_RECORDER.destroy();

            NOTIFICATION_PROCESSING.destroy();

            new CapturedNotification(this).createAndShow(KAPTURE);

            IS_PROCESSING = false;

            requestUIsUpdate(KAPTURE);

            if(Utils.hasWriteSecureSettings(this)) {
                disableSelf();
            }
        });
    }

    private void pauseRecording() {
        IS_PAUSED = true;

        SCREEN_MIC_RECORDER.pause();
        INTERNAL_AUDIO_RECORDER.pause();

        NOTIFICATION_CAPTURING.refreshRecordingState();

        OVERLAY_UI.refreshRecordingState();

        requestUIsStateChange();
    }

    private void resumeRecording() {
        IS_PAUSED = false;

        SCREEN_MIC_RECORDER.resume();
        INTERNAL_AUDIO_RECORDER.resume();

        NOTIFICATION_CAPTURING.refreshRecordingState();

        OVERLAY_UI.refreshRecordingState();

        requestUIsStateChange();
    }

    private void initVariables() {
        NOTIFICATION_CAPTURING = new CapturingNotification(this);

        NOTIFICATION_PROCESSING = new ProcessingNotification(this);

        KSETTINGS = new KSettings(this);

        SCREEN_MIC_RECORDER = new ScreenMicRecorder(this, KSETTINGS);

        INTERNAL_AUDIO_RECORDER = new InternalAudioRecorder(this, KSETTINGS);

        OVERLAY_UI = new Overlay(this, KSETTINGS);

        STOP_OPTION = new StopOption(this, KSETTINGS, this::stopRecording);

        BEFORE_START_OPTION = new BeforeStartOption(this, KSETTINGS);

        KAPTURE = new Kapture(this);

        KAPTURE.setProfileId(KProfile.getActiveProfileName(this));
    }

    private void initRecorders() {
        startForeground(Constants.Notification.Id.CAPTURING, NOTIFICATION_CAPTURING.create());

        KMediaProjection.generate(this);

        SCREEN_MIC_RECORDER.init();

        INTERNAL_AUDIO_RECORDER.init();
    }

    private void requestUIsProcessing() {
        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().getKaptureFragment().requestFloatingButtonUpdate();
        }

        Toast.makeText(this, getString(R.string.notificationprocessing_message), Toast.LENGTH_SHORT).show();

        NOTIFICATION_PROCESSING.createAndShow();

        QuickTileCapturingService.requestUiUpdate(this);
    }

    private void requestUIsStateChange() {
        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().getKaptureFragment().requestFloatingButtonUpdate();
        }

        Utils.Widget.updateWidgetsCapturingBtns(this);
    }

    private void requestUIsUpdate(Kapture kapture) {
        QuickTileCapturingService.requestUiUpdate(this);

        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().requestUiUpdate(kapture);
        }

        Utils.Widget.updateWidgetsCapturingBtns(this);
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
                    () -> processAndSaveHelper(kaptureFile, onComplete)
                );
            } catch (Exception ignore) {
                KFile.copyFile(SCREEN_MIC_RECORDER.getFile(), kaptureFile);

                if(!KSETTINGS.isToGenerateAudio_OnlyInternal()) {
                    final File helper = new File(KSETTINGS.getSavingLocationFile(), kaptureFile.getName().replaceAll("." + Constants.EXT_VIDEO_FORMAT, "") + "." + Constants.EXT_AUDIO_FORMAT);

                    try {
                        KFile.copyFile(INTERNAL_AUDIO_RECORDER.getFile(), helper);

                        KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_AUDIO_INTERNAL_ONLY, helper));

                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, getString(R.string.toast_error_merging_2), Toast.LENGTH_SHORT).show());
                    } catch (Exception ignore2) {
                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, getString(R.string.toast_error_merging_1), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, getString(R.string.toast_error_merging_1), Toast.LENGTH_SHORT).show());
                }

                processAndSaveHelper(kaptureFile, onComplete);
            }
        } else {
            KFile.copyFile(SCREEN_MIC_RECORDER.getFile(), kaptureFile);

            processAndSaveHelper(kaptureFile, onComplete);
        }
    }

    private void processAndSaveHelper(File kaptureFile, Runnable onComplete) {
        processExtras(kaptureFile);

        KAPTURE.notifyAllMediaScanner();

        new DB(this).insertKapture(KAPTURE);

        onComplete.run();
    }

    private void processExtras(File kaptureFile) {
        final String kaptureFileName = KFile.getDefaultKaptureFileName(this),
                     audioFileName = kaptureFile.getName().replaceAll(Constants.EXT_VIDEO_FORMAT, Constants.EXT_AUDIO_FORMAT);

        if(KSETTINGS.isToGenerateAudio_Audio()) {
            final File f = new File(
                KSETTINGS.getSavingLocationFile(),
                audioFileName.replaceAll(kaptureFileName, Objects.requireNonNull(Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_AUDIO_AUDIO)))
            );

            boolean noError = true;

            if(KSETTINGS.isToRecordMic()) {
                KFile.extractAudioFromVideo(kaptureFile, f);
            } else if(KSETTINGS.isToRecordInternalAudio()) {
                try {
                    KFile.copyFile(INTERNAL_AUDIO_RECORDER.getFile(), f);
                } catch (Exception ignore) {
                    noError = false;
                }
            }

            if(noError && (KSETTINGS.isToRecordMic() || KSETTINGS.isToRecordInternalAudio())) {
                KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_AUDIO_AUDIO, f));
            }
        }

        if(KSETTINGS.isToGenerateAudio_OnlyInternal() && KSETTINGS.isToRecordInternalAudio()) {
            final File f = new File(
                KSETTINGS.getSavingLocationFile(),
                audioFileName.replaceAll(kaptureFileName, Objects.requireNonNull(Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_AUDIO_INTERNAL_ONLY)))
            );

            try {
                KFile.copyFile(INTERNAL_AUDIO_RECORDER.getFile(), f);

                KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_AUDIO_INTERNAL_ONLY, f));
            } catch (Exception ignore) {}
        }

        if(KSETTINGS.isToGenerateAudio_OnlyMic() && KSETTINGS.isToRecordMic()) {
            final File f = new File(
                KSETTINGS.getSavingLocationFile(),
                audioFileName.replaceAll(kaptureFileName, Objects.requireNonNull(Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_AUDIO_MIC_ONLY)))
            );

            KFile.extractAudioFromVideo(SCREEN_MIC_RECORDER.getFile(), f);

            KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_AUDIO_MIC_ONLY, f));
        }

        if(KSETTINGS.isToGenerateVideo_NoAudio()) {
            final File f = new File(
                KSETTINGS.getSavingLocationFile(),
                kaptureFile.getName().replaceAll(kaptureFileName, kaptureFileName + KFile.FILE_SEPARATOR + Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_VIDEO_NO_AUDIO))
            );

            if(KSETTINGS.isToRecordMic()) {
                KFile.removeAudioFromVideo(SCREEN_MIC_RECORDER.getFile(), f);
            } else if(KSETTINGS.isToRecordInternalAudio()){
                KFile.copyFile(SCREEN_MIC_RECORDER.getFile(), f);
            }

            KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_VIDEO_NO_AUDIO, f));
        }

        if(KSETTINGS.isToRecordMic() && KSETTINGS.isToRecordInternalAudio()) {
            if(KSETTINGS.isToGenerateVideo_OnlyMicAudio()) {
                final File f = new File(
                    KSETTINGS.getSavingLocationFile(),
                    kaptureFile.getName().replaceAll(kaptureFileName, kaptureFileName + KFile.FILE_SEPARATOR + Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_VIDEO_MIC_ONLY))
                );

                KFile.copyFile(SCREEN_MIC_RECORDER.getFile(), f);

                KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_VIDEO_MIC_ONLY, f));
            }

            if(KSETTINGS.isToGenerateVideo_OnlyInternalAudio()) {
                final File f = new File(
                    KSETTINGS.getSavingLocationFile(),
                    kaptureFile.getName().replaceAll(kaptureFileName, kaptureFileName + KFile.FILE_SEPARATOR +Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_VIDEO_INTERNAL_ONLY))
                );

                KFile.removeAudioFromVideo(SCREEN_MIC_RECORDER.getFile(), f);

                try {
                    KFile.combineAudioAndVideo(this, INTERNAL_AUDIO_RECORDER.getFile(), f, f, null);

                    KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_VIDEO_INTERNAL_ONLY, f));
                } catch (Exception ignore) {}
            }
        }
    }
}












