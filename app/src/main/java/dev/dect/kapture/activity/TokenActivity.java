package dev.dect.kapture.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

                    Toast.makeText(this, getString(R.string.toast_error_access_required), Toast.LENGTH_SHORT).show();
                }

                finish();

                overridePendingTransition(0, 0);
            }
        );
    }

    private void init() {
        ACTIVITY_RESULT_LAUNCHER.launch(((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent());
    }

    public static void requestToken(Context ctx) {
        if(!hasToken()) {
            final Intent i = new Intent(ctx, TokenActivity.class);

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

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

        Toast.makeText(ctx, ctx.getString(R.string.toast_info_success_generic), Toast.LENGTH_SHORT).show();
    }
}
