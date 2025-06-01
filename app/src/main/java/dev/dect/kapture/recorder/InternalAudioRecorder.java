package dev.dect.kapture.recorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.KMediaProjection;

/** @noinspection ResultOfMethodCallIgnored*/
@SuppressLint("MissingPermission")
public class InternalAudioRecorder {
    private final Context CONTEXT;

    private final KSettings KSETTINGS;

    private final int BUFFER_SIZE = 1024,
                      BYTES_PER_EL = 2;

    private AudioRecord AUDIO_RECORDER;

    private boolean IS_RECORDING_INTERNAL_AUDIO = false,
                    HAS_WAV_FILE = false;

    private File TEMP_PCM_FILE,
                 TEMP_WAV_FILE;

    private Thread RECORDING_THREAD;

    public InternalAudioRecorder(Context ctx, KSettings rs) {
        this.CONTEXT = ctx;
        this.KSETTINGS = rs;

        createTempFile();
    }

    public void init() {
        if(!KSETTINGS.isToRecordInternalAudio()) {
            return;
        }

        final AudioPlaybackCaptureConfiguration config = new AudioPlaybackCaptureConfiguration.Builder(KMediaProjection.get())
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_GAME)
            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
            .build();

        final AudioFormat format = new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(KSETTINGS.getAudioSampleRate())
            .setChannelMask(KSETTINGS.isToRecordInternalAudioInStereo() ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_MONO)
            .build();

        AUDIO_RECORDER = new AudioRecord.Builder()
            .setAudioFormat(format)
            .setBufferSizeInBytes(BUFFER_SIZE * BYTES_PER_EL)
            .setAudioPlaybackCaptureConfig(config)
            .build();

        createWritingThread();
    }

    public void start(){
        if(!KSETTINGS.isToRecordInternalAudio()) {
            return;
        }

        AUDIO_RECORDER.startRecording();

        IS_RECORDING_INTERNAL_AUDIO = true;

        RECORDING_THREAD.start();
    }

    public void pause() {
        IS_RECORDING_INTERNAL_AUDIO = false;

        RECORDING_THREAD = null;
    }

    public void resume() {
        IS_RECORDING_INTERNAL_AUDIO = true;

        createWritingThread();

        RECORDING_THREAD.start();
    }

    public void stop() {
        if(!KSETTINGS.isToRecordInternalAudio()) {
            return;
        }

        IS_RECORDING_INTERNAL_AUDIO = false;

        AUDIO_RECORDER.stop();
        AUDIO_RECORDER.release();
    }

    public void destroy() {
        AUDIO_RECORDER = null;
        RECORDING_THREAD = null;

        TEMP_PCM_FILE.delete();
        TEMP_WAV_FILE.delete();
    }

    public File getFile() throws IOException {
        if(!HAS_WAV_FILE) {
            try {
                KFile.pcmToWav(TEMP_PCM_FILE, TEMP_WAV_FILE, KSETTINGS);

                HAS_WAV_FILE = true;
            } catch(Exception e) {
                throw e;
            }
        }

        return TEMP_WAV_FILE;
    }

    private void createTempFile() {
        try {
            final String name = InternalAudioRecorder.class.getSimpleName() + new Date().getTime();

            TEMP_PCM_FILE = File.createTempFile(name, ".pcm");
            TEMP_WAV_FILE = File.createTempFile(name, "." + Constants.EXT_AUDIO_FORMAT);
        } catch (Exception ignore) {}
    }

    private void createWritingThread() {
        RECORDING_THREAD = new Thread(this::writeToRawTempFile, CONTEXT.getPackageName());

        RECORDING_THREAD.setPriority(Thread.MAX_PRIORITY);
    }

    private void writeToRawTempFile() {
        try {
            final FileOutputStream outputStream = new FileOutputStream(TEMP_PCM_FILE.getAbsolutePath(), true);

            final short[] data = new short[BUFFER_SIZE];

            while(IS_RECORDING_INTERNAL_AUDIO) {
                AUDIO_RECORDER.read(data, 0, BUFFER_SIZE);

                final int arraySize = data.length;

                byte[] bytes = new byte[arraySize * 2];

                for(int i = 0; i < arraySize; i++) {
                    bytes[i * 2] = (byte) (data[i] & 0x00FF);

                    bytes[(i * 2) + 1] = (byte) (data[i] >> 8);

                    data[i] = 0;
                }

                outputStream.write(bytes, 0,BUFFER_SIZE * BYTES_PER_EL);
            }

            outputStream.close();
        } catch (IOException ignore) {}
    }
}