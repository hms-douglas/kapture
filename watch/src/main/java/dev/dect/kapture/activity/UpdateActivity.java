package dev.dect.kapture.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.wear.widget.CurvedTextView;

import com.google.android.gms.wearable.Node;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.utils.Utils;

/** @noinspection deprecation*/
@SuppressLint("SetTextI18n")
public class UpdateActivity extends Activity {
    private CurvedTextView CLOCK_EL;

    private final Node[] PHONE_NODE = new Node[1];

    private final BroadcastReceiver CLOCK_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateClock();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_update);

        initVariables();

        init();
    }

    @Override
    public void onResume() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);

        registerReceiver(CLOCK_RECEIVER, filter);

        updateClock();

        super.onResume();
    }

    @Override
    public void onPause() {
        unregisterReceiver(CLOCK_RECEIVER);

        super.onPause();
    }

    private void initVariables() {
        CLOCK_EL = findViewById(R.id.clock);

        Utils.Remote.getPhoneNodeAsync(this, PHONE_NODE);
    }

    private void init() {
        updateClock();

        checkForUpdates();
    }

    private void updateClock() {
        CLOCK_EL.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
    }

    private void checkForUpdates() {
        new CheckUpdate((data) -> new Handler(Looper.getMainLooper()).post(() -> {
            if(data == null) {
                Toast.makeText(this, getString(R.string.toast_error_check_update), Toast.LENGTH_SHORT).show();

                finish();

                return;
            }

            try {
                findViewById(R.id.loader).setVisibility(View.GONE);

                if(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode < data.getInt(Constants.Url.App.KeyTag.LATEST_VERSION_VERSION_CODE)) {
                    findViewById(R.id.update).setVisibility(View.VISIBLE);

                    ((TextView) findViewById(R.id.updateVersion)).setText(getString(R.string.about_version_available) + " " + data.getString(Constants.Url.App.KeyTag.LATEST_VERSION_VERSION_NAME));

                    findViewById(R.id.buttonPhone).setOnClickListener((v) -> {
                        try {
                            Utils.Remote.openLinkOnPhone(this, PHONE_NODE, data.getString(Constants.Url.App.KeyTag.LATEST_VERSION_LINK));
                        } catch (JSONException e) {
                            Toast.makeText(this, getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    findViewById(R.id.updated).setVisibility(View.VISIBLE);
                }
            } catch (Exception ignore) {
                Toast.makeText(this, getString(R.string.toast_error_check_update), Toast.LENGTH_SHORT).show();
            }
        })).start();
    }

    private static class CheckUpdate extends Thread {
        public interface CheckUpdateListener {
            void onResult(JSONObject json);
        }

        private final CheckUpdateListener LISTENER;

        public CheckUpdate(CheckUpdateListener l) {
            this.LISTENER = l;
        }

        @Override
        public void run() {
            String data = "";

            try {
                final URL url = new URL(Constants.Url.App.LATEST_VERSION_FILE_WATCH);

                final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                final InputStream inputStream = httpURLConnection.getInputStream();

                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = bufferedReader.readLine()) != null) {
                    data += line;
                }

                this.LISTENER.onResult(new JSONObject(data));

            } catch (IOException | JSONException ignore) {
                this.LISTENER.onResult(null);
            }
        }
    }
}