package dev.dect.kapture.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.utils.Utils;

@SuppressLint("SetTextI18n")
public class AboutActivity extends AppCompatActivity {

    private AppCompatButton BUTTON_UPDATE;

    private String LATEST_VERSION;

    private TextView LATEST_VERSION_EL;

    private LottieAnimationView LOADER_EL;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        setContentView(R.layout.activity_about);

        initVariables();

        initListeners();

        init();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(BUTTON_UPDATE.getVisibility() == View.GONE) {
            LATEST_VERSION_EL.setVisibility(View.GONE);

            LOADER_EL.setVisibility(View.VISIBLE);

            checkForUpdates();
        }
    }

    private void initVariables() {
        BUTTON_UPDATE = findViewById(R.id.btnUpdate);

        LATEST_VERSION_EL = findViewById(R.id.latest);

        LOADER_EL = findViewById(R.id.loader);
    }

    private void initListeners() {
        findViewById(R.id.btnBack).setOnClickListener((v) -> finish());

        findViewById(R.id.btnInfo).setOnClickListener((v) -> Utils.ExternalActivity.requestSettings(this));

        findViewById(R.id.btnCredits).setOnClickListener((v) -> startActivity(new Intent(this, CreditsActivity.class)));

        findViewById(R.id.btnGithub).setOnClickListener((v) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GITHUB_REPOSITORY_URL))));

        BUTTON_UPDATE.setOnClickListener((v) -> {
            final DialogPopup updateDialog = new DialogPopup(
                this,
                R.string.about_btn_update,
                R.string.about_btn_update_description,
                R.string.popup_btn_download,
                () -> {
                    try {
                        startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                            Uri.parse(Constants.GITHUB_PROJECT_DOWNLOAD_URL + LATEST_VERSION + ".apk")
                            )
                        );
                    } catch (Exception ignore) {
                        Toast.makeText(this, getString(R.string.toast_error_download), Toast.LENGTH_SHORT).show();
                    }
                },
                R.string.popup_btn_cancel,
                null,
                true,
                false,
                false
            );

            updateDialog.show();
        });
    }

    private void init() {
        try {
            ((TextView) findViewById(R.id.version)).setText(getString(R.string.about_version) + " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            ((TextView) findViewById(R.id.version)).setText(getString(R.string.about_version_unknown));
        }

        checkForUpdates();
    }

    private void checkForUpdates() {
        new CheckUpdate((data) -> new Handler(Looper.getMainLooper()).post(() -> {
            if(data == null) {
                Toast.makeText(this, getString(R.string.toast_error_check_update), Toast.LENGTH_SHORT).show();

                LOADER_EL.setVisibility(View.GONE);

                return;
            }

            try {
                if(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode < data.getInt(Constants.GITHUB_PROJECT_LATEST_VERSION_TAG_VERSION_CODE)) {
                    LATEST_VERSION = data.getString(Constants.GITHUB_PROJECT_LATEST_VERSION_TAG_VERSION_NAME);

                    LATEST_VERSION_EL.setText(getString(R.string.about_version_available) + " " + LATEST_VERSION);

                    BUTTON_UPDATE.setVisibility(View.VISIBLE);
                }

                LATEST_VERSION_EL.setVisibility(View.VISIBLE);
            } catch (Exception ignore) {
                Toast.makeText(this, getString(R.string.toast_error_check_update), Toast.LENGTH_SHORT).show();
            }

            LOADER_EL.setVisibility(View.GONE);
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
                final URL url = new URL(Constants.GITHUB_PROJECT_LATEST_VERSION_URL);

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
