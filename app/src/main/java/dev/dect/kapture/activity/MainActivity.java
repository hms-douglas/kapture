package dev.dect.kapture.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import android.Manifest;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;

import dev.dect.kapture.adapter.FragmentAdapter;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.R;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.fragment.KapturesFragment;
import dev.dect.kapture.fragment.SettingsFragment;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.notification.ReceivingNotification;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.PermissionPopup;
import dev.dect.kapture.utils.Utils;

@SuppressLint({"ApplySharedPref", "UnsafeIntentLaunch"})
public class MainActivity extends AppCompatActivity {
    private static MainActivity ACTIVITY = null;

    private boolean IS_OUT_FOR_PERMISSION = false,
                    IS_TABLET_UI = false;

    private SharedPreferences SP_APP;

    private AppCompatButton BTN_TAB_CAPTURES,
                            BTN_TAB_SETTINGS;

    private ViewPager2 VIEW_PAGER;

    private KapturesFragment KAPTURE_FRAGMENT;

    private Dialog POPUP;

    private int AMOUNT_SELECTED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initVariables();

        initListeners();

        checkAndRequestPermissions();
    }

    @Override
    protected void onDestroy() {
        ACTIVITY = null;

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ACTIVITY = this;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        ACTIVITY = this;

        if(IS_OUT_FOR_PERMISSION) {
            IS_OUT_FOR_PERMISSION = false;

            checkAndRequestPermissions();
        }
    }

    public static MainActivity getInstance() {
        return ACTIVITY;
    }

    private void checkAndRequestPermissions() {
        switch(SP_APP.getInt(Constants.Sp.App.PERMISSION_STEPS, 0)) {
            case 0:
                requestAndroidPermissions(true);

                POPUP.show();
                break;

            case 1:
                if(POPUP != null && POPUP.isShowing()) {
                    POPUP.dismiss();
                }

                POPUP = new DialogPopup(
                  this,
                    R.string.permission_storage_title,
                    R.string.permission_storage_popup,
                    R.string.permission_button_settings,
                    () -> {
                        SP_APP.edit().putInt(Constants.Sp.App.PERMISSION_STEPS, 2).commit();

                        IS_OUT_FOR_PERMISSION = true;

                        startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
                    },
                    R.string.permission_button_close,
                    this::finish,
                    false,
                    false,
                    false
                );

                POPUP.show();
                break;

            case 2:
                if(POPUP != null && POPUP.isShowing()) {
                    POPUP.dismiss();
                }

                POPUP = new DialogPopup(
                    this,
                    R.string.permission_security_title,
                    R.string.permission_security_popup,
                    R.string.permission_button_open_accessibility,
                    () -> {
                        SP_APP.edit().putInt(Constants.Sp.App.PERMISSION_STEPS, 3).commit();

                        IS_OUT_FOR_PERMISSION = true;

                        Utils.ExternalActivity.requestAccessibility(this);
                    },
                    R.string.permission_button_continue_to_app,
                    () -> {
                        SP_APP.edit().putInt(Constants.Sp.App.PERMISSION_STEPS, 3).commit();

                        checkAndRequestPermissions();
                    },
                    false,
                    false,
                    false
                );

                POPUP.show();
                break;

            case 3:
                init();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        checkAndRequestPermissions();
    }

    private void requestAndroidPermissions(boolean continueSteps) {
        POPUP = new PermissionPopup(
            this,
            () -> {
                ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.CAMERA
                }, 0);

                if(continueSteps) {
                    SP_APP.edit().putInt(Constants.Sp.App.PERMISSION_STEPS, 1).commit();
                } else {
                    POPUP.dismiss();
                }
            }
        );
    }

    private void initVariables() {
        ACTIVITY = this;

        SP_APP = KSharedPreferences.getAppSp(this);

        IS_TABLET_UI = getResources().getBoolean(R.bool.is_tablet);

        if(!IS_TABLET_UI) {
            BTN_TAB_CAPTURES = findViewById(R.id.bottomBarBtnCaptures);
            BTN_TAB_SETTINGS = findViewById(R.id.bottomBarBtnSettings);

            VIEW_PAGER = findViewById(R.id.viewPager);
        }

        KAPTURE_FRAGMENT = new KapturesFragment();
    }

    private void initListeners() {
        if(!IS_TABLET_UI) {
            VIEW_PAGER.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                if (position == 0) {
                    BTN_TAB_CAPTURES.setTextColor(getColor(R.color.bottom_bar_btn_text_color_on));
                    BTN_TAB_CAPTURES.setText(generateEnabledBottomBarBtn(R.string.title_captures));

                    BTN_TAB_SETTINGS.setTextColor(getColor(R.color.bottom_bar_btn_text_color_off));
                    BTN_TAB_SETTINGS.setText(getString(R.string.title_settings));
                } else {
                    BTN_TAB_CAPTURES.setTextColor(getColor(R.color.bottom_bar_btn_text_color_off));
                    BTN_TAB_CAPTURES.setText(getString(R.string.title_captures));

                    BTN_TAB_SETTINGS.setTextColor(getColor(R.color.bottom_bar_btn_text_color_on));
                    BTN_TAB_SETTINGS.setText(generateEnabledBottomBarBtn(R.string.title_settings));
                }
                }
            });

            BTN_TAB_CAPTURES.setOnClickListener((v) -> goToFragment(0));
            BTN_TAB_SETTINGS.setOnClickListener((v) -> goToFragment(1));

            ACTIVITY.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if(VIEW_PAGER.getCurrentItem() == 0) {
                        if(KAPTURE_FRAGMENT.isSelection()) {
                            KAPTURE_FRAGMENT.unselectAll();
                        } else if(KAPTURE_FRAGMENT.isSearching()) {
                            KAPTURE_FRAGMENT.closeSearch();
                        } else {
                            moveTaskToBack(false);
                        }
                    } else {
                        goToFragment(0);
                    }
                }
            });
        }

        findViewById(R.id.btnMore).setOnClickListener((v) -> {
            if(!KAPTURE_FRAGMENT.isSelection()) {
                return;
            }

            final PopupMenu popupMenu = new PopupMenu(this, v, Gravity.END, 0, R.style.KPopupMenu);

            final Menu menu = popupMenu.getMenu();

            menu.setGroupDividerEnabled(true);

            popupMenu.getMenuInflater().inflate(R.menu.capture_bottom_bar_more, menu);

            if(AMOUNT_SELECTED > 1) {
                menu.findItem(R.id.menuRename).setEnabled(false);
                menu.findItem(R.id.menuOpenWith).setEnabled(false);
                menu.findItem(R.id.menuShowExtra).setEnabled(false);
            } else {
                if(!KAPTURE_FRAGMENT.selectedHasExtra()) {
                    menu.setGroupVisible(R.id.groupExtras, false);
                }

                if(!KAPTURE_FRAGMENT.selectedHasScreenshot()) {
                    menu.setGroupVisible(R.id.groupScreenshots, false);
                }
            }

            popupMenu.setOnMenuItemClickListener((item) -> {
                final int idClicked = item.getItemId();

                if(idClicked == R.id.menuRename) {
                    KAPTURE_FRAGMENT.renameSelected();
                } else if(idClicked == R.id.menuRemove) {
                    KAPTURE_FRAGMENT.removeSelected();
                } else if(idClicked == R.id.menuDeleteExtra) {
                    KAPTURE_FRAGMENT.deleteSelectedExtras();
                } else if(idClicked == R.id.menuShowExtra) {
                    KAPTURE_FRAGMENT.showSelectedExtras();
                } else if(idClicked == R.id.menuDeleteScreenshot) {
                    KAPTURE_FRAGMENT.deleteSelectedScreenshots();
                } else if(idClicked == R.id.menuShowScreenshot) {
                    KAPTURE_FRAGMENT.showSelectedScreenshots();
                }else if(idClicked == R.id.menuOpenWith) {
                    KAPTURE_FRAGMENT.openSelectedWith();
                }

                return true;
            });

            popupMenu.show();
        });

        findViewById(R.id.btnDelete).setOnClickListener((v) -> KAPTURE_FRAGMENT.deleteSelected());

        findViewById(R.id.btnShare).setOnClickListener((v) -> KAPTURE_FRAGMENT.shareSelected());

        findViewById(R.id.btnWifiShare).setOnClickListener((v) -> KAPTURE_FRAGMENT.wifiShareSelected());

        KAPTURE_FRAGMENT.setListener((hasSelection, amountSelected) -> {
            AMOUNT_SELECTED = amountSelected;

            toggleBottomMenu(hasSelection);
        });
    }

    private void init() {
        SettingsFragment.setTheme(this, SP_APP.getInt(Constants.Sp.App.APP_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), false);

        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            requestAndroidPermissions(false);

            POPUP.show();
            return;
        }

        if(!Environment.isExternalStorageManager()) {
            new DialogPopup(
                this,
                R.string.permission_missing_popup,
                R.string.permission_required_popup,
                R.string.permission_button_settings,
                () -> {
                    Utils.ExternalActivity.requestSettings(this);
                    finish();
                },
                R.string.permission_button_close,
                this::finish,
                true,
                true,
                false
            ).show();
        }

        final boolean isToLaunchNotificationSettings = getIntent().getCategories().contains("android.intent.category.NOTIFICATION_PREFERENCES");

        if(IS_TABLET_UI) {
            final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.frameLayout, KAPTURE_FRAGMENT);

            fragmentTransaction.commitNow();

            KAPTURE_FRAGMENT.openSettings_tabletUi();
        } else {
            VIEW_PAGER.setOffscreenPageLimit(1);

            VIEW_PAGER.setAdapter(
                new FragmentAdapter(
                    this,
                    Arrays.asList(
                        KAPTURE_FRAGMENT,
                        new SettingsFragment()
                    )
                )
            );

            if(isToLaunchNotificationSettings) {
                goToFragment(1, false);
            }
        }
    }

    private void goToFragment(int pos, boolean animate) {
        if(!IS_TABLET_UI && pos != VIEW_PAGER.getCurrentItem()) {
            VIEW_PAGER.setCurrentItem(pos, animate);
        }
    }

    private void goToFragment(int pos) {
        goToFragment(pos, true);
    }

    private SpannableStringBuilder generateEnabledBottomBarBtn(int s) {
        final String text = getString(s);

        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);

        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(new CharacterStyle() {
            @Override
            public void updateDrawState(TextPaint tp) {
                tp.underlineColor = getColor(R.color.bottom_bar_btn_text_color_on);
                tp.underlineThickness = getResources().getDimensionPixelSize(R.dimen.bottom_bar_btn_text_underline);
            }
        }, 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableStringBuilder;
    }

    public void requestUiUpdate(Kapture kapture) {
        KAPTURE_FRAGMENT.updateUI(kapture);
    }

    public KapturesFragment getKaptureFragment() {
        return KAPTURE_FRAGMENT;
    }

    public void relaunch() {
        navigateUpTo(new Intent(MainActivity.this, MainActivity.class));
        startActivity(getIntent());
    }

    private void toggleBottomMenu(boolean show) {
        if(IS_TABLET_UI) {
            if(show) {
                toggleBottomMenuAnimation(findViewById(R.id.bottomBarActions), null);

                KAPTURE_FRAGMENT.setCaptureButtonVisibility(true);
            } else {
                toggleBottomMenuAnimation(null, findViewById(R.id.bottomBarActions));

                KAPTURE_FRAGMENT.setCaptureButtonVisibility(false);
            }
        } else {
            if(show) {
                if(VIEW_PAGER.isUserInputEnabled()) {
                    VIEW_PAGER.setUserInputEnabled(false);

                    toggleBottomMenuAnimation(findViewById(R.id.bottomBarActions), findViewById(R.id.bottomBarNavigation));

                    KAPTURE_FRAGMENT.setCaptureButtonVisibility(true);
                }
            } else {
                VIEW_PAGER.setUserInputEnabled(true);

                toggleBottomMenuAnimation(findViewById(R.id.bottomBarNavigation), findViewById(R.id.bottomBarActions));

                KAPTURE_FRAGMENT.setCaptureButtonVisibility(false);
            }
        }
    }

    private void toggleBottomMenuAnimation(View vIn, View vOut) {
        final int height = getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);

        final ViewGroup.LayoutParams layoutParamsIn = vIn == null ? null : vIn.getLayoutParams(),
                                     layoutParamsOut = vOut == null ? null : vOut.getLayoutParams();

        final ValueAnimator animation = ValueAnimator.ofInt(1, height);

        animation.addUpdateListener(valueAnimator -> {
            final int animatedValue = (Integer) valueAnimator.getAnimatedValue();

            if(layoutParamsIn != null) {
                layoutParamsIn.height = animatedValue;

                vIn.setLayoutParams(layoutParamsIn);
            }

            if(layoutParamsOut != null) {
                layoutParamsOut.height = height - animatedValue;

                vOut.setLayoutParams(layoutParamsOut);
            }
        });

        animation.setDuration(450);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                if(vIn != null) {
                    vIn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(vOut != null) {
                    vOut.setVisibility(View.GONE);
                }
            }
        });

        animation.start();
    }
}