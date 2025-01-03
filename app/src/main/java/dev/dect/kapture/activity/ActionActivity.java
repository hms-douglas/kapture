package dev.dect.kapture.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.File;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.fragment.SettingsFragment;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.ExtraPopup;
import dev.dect.kapture.server.WifiShare;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KFile;

/** @noinspection ResultOfMethodCallIgnored*/
public class ActionActivity extends AppCompatActivity {
    public static String INTENT_KAPTURE_ID = "id",
                         INTENT_ACTION = "action";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        init();
    }

    private void init() {
        SettingsFragment.setTheme(this, getSharedPreferences(Constants.SP, MODE_PRIVATE).getInt(Constants.SP_KEY_APP_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), false);

        if(getIntent().getExtras() == null) {
            finish();
        } else {
            if(getIntent().hasExtra(INTENT_KAPTURE_ID)) {
                final Kapture kapture = new DB(this).selectKapture(getIntent().getExtras().getLong(INTENT_KAPTURE_ID));

                switch (getIntent().getExtras().getString(INTENT_ACTION, null)) {
                    case Constants.NOTIFICATION_CAPTURED_ACTION_EXTRA:
                        new ExtraPopup(this, kapture.getExtras(), this::finish).show();
                        break;

                    case Constants.NOTIFICATION_CAPTURED_ACTION_SHARE:
                        KFile.shareFile(this, kapture.getLocation());
                        finish();
                        break;

                    case Constants.NOTIFICATION_CAPTURED_ACTION_DELETE:
                        final File f = new File(kapture.getLocation());

                        new DialogPopup(
                            this,
                            DialogPopup.NO_TEXT,
                            getString(R.string.popup_delete_text_1) + " " + f.getName() + "?",
                            R.string.popup_btn_delete,
                            () -> {
                                new DB(this).deleteKapture(kapture);

                                for(Kapture.Extra extra : kapture.getExtras()) {
                                    new File(extra.getLocation()).delete();
                                }

                                for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {
                                    new File(screenshot.getLocation()).delete();
                                }

                                f.delete();

                                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();

                                finish();
                            },
                            R.string.popup_btn_cancel,
                            this::finish,
                            false,
                            false,
                            true
                        ).show();
                        break;

                    default:
                        finish();
                        break;
                }
            } else {
                switch(getIntent().getExtras().getString(INTENT_ACTION, null)) {
                    case Constants.SHORTCUT_STATIC_ACTION_START:
                        CapturingService.requestStartRecording(this);
                        finish();
                        break;

                    case Constants.SHORTCUT_STATIC_ACTION_STOP:
                        CapturingService.requestStopRecording();
                        finish();
                        break;

                    case Constants.SHORTCUT_STATIC_ACTION_PAUSE:
                        CapturingService.requestPauseRecording();
                        finish();
                        break;

                    case Constants.SHORTCUT_STATIC_ACTION_RESUME:
                        CapturingService.requestResumeRecording();
                        finish();
                        break;

                    case Constants.SHORTCUT_STATIC_ACTION_WIFI_SHARE:
                        new WifiShare(this).setListener(this::finish).start();
                        break;

                    default:
                        finish();
                        break;
                }
            }
        }
    }
}
