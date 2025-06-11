package dev.dect.kapture.downloader;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import dev.dect.kapture.data.Constants;

public class VersionsDownloader extends Thread {
    public interface VersionsDownloaderListener {
        void onResult(JSONArray json);
    }

    private final VersionsDownloaderListener LISTENER;

    public VersionsDownloader(VersionsDownloaderListener l) {
        this.LISTENER = l;
    }

    @Override
    public void run() {
        String data = "";

        try {
            final URL url = new URL(Constants.Url.App.WATCH_VERSIONS_FILE);

            final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            final InputStream inputStream = httpURLConnection.getInputStream();

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while((line = bufferedReader.readLine()) != null) {
                data += line;
            }

            this.LISTENER.onResult(new JSONArray(data));
        } catch (IOException | JSONException e) {
            this.LISTENER.onResult(null);
        }
    }
}