package dev.dect.kapture.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.Menu;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import org.json.JSONArray;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.profile.ProfileEditorActivity;
import dev.dect.kapture.activity.profile.ProfileManagerActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.service.CapturingService;
import dev.dect.kapture.widget.ProfileWidget;

@SuppressLint("ApplySharedPref")
public class KProfile {
    public static void init(AppCompatButton btn) {
        final Context ctx = btn.getContext();

        initButtonListener(ctx, btn);

        updateButtonUi(ctx, btn);
    }

    private static void initButtonListener(Context ctx, AppCompatButton btn) {
        btn.setOnClickListener((v) -> {
            final PopupMenu popupMenu = new PopupMenu(ctx, btn, Gravity.START, 0, R.style.KPopupMenu);

            final Menu menu = popupMenu.getMenu();

            final ArrayList<SharedPreferences> profiles = getAllProfilesSp(ctx);

            if(!profiles.isEmpty()) {
                menu.add(0, -1, Menu.NONE, ctx.getString(R.string.profile_default)).setIcon(R.drawable.icon_profile_i_default).setIconTintList(ColorStateList.valueOf(ctx.getColor(R.color.app_bar_profile)));

                int i = 0;

                for(SharedPreferences sp : profiles) {
                    menu
                        .add(0, i++, Menu.NONE, sp.getString(Constants.Sp.Profile.SP_FILE_USER_NAME, "?"))
                        .setIcon(getIconResId(sp))
                        .setIconTintList(
                            ColorStateList.valueOf(
                                Utils.Converter.hexColorToInt(
                                    sp.getString(Constants.Sp.Profile.SP_FILE_ICON_COLOR,
                                    Utils.Converter.intColorToHex(ctx, R.color.app_bar_profile)
                                )
                            )
                        )
                    );
                }
            }

            menu.add(1, -2, Menu.NONE, ctx.getString(R.string.profile_new)).setIcon(R.drawable.icon_profile_menu_new).setIconTintList(ColorStateList.valueOf(ctx.getColor(R.color.app_bar_profile)));

            if(!profiles.isEmpty()) {
                menu.add(1, -3, Menu.NONE, ctx.getString(R.string.profile_manage)).setIcon(R.drawable.icon_profile_menu_manage).setIconTintList(ColorStateList.valueOf(ctx.getColor(R.color.app_bar_profile)));
            }

            menu.setGroupDividerEnabled(true);

            popupMenu.setForceShowIcon(true);

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if(menuItem.getItemId() == -2) {
                    if(getAllProfilesName(ctx).length() == 0) {
                        new DialogPopup(
                            ctx,
                            DialogPopup.NO_TEXT,
                            R.string.profile_about_message,
                            R.string.popup_btn_ok,
                            () -> newProfile(ctx),
                            DialogPopup.NO_TEXT,
                            null,
                            false,
                            false,
                            false
                        ).show();
                    } else {
                        newProfile(ctx);
                    }
                } else if(menuItem.getItemId() == -3) {
                    manageProfiles(ctx);
                } else {
                    setActiveProfile(
                        ctx,
                        menuItem.getItemId() == -1 ? Constants.Sp.Profile.SP_FILE_NAME_DEFAULT : profiles.get(menuItem.getItemId()).getString(Constants.Sp.Profile.SP_FILE_NAME, null),
                        true
                    );
                }

                return true;
            });

            popupMenu.show();
        });
    }

    public static void updateButtonUi(Context ctx, AppCompatButton btn) {
        final SharedPreferences spProfile = KSharedPreferences.getActiveProfileSp(ctx);

        final String activeProfileFileName = spProfile.getString(Constants.Sp.Profile.SP_FILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT);

        if(activeProfileFileName.equals(Constants.Sp.Profile.SP_FILE_NAME_DEFAULT)) {
            btn.setText(ctx.getString(R.string.profile_default));

            final Drawable icon = Utils.getDrawable(ctx, R.drawable.icon_profile_i_default);

            Utils.colorDrawable(icon, ctx.getColor(R.color.app_bar_profile));

            btn.setCompoundDrawablesWithIntrinsicBounds(icon, null, Utils.getDrawable(ctx, R.drawable.dropdown_arrow), null);
        } else {
            btn.setText(spProfile.getString(Constants.Sp.Profile.SP_FILE_USER_NAME, "?"));

            final Drawable icon = Utils.getDrawable(ctx, getIconResId(spProfile));

            Utils.colorDrawable(icon, spProfile.getString(Constants.Sp.Profile.SP_FILE_ICON_COLOR, Utils.Converter.intColorToHex(ctx, R.color.app_bar_profile)));

            btn.setCompoundDrawablesWithIntrinsicBounds(icon, null, Utils.getDrawable(ctx, R.drawable.dropdown_arrow), null);
        }
    }

    public static SharedPreferences.OnSharedPreferenceChangeListener createAndAddProfileListenerUpdate(SharedPreferences spApp, AppCompatButton btn, @Nullable Runnable runnable) {
        final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, s) -> {
            if(s != null && s.equals(Constants.Sp.App.ACTIVE_PROFILE_NAME)) {
                updateButtonUi(btn.getContext(), btn);

                if(runnable != null) {
                    runnable.run();
                }
            }
        };

        spApp.registerOnSharedPreferenceChangeListener(listener);

        return listener;
    }

    public static int getIconResId(SharedPreferences sp) {
        return getIconResId(sp.getInt(Constants.Sp.Profile.SP_FILE_ICON, 0));
    }

    public static int getIconResId(int index) {
        return getAllIcons()[index];
    }

    public static int[] getAllIcons() {
        return new int[]{
            R.drawable.icon_profile_i_default,
            R.drawable.icon_profile_i_star,
            R.drawable.icon_profile_i_heart,
            R.drawable.icon_profile_i_diamond,
            R.drawable.icon_profile_i_bear,
            R.drawable.icon_profile_i_cat,
            R.drawable.icon_profile_i_dog,
            R.drawable.icon_profile_i_robot,
            R.drawable.icon_profile_i_puzzle,
            R.drawable.icon_profile_i_game,
            R.drawable.icon_profile_i_makeup,
            R.drawable.icon_profile_i_lab,
            R.drawable.icon_profile_i_shopping,
            R.drawable.icon_profile_i_message,
            R.drawable.icon_profile_i_calendar,
            R.drawable.icon_profile_i_clock,
            R.drawable.icon_profile_i_headphone,
            R.drawable.icon_profile_i_music,
            R.drawable.icon_profile_i_location,
            R.drawable.icon_profile_i_car,
            R.drawable.icon_profile_i_person,
            R.drawable.icon_profile_i_dex,
            R.drawable.icon_profile_i_phone,
            R.drawable.icon_profile_i_book,
            R.drawable.icon_profile_i_pen,
            R.drawable.icon_profile_i_multiscreen,
            R.drawable.icon_profile_i_home,
            R.drawable.icon_profile_i_moon,
            R.drawable.icon_profile_i_disturb,
            R.drawable.icon_profile_i_wifi,
            R.drawable.icon_profile_i_apps,
            R.drawable.icon_profile_i_battery,
            R.drawable.icon_profile_i_lightning,
            R.drawable.icon_profile_i_movie,
            R.drawable.icon_profile_i_smile
        };
    }

    public static boolean isUsingProfile(Context ctx, String name) {
        return KSharedPreferences.getActiveProfileSp(ctx).getString(Constants.Sp.Profile.SP_FILE_NAME, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT).equals(name);
    }

    public static ArrayList<SharedPreferences> getAllProfilesSp(Context ctx) {
        final ArrayList<SharedPreferences> sps = new ArrayList<>();

        final JSONArray profilesName = getAllProfilesName(ctx);

        try {
            for (int i = 0; i < profilesName.length(); i++) {
                sps.add(KSharedPreferences.getProfileSp(ctx, profilesName.getString(i)));
            }
        } catch (Exception ignore) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
        }

        return sps;
    }

    public static JSONArray getAllProfilesName(Context ctx) {
        final SharedPreferences sp = KSharedPreferences.getAppSp(ctx);

        try {
            return new JSONArray(sp.getString(Constants.Sp.App.PROFILE_NAMES, "[]"));
        } catch (Exception ignore) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
        }

        return new JSONArray();
    }

    public static void newProfile(Context ctx) {
        final Intent i = new Intent(ctx, ProfileEditorActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ctx.startActivity(i);
    }

    private static void manageProfiles(Context ctx) {
        final Intent i = new Intent(ctx, ProfileManagerActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ctx.startActivity(i);
    }

    public static void editProfile(Context ctx, String name) {
        final Intent i = new Intent(ctx, ProfileEditorActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        i.putExtra(ProfileEditorActivity.INTENT_FILE_NAME, name);

        ctx.startActivity(i);
    }

    public static void deleteProfile(Context ctx, String name, @Nullable Runnable onDeleted) {
        if(CapturingService.isRecording() || CapturingService.isProcessing()) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

            return;
        }

        if(!isNameValid(ctx, name, true)) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            return;
        }

        new DialogPopup(
            ctx,
            R.string.tooltip_profile_delete,
            ctx.getString(R.string.popup_delete_text_5)  + " \"" + KSharedPreferences.getProfileSp(ctx, name).getString(Constants.Sp.Profile.SP_FILE_USER_NAME, "?") + "\"?",
            R.string.popup_btn_delete,
            () -> {
                if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                    return;
                }

                if(isUsingProfile(ctx, name)) {
                    setActiveProfile(ctx, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT, false);
                }

                final JSONArray profiles = getAllProfilesName(ctx);

                try {
                    for (int i = 0; i < profiles.length(); i++) {
                        if (profiles.getString(i).equals(name)) {
                            profiles.remove(i);

                            break;
                        }
                    }

                    KSharedPreferences.getAppSp(ctx).edit().putString(Constants.Sp.App.PROFILE_NAMES, profiles.toString()).commit();

                    KSharedPreferences.getProfileSp(ctx, name).edit().clear().commit();

                    ProfileWidget.requestUpdateAllFromType(ctx);

                    if(onDeleted != null) {
                        onDeleted.run();
                    }
                } catch (Exception ignore) {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
                }
            },
            R.string.popup_btn_cancel,
            null,
            false,
            false,
            true
        ).show();
    }

    public static void setActiveProfile(Context ctx, String name, boolean showToast) {
        try {
            if(name == null) {
                Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

                return;
            }

            if(CapturingService.isRecording() || CapturingService.isProcessing()) {
                Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

                return;
            }

            if(isUsingProfile(ctx, name)) {
                return;
            }

            final SharedPreferences sp = KSharedPreferences.setSpActiveProfile(ctx, name);

            ProfileWidget.requestUpdateAllFromType(ctx);

            if(showToast) {
                Toast.makeText(
                    ctx,
                    ctx.getString(R.string.toast_success_active_profile) + " " + (isUsingProfile(ctx, Constants.Sp.Profile.SP_FILE_NAME_DEFAULT) ? ctx.getString(R.string.profile_default) : sp.getString(Constants.Sp.Profile.SP_FILE_USER_NAME, "?")),
                    Toast.LENGTH_SHORT
                ).show();
            }
        } catch (Exception ignore) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean createProfile(Context ctx, String userName, String color, int icon, boolean activate) {
        if(isUserNameValid(ctx, userName)) {
            String name = String.valueOf(Utils.generateTimeId());

            while(KSharedPreferences.exists(ctx, name)) {
                name = String.valueOf(Utils.generateTimeId());
            }

            final JSONArray profiles = getAllProfilesName(ctx);

            profiles.put(name);

            KSharedPreferences.getAppSp(ctx).edit().putString(Constants.Sp.App.PROFILE_NAMES, profiles.toString()).commit();

            final SharedPreferences.Editor spClonedEditor = KSharedPreferences.cloneActiveProfile(ctx, name).edit();

            spClonedEditor.putString(Constants.Sp.Profile.SP_FILE_NAME, name);
            spClonedEditor.putString(Constants.Sp.Profile.SP_FILE_USER_NAME, userName);
            spClonedEditor.putString(Constants.Sp.Profile.SP_FILE_ICON_COLOR, color);
            spClonedEditor.putInt(Constants.Sp.Profile.SP_FILE_ICON, icon);

            spClonedEditor.commit();

            if(activate) {
                setActiveProfile(ctx, name, true);
            }

            ProfileWidget.requestUpdateAllFromType(ctx);

            return true;
        }

        return false;
    }

    public static boolean updateProfile(Context ctx, String name, String userName, String color, int icon) {
        if(isNameValid(ctx, name, true) && isUserNameValid(ctx, userName)) {
            final SharedPreferences.Editor sp = KSharedPreferences.getProfileSp(ctx, name).edit();

            sp.putString(Constants.Sp.Profile.SP_FILE_USER_NAME, userName);
            sp.putString(Constants.Sp.Profile.SP_FILE_ICON_COLOR, color);
            sp.putInt(Constants.Sp.Profile.SP_FILE_ICON, icon);

            sp.commit();

            ProfileWidget.requestUpdateAllFromType(ctx);

            return true;
        }

        return false;
    }

    private static boolean isUserNameValid(Context ctx, String userName) {
        if(userName.trim().isEmpty()) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_profile_name_empty), Toast.LENGTH_SHORT).show();

            return false;
        } else if(userName.length() > 20) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_profile_name_long) + " " + 20, Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    private static boolean isNameValid(Context ctx, String name, boolean validateExistance) {
        return name != null && !name.trim().isEmpty() && !name.equals(Constants.Sp.Profile.SP_FILE_NAME_DEFAULT) && (validateExistance ? KSharedPreferences.exists(ctx, name) : true);
    }
}
