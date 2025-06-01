package dev.dect.kapture.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionConfig;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.R;

public class TokenActivity extends AppCompatActivity {
    private static Intent INTENT_TOKEN;

    private ActivityResultLauncher<Intent> ACTIVITY_RESULT_LAUNCHER;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        initVariables();

        init();
    }

    private void initVariables() {
        ACTIVITY_RESULT_LAUNCHER = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (result) -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    INTENT_TOKEN = result.getData();

                    CapturingService.requestStartRecording(this);
                } else {
                    INTENT_TOKEN = null;
                }

                finish();

                overridePendingTransition(0, 0);
            }
        );
    }

    private void init() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ACTIVITY_RESULT_LAUNCHER.launch(
                ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(
                    MediaProjectionConfig.createConfigForDefaultDisplay()
                )
            );
        } else {
            ACTIVITY_RESULT_LAUNCHER.launch(
                ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent()
            );
        }
    }

    public static void requestToken(Context ctx) {
        if(!hasToken() || isToRecycle(ctx)) {
            final Intent i = new Intent(ctx, TokenActivity.class);

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

            ctx.startActivity(i);
        }
    }

    public static boolean hasToken() {
        return INTENT_TOKEN != null;
    }

    public static Intent getToken() {
        if(hasToken()) {
            return (Intent) INTENT_TOKEN.clone();
        }

        return null;
    }

    public static void clearToken() {
        INTENT_TOKEN = null;
    }

    public static void clearToken(Context ctx) {
        clearToken();

        Toast.makeText(ctx, ctx.getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();
    }

    public static boolean isToRecycle(Context ctx) {
        return ((Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) && KSharedPreferences.getAppSp(ctx).getBoolean(Constants.Sp.App.IS_TO_RECYCLE_TOKEN, DefaultSettings.IS_TO_RECYCLE_TOKEN));
    }
}