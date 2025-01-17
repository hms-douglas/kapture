package dev.dect.kapture.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFmpegKit;

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

        OVERLAY_UI.destroy();

        BEFORE_START_OPTION.destroy();

        stopForeground(STOP_FOREGROUND_REMOVE);

        IS_RECORDING = false;
        IS_PAUSED = false;
        IS_PROCESSING = true;

        requestUIsProcessing();

        SCREEN_MIC_RECORDER.stop();

        INTERNAL_AUDIO_RECORDER.stop();

        KMediaProjection.destroy();

        new Thread(() -> {
            processAndSave();

            SCREEN_MIC_RECORDER.destroy();

            INTERNAL_AUDIO_RECORDER.destroy();

            NOTIFICATION_PROCESSING.destroy();

            new CapturedNotification(this).createAndShow(KAPTURE);

            IS_PROCESSING = false;

            requestUIsUpdate(KAPTURE);

            if(Utils.hasWriteSecureSettings(this)) {
                disableSelf();
            }
        }).start();
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
    }

    private void initRecorders() {
        startForeground(Constants.Notification.Id.CAPTURING, NOTIFICATION_CAPTURING.create());

        KMediaProjection.generate(this);

        SCREEN_MIC_RECORDER.init();

        INTERNAL_AUDIO_RECORDER.init();
    }

    private void requestUIsProcessing() {
        Toast.makeText(this, getString(R.string.notificationprocessing_message), Toast.LENGTH_SHORT).show();

        NOTIFICATION_PROCESSING.createAndShow();

        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().getKaptureFragment().requestFloatingButtonUpdate();
        }

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

    public void processAndSave() {
        final File videoMicFile = SCREEN_MIC_RECORDER.getFile(),
                   internalAudioFile = INTERNAL_AUDIO_RECORDER.getFile(),
                   kaptureFile =  KFile.generateNewEmptyKaptureFile(this, KSETTINGS);

        KAPTURE.setFile(kaptureFile);

        if(KSETTINGS.isToRecordInternalAudio()) {
            if(KSETTINGS.isToRecordMic()) {
                if(KSETTINGS.isToBoostMicVolume()) {
                    FFmpegKit.execute(
                "-i \""
                        + videoMicFile.getAbsolutePath()
                        + "\" -i \""
                        + internalAudioFile.getAbsolutePath()
                        + "\" -filter_complex \"[0:a]volume=volume=" + KSETTINGS.getMicBoostVolumeFactor() + "[out0];[out0][1:a]amix=duration=first[out1]\" -map \"[out1]\" -map \"0:v\" -c:v copy -c:a aac \""
                        + kaptureFile.getAbsolutePath() + "\""
                    );
                } else {
                    FFmpegKit.execute(
                "-i \""
                        + videoMicFile.getAbsolutePath()
                        + "\" -i \""
                        + internalAudioFile.getAbsolutePath()
                        + "\" -filter_complex \"[0:a][1:a]amix=duration=first[a]\" -map 0:v -map \"[a]\" -c:v copy -c:a aac \""
                        + kaptureFile.getAbsolutePath() + "\""
                    );
                }
            } else {
                FFmpegKit.execute(
            "-i \""
                    + videoMicFile.getAbsolutePath()
                    + "\" -i \""
                    + internalAudioFile.getAbsolutePath()
                    + "\" -c:v copy -c:a aac \""
                    + kaptureFile.getAbsolutePath() + "\""
                );
            }
        } else if(KSETTINGS.isToRecordMic() && KSETTINGS.isToBoostMicVolume()) {
            FFmpegKit.execute(
        "-i \""
                + videoMicFile.getAbsolutePath()
                + "\" -filter_complex \"[0:a]volume=volume=" + KSETTINGS.getMicBoostVolumeFactor() + "[out0]\" -map \"[out0]\" -map \"0:v\" -c:v copy \""
                + kaptureFile.getAbsolutePath() + "\""
            );
        } else {
            KFile.copyFile(videoMicFile, kaptureFile);
        }

        final String kaptureFileName = KFile.getDefaultKaptureFileName(this),
                     audioFileName = kaptureFile.getName().replaceAll(KSETTINGS.getVideoFileFormat(), KSETTINGS.getAudioFileFormat());

        if(KSETTINGS.isToGenerateMp3Audio()) {
            final File f = new File(
            KSETTINGS.getSavingLocationFile(),
            audioFileName.replaceAll(kaptureFileName, Objects.requireNonNull(Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_MP3_AUDIO)))
            );

            if(KSETTINGS.isToRecordMic()) {
                FFmpegKit.execute(
            "-i \""
                    + kaptureFile.getAbsolutePath()
                    + "\" -map 0:a \""
                    + f.getAbsolutePath() + "\""
                );
            } else if(KSETTINGS.isToRecordInternalAudio()) {
                KFile.copyFile(internalAudioFile, f);
            }

            KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_MP3_AUDIO, f));
        }

        if(KSETTINGS.isToGenerateMp3OnlyInternal() && KSETTINGS.isToRecordInternalAudio()) {
            final File f = new File(
                KSETTINGS.getSavingLocationFile(),
                audioFileName.replaceAll(kaptureFileName, Objects.requireNonNull(Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_MP3_INTERNAL_ONLY)))
            );

            KFile.copyFile(internalAudioFile, f);

            KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_MP3_INTERNAL_ONLY, f));
        }

        if(KSETTINGS.isToGenerateMp3OnlyMic() && KSETTINGS.isToRecordMic()) {
            final File f = new File(
                KSETTINGS.getSavingLocationFile(),
                audioFileName.replaceAll(kaptureFileName, Objects.requireNonNull(Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_MP3_MIC_ONLY)))
            );

            FFmpegKit.execute(
        "-i \""
                + videoMicFile.getAbsolutePath()
                + "\" -map 0:a \""
                + f.getAbsolutePath() + "\""
            );

            KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_MP3_MIC_ONLY, f));
        }

        if(KSETTINGS.isToGenerateMp4NoAudio()) {
            final File f = new File(
                KSETTINGS.getSavingLocationFile(),
                kaptureFile.getName().replaceAll(kaptureFileName, kaptureFileName + KFile.FILE_SEPARATOR + Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_MP4_NO_AUDIO))
            );

            if(KSETTINGS.isToRecordMic()) {
                FFmpegKit.execute(
            "-i \""
                    + videoMicFile.getAbsolutePath()
                    + "\" -c copy -an \""
                    + f.getAbsolutePath() + "\""
                );
            } else if(KSETTINGS.isToRecordInternalAudio()){
                KFile.copyFile(videoMicFile, f);
            }

            KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_MP4_NO_AUDIO, f));
        }

        if(KSETTINGS.isToRecordMic() && KSETTINGS.isToRecordInternalAudio()) {
            if(KSETTINGS.isToGenerateMp4OnlyMicAudio()) {
                final File f = new File(
                    KSETTINGS.getSavingLocationFile(),
                    kaptureFile.getName().replaceAll(kaptureFileName, kaptureFileName + KFile.FILE_SEPARATOR + Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_MP4_MIC_ONLY))
                );

                KFile.copyFile(videoMicFile, f);

                KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_MP4_MIC_ONLY, f));
            }

            if(KSETTINGS.isToGenerateMp4OnlyInternalAudio()) {
                final File f = new File(
                    KSETTINGS.getSavingLocationFile(),
                    kaptureFile.getName().replaceAll(kaptureFileName, kaptureFileName + KFile.FILE_SEPARATOR +Kapture.Extra.getFileNameComplementByType(this, Kapture.Extra.EXTRA_MP4_INTERNAL_ONLY))
                );

                FFmpegKit.execute(
            "-i \""
                    + videoMicFile.getAbsolutePath()
                    + "\" -i \""
                    + internalAudioFile.getAbsolutePath()
                    + "\" -c copy -c:a aac \""
                    +  f.getAbsolutePath() + "\""
                );

                KAPTURE.addExtra(new Kapture.Extra(Kapture.Extra.EXTRA_MP4_INTERNAL_ONLY, f));
            }
        }

        KAPTURE.notifyAllMediaScanner();

        new DB(this).insertKapture(KAPTURE);
    }
}












