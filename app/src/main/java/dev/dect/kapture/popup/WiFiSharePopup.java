package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.server.KSecurity;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class WiFiSharePopup extends Dialog {
    public interface OnWiFiSharePopupListener {
        void onStopRequested();
    }

    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    private final Context CONTEXT;

    private final View VIEW;

    private final TextView PASSWORD_EL;

    private final ImageButton BTN_TOGGLE_PASSWORD;

    private String IP = "";

    private int PORT = 0;

    private boolean IS_PASSWORD_VISIBLE = false;

    private OnWiFiSharePopupListener LISTENER;

    private KSecurity KSECURITY;

    public WiFiSharePopup(Context ctx) {
        super(ctx, R.style.Theme_Translucent);

        this.CONTEXT = ctx;

        VIEW = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_wifi_share, null);

        this.POPUP_CONTAINER = VIEW.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = VIEW.findViewById(R.id.popup);
        this.PASSWORD_EL = VIEW.findViewById(R.id.password);
        this.BTN_TOGGLE_PASSWORD = VIEW.findViewById(R.id.btnPassword);

        VIEW.findViewById(R.id.popupBtnStop).setOnClickListener((v) -> this.dismissWithAnimation());

        VIEW.findViewById(R.id.btnPassword).setOnClickListener((v) -> {
            IS_PASSWORD_VISIBLE = !IS_PASSWORD_VISIBLE;

            refreshPasswordField();
        });

        this.setCancelable(false);

        this.setContentView(VIEW);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);

        dismiss();

        LISTENER.onStopRequested();
    }

    public void setPort(int port) {
        this.PORT = port;

        showAddress();
    }

    public void setIP(String ip) {
        this.IP = ip;

        showAddress();
    }

    public void setAmount(int amount) {
        ((TextView) VIEW.findViewById(R.id.title)).setText(CONTEXT.getString(R.string.popup_wifi_share_title) + " " + amount + " " + CONTEXT.getString(amount == 1 ? R.string.kapture : R.string.kapture_plural));
    }

    public void setKSecurity(KSecurity ks) {
        VIEW.findViewById(R.id.passwordContainer).setVisibility(View.VISIBLE);

        this.KSECURITY = ks;
    }

    public void refreshPasswordField() {
        if(KSECURITY != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if(IS_PASSWORD_VISIBLE) {
                    PASSWORD_EL.setText(KSECURITY.getPasswordFormated());
                    BTN_TOGGLE_PASSWORD.setImageResource(R.drawable.icon_wifi_share_password_show);
                    BTN_TOGGLE_PASSWORD.setContentDescription(CONTEXT.getString(R.string.tooltip_hide_password));
                } else {
                    PASSWORD_EL.setText(KSECURITY.getHiddenPassword());
                    BTN_TOGGLE_PASSWORD.setImageResource(R.drawable.icon_wifi_share_password_hide);
                    BTN_TOGGLE_PASSWORD.setContentDescription(CONTEXT.getString(R.string.tooltip_show_password));
                }
            });
        }
    }

    public void setListener(OnWiFiSharePopupListener listener) {
        this.LISTENER = listener;
    }

    private void showAddress() {
        ((TextView) VIEW.findViewById(R.id.ip)).setText("http://" + IP + ":" + PORT);
    }
}
