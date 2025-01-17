package dev.dect.kapture.activity.profile;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.ProfileIconAdapter;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.popup.ColorPickerPopup;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.utils.KProfile;
import dev.dect.kapture.utils.Utils;

public class ProfileEditorActivity extends AppCompatActivity {
    public static final String INTENT_FILE_NAME = "name";

    private String PROFILE_NAME,
                   USER_PROFILE_NAME,
                   PROFILE_ICON_COLOR;

    private int PROFILE_ICON;

    private EditText USER_PROFILE_NAME_INPUT;

    private ImageView PROFILE_ICON_EL;

    private RecyclerView RECYCLER_VIEW;

    private ProfileIconAdapter PROFILE_ICON_ADAPTER;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);

        setContentView(R.layout.activity_profile_editor);

        initVariables();

        initListeners();

        init();
    }

    private void initVariables() {
        USER_PROFILE_NAME_INPUT = findViewById(R.id.userNameInput);

        PROFILE_ICON_EL = findViewById(R.id.icon);

        RECYCLER_VIEW = findViewById(R.id.recyclerView);

        if(getIntent().hasExtra(INTENT_FILE_NAME)) {
            PROFILE_NAME = getIntent().getStringExtra(INTENT_FILE_NAME);

            final SharedPreferences spProfile = KSharedPreferences.getProfileSp(this, PROFILE_NAME);

            USER_PROFILE_NAME = spProfile.getString(Constants.Sp.Profile.SP_FILE_USER_NAME, "");

            PROFILE_ICON_COLOR = spProfile.getString(Constants.Sp.Profile.SP_FILE_ICON_COLOR, Utils.Converter.intColorToHex(this, R.color.app_bar_profile));

            PROFILE_ICON = spProfile.getInt(Constants.Sp.Profile.SP_FILE_ICON, 0);
        } else {
            PROFILE_NAME = null;
            
            USER_PROFILE_NAME = "";

            PROFILE_ICON_COLOR = Utils.Converter.intColorToHex(this, R.color.app_bar_profile);

            PROFILE_ICON = 0;
        }

        PROFILE_ICON_ADAPTER = new ProfileIconAdapter(PROFILE_ICON);
    }

    private void initListeners() {
        findViewById(R.id.btnCancel).setOnClickListener((v) -> finish());

        findViewById(R.id.picker).setOnClickListener((v) -> {
            new ColorPickerPopup(this, PROFILE_ICON_COLOR, false, (color) -> {
                PROFILE_ICON_COLOR = color;

                updateIcon();
            }).show();
        });

        final LinearLayout pickerContainer = findViewById(R.id.pickerContainer);

        for(int i = 0; i < pickerContainer.getChildCount() - 1; i++) {
            final View btn = pickerContainer.getChildAt(i);

            btn.setOnClickListener((v) -> {
                PROFILE_ICON_COLOR = Utils.Converter.intColorToHex(btn.getBackgroundTintList().getDefaultColor());

                updateIcon();
            });
        }

        RECYCLER_VIEW.setOnClickListener((v) -> {
            PROFILE_ICON = PROFILE_ICON_ADAPTER.getSelected();

            updateIcon();
        });
        
        findViewById(R.id.btnDone).setOnClickListener((v) -> {
            boolean success;

            if(PROFILE_NAME == null) {
                success = KProfile.createProfile(
                    this,
                    USER_PROFILE_NAME_INPUT.getText().toString(),
                    PROFILE_ICON_COLOR,
                    PROFILE_ICON,
                    !CapturingService.isRecording() && !CapturingService.isProcessing()
                );
            } else {
                success = KProfile.updateProfile(
                    this,
                    PROFILE_NAME,
                    USER_PROFILE_NAME_INPUT.getText().toString(),
                    PROFILE_ICON_COLOR,
                    PROFILE_ICON
                );
            }

            if(success) {
                finish();
            }
        });
    }

    private void init() {
        RECYCLER_VIEW.setLayoutManager(new GridLayoutManager(this, getResources().getBoolean(R.bool.is_tablet) ? 15 : 8));

        RECYCLER_VIEW.setNestedScrollingEnabled(false);

        RECYCLER_VIEW.setAdapter(PROFILE_ICON_ADAPTER);

        showInfo();

        Utils.Keyboard.requestShowForInput(USER_PROFILE_NAME_INPUT);
    }

    private void showInfo() {
        USER_PROFILE_NAME_INPUT.setText(USER_PROFILE_NAME);

        updateIcon();
    }

    private void updateIcon() {
        final Drawable icon = Utils.getDrawable(this, KProfile.getIconResId(PROFILE_ICON));

        Utils.colorDrawable(icon, PROFILE_ICON_COLOR);

        PROFILE_ICON_EL.setImageDrawable(icon);
    }
}
