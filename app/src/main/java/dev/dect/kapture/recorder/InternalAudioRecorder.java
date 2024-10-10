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

import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.KMediaProjection;
import dev.dect.kapture.data.KSettings;

/** @noinspection ResultOfMethodCallIgnored*/
@SuppressLint("MissingPermission")
public class InternalAudioRecorder {
    private final Context CONTEXT;

    private final KSettings KSETTINGS;

    private final int BUFFER_SIZE = 1024,
                      BYTES_PER_EL = 2;

    private AudioRecord AUDIO_RECORDER;

    private boolean IS_RECORDING_INTERNAL_AUDIO = false;

    private File TEMP_PMC_FILE,
                 TEMP_MP3_FILE;

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
                .setSampleRate(KSETTINGS.getInternalAudioSampleRate())
                .setChannelMask(KSETTINGS.isToRecordInternalAudioInStereo() ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_MONO)
                .build();

        AUDIO_RECORDER = new AudioRecord.Builder()
                .setAudioFormat(format)
                .setBufferSizeInBytes(BUFFER_SIZE * BYTES_PER_EL)
                .setAudioPlaybackCaptureConfig(config)
                .build();

        RECORDING_THREAD = new Thread(this::writeToRawTempFile, CONTEXT.getPackageName());

        RECORDING_THREAD.setPriority(Thread.MAX_PRIORITY);
    }

    public void start(){
        if(!KSETTINGS.isToRecordInternalAudio()) {
            return;
        }

        AUDIO_RECORDER.startRecording();

        IS_RECORDING_INTERNAL_AUDIO = true;

        RECORDING_THREAD.start();
    }

    public void stop() {
        if(!KSETTINGS.isToRecordInternalAudio()) {
            return;
        }

        IS_RECORDING_INTERNAL_AUDIO = false;

        AUDIO_RECORDER.stop();
        AUDIO_RECORDER.release();

        KFile.pmcToMp3(TEMP_PMC_FILE, TEMP_MP3_FILE);
    }

    public void destroy() {
        AUDIO_RECORDER = null;
        RECORDING_THREAD = null;

        TEMP_PMC_FILE.delete();
        TEMP_MP3_FILE.delete();
    }

    public File getFile() {
        return TEMP_MP3_FILE;
    }

    private void createTempFile() {
        try {
            final String name = InternalAudioRecorder.class.getSimpleName() + new Date().getTime();

            TEMP_PMC_FILE = File.createTempFile(name, ".pmc");
            TEMP_MP3_FILE = File.createTempFile(name, ".mp3");
        } catch (Exception ignore) {}
    }

    private void writeToRawTempFile(){
        try {
            final FileOutputStream outputStream = new FileOutputStream(TEMP_PMC_FILE.getAbsolutePath());

            final short[] data = new short[BUFFER_SIZE];

            while(IS_RECORDING_INTERNAL_AUDIO) {
                AUDIO_RECORDER.read(data, 0, BUFFER_SIZE);

                outputStream.write(shortToByte(data), 0,BUFFER_SIZE * BYTES_PER_EL);
            }

            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] shortToByte(short[] data) {
        final int arraySize = data.length;

        byte[] bytes = new byte[arraySize * 2];

        for(int i = 0; i < arraySize; i++) {
            bytes[i * 2] = (byte) (data[i] & 0x00FF);

            bytes[(i * 2) + 1] = (byte) (data[i] >> 8);

            data[i] = 0;
        }

        return bytes;
    }
}