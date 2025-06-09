package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.wear.widget.CurvedTextView;

import com.google.android.gms.wearable.Node;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.zxing.EncodeHintType;

import net.glxn.qrgen.android.QRCode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.server.KSecurity;
import dev.dect.kapture.utils.Utils;

@SuppressLint({"InflateParams", "SetTextI18n"})
public class WiFiSharePopup extends Dialog {
    public interface OnWiFiSharePopupListener {
        void onStopRequested();
    }

    private final Context CONTEXT;

    private final View VIEW;

    private final TextView PASSWORD_EL;

    private final ImageButton BTN_TOGGLE_PASSWORD;

    private final CurvedTextView CLOCK_EL;

    private final Node[] PHONE_NODE = new Node[1];

    private final BottomSheetBehavior<View> BOTTOM_SHEET_EL;

    private final ConstraintLayout BOTTOM_SHEET_BACKGROUND_EL;

    private final ImageView QR_CODE;

    private String IP = "";

    private int PORT = 0;

    private boolean IS_PASSWORD_VISIBLE = false;

    private OnWiFiSharePopupListener LISTENER;

    private KSecurity KSECURITY;

    private final BroadcastReceiver CLOCK_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        updateClock();
        }
    };

    public WiFiSharePopup(Context ctx) {
        super(ctx, R.style.Theme_Translucent);

        this.CONTEXT = ctx;

        Utils.Remote.getPhoneNodeAsync(CONTEXT, PHONE_NODE);

        VIEW = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_wifi_share, null);

        this.PASSWORD_EL = VIEW.findViewById(R.id.password);
        this.BTN_TOGGLE_PASSWORD = VIEW.findViewById(R.id.btnPassword);

        this.CLOCK_EL = VIEW.findViewById(R.id.clock);

        this.BOTTOM_SHEET_EL = BottomSheetBehavior.from(VIEW.findViewById(R.id.bottomSheet));
        this.BOTTOM_SHEET_BACKGROUND_EL = VIEW.findViewById(R.id.bottomSheetBackground);

        this.QR_CODE = VIEW.findViewById(R.id.qr_code);

        VIEW.findViewById(R.id.popupBtnStop).setOnClickListener((v) -> {
            this.dismiss();

            try {
                CONTEXT.unregisterReceiver(CLOCK_RECEIVER);
            } catch (Exception ignore) {}

            try {
                Objects.requireNonNull(getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } catch (Exception ignore) {}

            LISTENER.onStopRequested();
        });

        VIEW.findViewById(R.id.popupBtnOnPhone).setOnClickListener((v) -> {
            Utils.Remote.openLinkOnPhone(
                CONTEXT,
                PHONE_NODE,
                    "http://" + IP + ":" + PORT + (KSECURITY == null ? "" : "?" + KSecurity.TOKEN_ID + "=" + KSECURITY.getToken()),
                false
            );
        });

        VIEW.findViewById(R.id.btnPassword).setOnClickListener((v) -> {
            IS_PASSWORD_VISIBLE = !IS_PASSWORD_VISIBLE;

            refreshPasswordField();
        });

        BOTTOM_SHEET_EL.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    VIEW.findViewById(R.id.bottomSheet).setBackgroundTintList(ColorStateList.valueOf(CONTEXT.getColor(R.color.activity_background)));
                } else {
                    VIEW.findViewById(R.id.bottomSheet).setBackgroundTintList(ColorStateList.valueOf(CONTEXT.getColor(R.color.bottom_sheet_background_qr)));
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                BOTTOM_SHEET_BACKGROUND_EL.setAlpha(1f - slideOffset);
                QR_CODE.setAlpha(slideOffset);
            }
        });

        VIEW.findViewById(R.id.bottomSheetToggle).setOnClickListener((v) -> bottomSheetToggle());

        this.setCancelable(false);

        this.setContentView(VIEW);
    }

    @Override
    public void show() {
        super.show();

        try {
            Objects.requireNonNull(getWindow()).addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception ignore) {}

        try {
            final IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);

            CONTEXT.registerReceiver(CLOCK_RECEIVER, filter);

            updateClock();
        } catch (Exception ignore) {
            CLOCK_EL.setVisibility(View.GONE);
        }
    }



    public void setPort(int port) {
        this.PORT = port;

        showAddress();
    }

    public void setIP(String ip) {
        this.IP = ip;

        showAddress();
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

                QR_CODE.setImageBitmap(
                    QRCode.from(
                    "URL:http://" + IP + ":" + PORT + ("?" + KSecurity.TOKEN_ID + "=" + KSECURITY.getToken())
                    ).withHint(EncodeHintType.MARGIN, 0)
                    .withSize(300,300)
                    .bitmap()
                );
            });
        } else {
            QR_CODE.setImageBitmap(
                QRCode.from(
                "URL:http://" + IP + ":" + PORT
                ).withHint(EncodeHintType.MARGIN, 0)
                .withSize(300,300)
                .bitmap()
            );
        }
    }

    public void setListener(OnWiFiSharePopupListener listener) {
        this.LISTENER = listener;
    }

    private void showAddress() {
        ((TextView) VIEW.findViewById(R.id.ip)).setText("http://" + IP + ":" + PORT);
    }

    private void updateClock() {
        CLOCK_EL.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
    }

    private void bottomSheetToggle() {
        BOTTOM_SHEET_EL.setState(
            BOTTOM_SHEET_EL.getState() == BottomSheetBehavior.STATE_EXPANDED
            ?  BottomSheetBehavior.STATE_COLLAPSED
            : BottomSheetBehavior.STATE_EXPANDED
        );
    }
}
