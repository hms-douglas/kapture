package dev.dect.kapture.recorder;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

import dev.dect.kapture.R;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KMediaProjection;

/** @noinspection ResultOfMethodCallIgnored*/
public class ScreenMicRecorder {
    private MediaRecorder MEDIA_RECORDER;

    private VirtualDisplay VIRTUAL_DISPLAY;

    private final KSettings KSETTINGS;

    private final Context CONTEXT;

    private File TEMP_FILE;

    public ScreenMicRecorder(Context ctx, KSettings rs) {
        this.CONTEXT = ctx;
        this.KSETTINGS = rs;

        createTempFile();
    }

    public void init() {
        if(MEDIA_RECORDER != null) {
            MEDIA_RECORDER.reset();
        }

        try {
            MEDIA_RECORDER = new MediaRecorder(CONTEXT);

            MEDIA_RECORDER.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            if(KSETTINGS.isToRecordMic()) {
                MEDIA_RECORDER.setAudioSource(MediaRecorder.AudioSource.MIC);
            }

            MEDIA_RECORDER.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            MEDIA_RECORDER.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            if(KSETTINGS.isToRecordMic()) {
                MEDIA_RECORDER.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                MEDIA_RECORDER.setAudioEncodingBitRate(KSETTINGS.getMicAudioBitRate());
                MEDIA_RECORDER.setAudioSamplingRate(KSETTINGS.getMicAudioSampleRate());
            }

            MEDIA_RECORDER.setVideoEncodingBitRate(KSETTINGS.getVideoBitRate());
            MEDIA_RECORDER.setVideoFrameRate(KSETTINGS.getVideoFrameRate());
            MEDIA_RECORDER.setVideoSize(KSETTINGS.getVideoWidth(), KSETTINGS.getVideoHeight());

            MEDIA_RECORDER.setOutputFile(TEMP_FILE.getAbsolutePath());

            MEDIA_RECORDER.prepare();
        } catch (Exception ignore) {
            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            CapturingService.requestStopRecording();
        }
    }

    public void start() {
        VIRTUAL_DISPLAY = KMediaProjection.get().createVirtualDisplay(
            CONTEXT.getString(R.string.app_name),
            KSETTINGS.getVideoWidth(),
            KSETTINGS.getVideoHeight(),
            KSETTINGS.getVideoDpi(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            MEDIA_RECORDER.getSurface(),
            null,
            null
        );

        MEDIA_RECORDER.start();
    }

    public void stop() {
        MEDIA_RECORDER.stop();
        MEDIA_RECORDER.reset();

        VIRTUAL_DISPLAY.release();
    }

    public void destroy() {
        MEDIA_RECORDER = null;

        TEMP_FILE.delete();
    }

    public File getFile() {
        return TEMP_FILE;
    }

    private void createTempFile() {
        try {
            TEMP_FILE = File.createTempFile(ScreenMicRecorder.class.getSimpleName() + new Date().getTime(), "." + KSETTINGS.getVideoFileFormat());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
