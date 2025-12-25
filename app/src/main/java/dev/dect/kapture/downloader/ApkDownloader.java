package dev.dect.kapture.downloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import dev.dect.kapture.R;
import dev.dect.kapture.model.Version;

/** @noinspection ResultOfMethodCallIgnored*/
public class ApkDownloader {
    private final String TAG = ApkDownloader.class.getSimpleName();

    public interface OnApkDownloaderListener {
        default void onProgressChange(float percentage){}

        default void onDownloadComplete(File file){}

        default void onDownloadError(float percentage){}
    }

    private final Context CONTEXT;

    private final Version VERSION;

    private final OnApkDownloaderListener LISTENER;

    private boolean IS_COMPLETE;

    private boolean IS_TO_CANCEL;

    private long LAST_UPDATE;

    public ApkDownloader(Context ctx, Version version, OnApkDownloaderListener l) {
        this.CONTEXT = ctx;
        this.VERSION = version;
        this.LISTENER = l;

        this.IS_COMPLETE = false;
        this.IS_TO_CANCEL = false;
    }

    public boolean isComplete() {
        return IS_COMPLETE;
    }

    public void cancel() {
        this.IS_TO_CANCEL = true;
    }

    public void download() {
        IS_COMPLETE = false;

        if(VERSION.isFile() || !VERSION.getLinkPath().toLowerCase(Locale.ROOT).endsWith("apk")) {
            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).post(() -> LISTENER.onDownloadError(0));

            return;
        }

        final File apk = new File(
            CONTEXT.getCacheDir(),
            VERSION.getFileName()
        );

        final Handler handler = new Handler(Looper.getMainLooper());

        final int[] size = new int[]{0},
                  downloadedSize = new int[]{0};

        new Thread(() -> {
            try {
                LAST_UPDATE = System.currentTimeMillis();

                final URL url = new URL(VERSION.getLinkPath());

                final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                final InputStream inputStream = httpURLConnection.getInputStream();

                size[0] = httpURLConnection.getContentLength();

                final FileOutputStream fileOutputStream = new FileOutputStream(apk);

                final byte[] bytes = new byte[1024];

                int bufferLength;

                while(!IS_TO_CANCEL && (bufferLength = inputStream.read(bytes, 0, 1024)) > 0) {
                    fileOutputStream.write(bytes, 0, bufferLength);

                    downloadedSize[0] += bufferLength;

                    if((System.currentTimeMillis() - LAST_UPDATE) > 500) {
                        LAST_UPDATE = System.currentTimeMillis();

                        handler.post(() -> LISTENER.onProgressChange(calculateProgress(size[0], downloadedSize[0])));
                    }
                }

                inputStream.close();

                fileOutputStream.close();

                if(IS_TO_CANCEL) {
                    if(apk.exists()) {
                        apk.delete();
                    }

                    return;
                }

                IS_COMPLETE = true;

                handler.post(() -> LISTENER.onDownloadComplete(apk));
            } catch (Exception e) {
                Log.e(TAG, "download: " + e.getMessage());

                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

                    LISTENER.onDownloadError(calculateProgress(size[0], downloadedSize[0]));
                });
            }
        }).start();
    }

    private float calculateProgress(int size, int downloaded) {
        return size != 0 ? ((downloaded * 100f) / size) : 0f;
    }
}
