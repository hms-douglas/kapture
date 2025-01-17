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
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.fragment.SettingsFragment;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.ExtraPopup;
import dev.dect.kapture.server.WifiShare;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KFile;
import dev.dect.kapture.utils.KProfile;

/** @noinspection ResultOfMethodCallIgnored*/
public class ActionActivity extends AppCompatActivity {
    public static String INTENT_KAPTURE_ID = "id",
                         INTENT_ACTION = "action",
                         INTENT_NOTIFICATION_ID = "not_id",
                         INTENT_FROM = "from",
                         INTENT_PROFILE_NAME = "name";

    public static final int FROM_APP = 0,
                            FROM_NOTIFICATION = 1,
                            FROM_QUICK_TILE = 2,
                            FROM_WIDGET = 3;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        init();
    }

    private void init() {
        SettingsFragment.setTheme(this, KSharedPreferences.getAppSp(this).getInt(Constants.Sp.App.APP_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), false);

        if(getIntent().getExtras() == null) {
            finish();
        } else {
            switch(getIntent().getIntExtra(INTENT_FROM, FROM_APP)) {
                case FROM_NOTIFICATION:
                    fromNotification();
                    break;

                case FROM_QUICK_TILE:
                    fromQuickTile();
                    break;

                case FROM_WIDGET:
                    fromWidget();
                    break;

                default:
                    fromApp();
                    break;
            }
        }
    }

    private void fromNotification() {
        final Kapture kapture = new DB(this).selectKapture(getIntent().getExtras().getLong(INTENT_KAPTURE_ID));

        switch (getIntent().getExtras().getString(INTENT_ACTION, null)) {
            case Constants.Notification.Action.EXTRA:
                new ExtraPopup(this, kapture.getExtras(), this::finish).show();
                break;

            case Constants.Notification.Action.SHARE:
                KFile.shareFile(this, kapture.getLocation());
                finish();
                break;

            case Constants.Notification.Action.DELETE:
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

                            if(getIntent().hasExtra(INTENT_NOTIFICATION_ID)) {
                                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(getIntent().getIntExtra(INTENT_NOTIFICATION_ID, -1));
                            }

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
    }

    private void fromQuickTile() {
        switch(getIntent().getExtras().getString(INTENT_ACTION, null)) {
            case Constants.QuickTile.Action.WIFI_SHARE:
                new WifiShare(this).setListener(this::finish).start();
                break;

            default:
                finish();
                break;
        }
    }

    private void fromWidget() {
        switch(getIntent().getExtras().getString(INTENT_ACTION, null)) {
            case Constants.Widget.Action.START:
                CapturingService.requestStartRecording(this);
                finish();
                break;

            case Constants.Widget.Action.STOP:
                CapturingService.requestStopRecording();
                finish();
                break;

            case Constants.Widget.Action.PAUSE:
                CapturingService.requestPauseRecording();
                finish();
                break;

            case Constants.Widget.Action.RESUME:
                CapturingService.requestResumeRecording();
                finish();
                break;

            case Constants.Widget.Action.WIFI_SHARE:
                new WifiShare(this).setListener(this::finish).start();
                break;

            case Constants.Widget.Action.SET_PROFILE:
                KProfile.setActiveProfile(this, getIntent().getStringExtra(INTENT_PROFILE_NAME), false);
                finish();
                break;

            default:
                finish();
                break;
        }
    }

    private void fromApp() {}
}
