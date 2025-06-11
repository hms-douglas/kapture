package dev.dect.kapture.activity.installer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.AboutActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.downloader.VersionsDownloader;
import dev.dect.kapture.model.Version;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.PickerPopup;
import dev.dect.kapture.utils.Utils;

/** @noinspection ResultOfMethodCallIgnored */
@SuppressLint("SetTextI18n")
public class InstallActivity extends AppCompatActivity {
    private int[] STEPS,
                  INSTRUCTIONS,
                  ANIMATIONS;

    private int CURRENT_STEP;

    private TextView STEP_EL,
                     INSTRUCTION_EL;

    private ImageView ANIMATION_EL;

    private EditText CONNECTING_ADDRESS_EL,
                     PARING_ADDRESS_EL,
                     PARING_CODE_EL;

    private AppCompatButton VERSION_EL;

    private Version VERSION_SELECTED;

    private ArrayList<Version> VERSIONS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_install);

        Utils.updateStatusBarColor(this);

        initVariables();

        initListeners();

        init();
    }

    private void initVariables() {
        CURRENT_STEP = 0;

        STEPS = new int[] {
            R.string.how_to_connect_step_0,
            R.string.how_to_connect_step_1,
            R.string.how_to_connect_step_2,
            R.string.how_to_connect_step_3,
            R.string.how_to_connect_step_4,
            R.string.how_to_connect_step_5
        };

        INSTRUCTIONS = new int[] {
            R.string.how_to_connect_instruction_0,
            R.string.how_to_connect_instruction_1,
            R.string.how_to_connect_instruction_2,
            R.string.how_to_connect_instruction_3,
            R.string.how_to_connect_instruction_4,
            R.string.how_to_connect_instruction_5
        };

        ANIMATIONS = new int[] {
            R.drawable.instruction_0,
            R.drawable.instruction_1,
            R.drawable.instruction_2,
            R.drawable.instruction_3,
            R.drawable.instruction_4,
            -1
        };

        VERSIONS = new ArrayList<>();

        STEP_EL = findViewById(R.id.step);
        INSTRUCTION_EL = findViewById(R.id.instruction);
        ANIMATION_EL = findViewById(R.id.animation);
        CONNECTING_ADDRESS_EL = findViewById(R.id.connectingAddress);
        PARING_ADDRESS_EL = findViewById(R.id.paringAddress);
        PARING_CODE_EL = findViewById(R.id.paringCode);
        VERSION_EL = findViewById(R.id.version);
    }

    private void initListeners() {
        ((AppBarLayout) findViewById(R.id.titleBar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            verticalOffset = Math.abs(verticalOffset);

            final float opacity = (float) verticalOffset / ((appBarLayout.getHeight() - findViewById(R.id.toolbar).getHeight()) / 2f);

            findViewById(R.id.titleExpanded).setAlpha(1 - opacity);

            findViewById(R.id.titleCollapsed).setAlpha(opacity != 1 ? opacity - 0.5f : 1);
        });

        findViewById(R.id.btnMore).setOnClickListener((v) -> {
            final PopupMenu popupMenu = new PopupMenu(this, v, Gravity.END, 0, R.style.KPopupMenu);

            final Menu menu = popupMenu.getMenu();

            menu.setGroupDividerEnabled(true);

            popupMenu.getMenuInflater().inflate(R.menu.install_more, menu);

            popupMenu.setOnMenuItemClickListener((item) -> {
                final int id = item.getItemId();

                if(id == R.id.menuClearCache) {
                    for(File f : Objects.requireNonNull(getCacheDir().listFiles())) {
                        if(f.getName().startsWith(Constants.VERSION_FILE_PREFIX)) {
                            f.delete();
                        }
                    }

                    Toast.makeText(this, getString(R.string.toast_success_generic), Toast.LENGTH_SHORT).show();

                    fetchVersions();
                } else if(id == R.id.menuOpenAccessibility) {
                    Utils.ExternalActivity.requestAccessibility(this);
                } else if(id == R.id.menuShowCommand) {
                    new DialogPopup(
                        this,
                        DialogPopup.NO_TEXT,
                        R.string.command,
                        R.string.popup_btn_ok,
                        null,
                        DialogPopup.NO_TEXT,
                        null,
                        true,
                        false,
                        false
                    ).show();
                } else if(id == R.id.menuAbout) {
                    startActivity(new Intent(this, AboutActivity.class));
                }

                return true;
            });

            popupMenu.show();
        });

        findViewById(R.id.btnPrevious).setOnClickListener((v) -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

            CURRENT_STEP--;

            updateStepUI();
        });

        findViewById(R.id.btnNext).setOnClickListener((v) -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

            CURRENT_STEP++;

            updateStepUI();
        });

        findViewById(R.id.btnInstall).setOnClickListener((v) -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

            requestInstallActivity();
        });

        findViewById(R.id.btnBack).setOnClickListener((v) -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

            finish();
        });

        ((SwipeRefreshLayout) findViewById(R.id.swipeToRefresh)).setOnRefreshListener(() -> {
            ((SwipeRefreshLayout) findViewById(R.id.swipeToRefresh)).setRefreshing(true);

            Toast.makeText(this, getString(R.string.toast_info_refreshing), Toast.LENGTH_SHORT).show();

            new CountDownTimer(1500, 500) {
                @Override
                public void onTick(long l) {}

                @Override
                public void onFinish() {
                    ((SwipeRefreshLayout) findViewById(R.id.swipeToRefresh)).setRefreshing(false);

                    fetchVersions();
                }
            }.start();
        });

        VERSION_EL.setOnClickListener((v) -> {
            if(VERSION_SELECTED == null) {
                Toast.makeText(this, VERSION_EL.getText(), Toast.LENGTH_SHORT).show();
            } else {
                final String[] names = new String[VERSIONS.size()];

                for(int i = 0; i < VERSIONS.size(); i++) {
                    names[i] = "v" + VERSIONS.get(i).getVersionName();
                }

                new PickerPopup(
                    this,
                    R.string.version_popup_title,
                    names,
                    VERSIONS.indexOf(VERSION_SELECTED),
                    this::setSelectedVersion
                ).show();
            }
        });

        PARING_CODE_EL.setOnEditorActionListener((textView, i, keyEvent) -> {
            if(i == EditorInfo.IME_ACTION_DONE) {
                findViewById(R.id.btnInstall).callOnClick();

                return true;
            }

            return false;
        });
    }

    private void init() {
        updateStepUI();

        fetchVersions();
    }

    private void updateStepUI() {
        String step = getString(R.string.how_to_connect_step_number);

        step = step.replaceFirst("%d0", String.valueOf((CURRENT_STEP + 1))).replaceFirst("%d1", String.valueOf(STEPS.length));

        step += " - " + getString(STEPS[CURRENT_STEP]);

        STEP_EL.setText(step);

        INSTRUCTION_EL.setText(INSTRUCTIONS[CURRENT_STEP]);

        findViewById(R.id.btnPrevious).setVisibility(CURRENT_STEP == 0 ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.btnNext).setVisibility(CURRENT_STEP == (STEPS.length - 1) ? View.INVISIBLE : View.VISIBLE);

        if(ANIMATIONS[CURRENT_STEP] == -1) {
            findViewById(R.id.watchAnimationContainer).setVisibility(View.GONE);
        } else {
            findViewById(R.id.watchAnimationContainer).setVisibility(View.VISIBLE);

            try {
                final Drawable drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(getResources(), ANIMATIONS[CURRENT_STEP]));

                ANIMATION_EL.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .alpha(0.2f)
                .setDuration(400)
                .withEndAction(() -> {
                    ANIMATION_EL.setImageDrawable(drawable);

                    ANIMATION_EL.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(400)
                    .withEndAction(() -> {
                        ((AnimatedImageDrawable) drawable).start();
                    }).start();
                }).start();
            } catch (Exception e) {
                ANIMATION_EL.setImageResource(ANIMATIONS[CURRENT_STEP]);
            }
        }
    }

    private void requestInstallActivity() {
        final Intent i = new Intent(this, InstallProgressActivity.class);

        i.putExtra(InstallProgressActivity.EXTRA_VERSION, VERSION_SELECTED);
        i.putExtra(InstallProgressActivity.EXTRA_CONNECTING_IP_PORT, CONNECTING_ADDRESS_EL.getText().toString());
        i.putExtra(InstallProgressActivity.EXTRA_PARING_IP_PORT, PARING_ADDRESS_EL.getText().toString());
        i.putExtra(InstallProgressActivity.EXTRA_PARING_CODE, PARING_CODE_EL.getText().toString());
        i.putExtra(InstallProgressActivity.EXTRA_ENABLE_SECURE_SETTING, ((Switch) findViewById(R.id.switchSecurity)).isChecked());

        startActivity(i);

        PARING_ADDRESS_EL.setText("");
        PARING_CODE_EL.setText("");
    }

    private void fetchVersions() {
        VERSION_SELECTED = null;

        VERSIONS.clear();

        VERSION_EL.setText(getString(R.string.version_loading));

        fetchVersionsFromInternet();
    }

    private void fetchVersionsFromInternet() {
        new VersionsDownloader((data) -> new Handler(Looper.getMainLooper()).post(() -> {
            if(data == null) {
                Toast.makeText(this, getString(R.string.toast_error_version_fetch), Toast.LENGTH_SHORT).show();

                fetchVersionsFromCache();

                return;
            }

            try {
                for(int i = 0; i < data.length(); i++) {
                    final Version version = new Version(data.getJSONObject(i));

                    if(version.isValid()) {
                        VERSIONS.add(version);
                    }
                }

                VERSIONS.sort(Comparator.comparing(Version::getVersionCode, Comparator.reverseOrder()));

                if(VERSIONS.isEmpty()) {
                    Toast.makeText(this, getString(R.string.toast_error_version_fetch), Toast.LENGTH_SHORT).show();

                    fetchVersionsFromCache();
                } else {
                    setSelectedVersion(0);
                }
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.toast_error_version_fetch), Toast.LENGTH_SHORT).show();

                fetchVersionsFromCache();
            }
        })).start();
    }

    private void fetchVersionsFromCache() {
        final File cacheDir = getCacheDir();

        for(File f : Objects.requireNonNull(cacheDir.listFiles())) {
            if(f.getName().startsWith(Constants.VERSION_FILE_PREFIX)) {
                VERSIONS.add(new Version(f));
            }
        }

        if(VERSIONS.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_error_version_cached), Toast.LENGTH_SHORT).show();

            VERSION_SELECTED = null;

            VERSION_EL.setText(getString(R.string.version_error));
        } else {
            VERSIONS.sort(Comparator.comparing(Version::getVersionCode, Comparator.reverseOrder()));

            setSelectedVersion(0);
        }
    }

    private void setSelectedVersion(int index) {
        VERSION_SELECTED = VERSIONS.get(index);

        VERSION_EL.setText("v" + VERSION_SELECTED.getVersionName());
    }
}