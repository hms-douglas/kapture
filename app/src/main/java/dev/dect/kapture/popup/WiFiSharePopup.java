package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class WiFiSharePopup extends Dialog {
    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    public WiFiSharePopup(Context ctx, String ip, int port, int amountSharing, Runnable btnStop) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_wifi_share, null);

        this.POPUP_CONTAINER = view.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = view.findViewById(R.id.popup);

        ((TextView) view.findViewById(R.id.title)).setText(ctx.getString(R.string.popup_wifi_share_title) + " " + amountSharing + " " + ctx.getString(amountSharing == 1 ? R.string.kapture : R.string.kapture_plural));
        ((TextView) view.findViewById(R.id.ip)).setText("http://" + ip + ":" + port);

        view.findViewById(R.id.popupBtnStop).setOnClickListener((v) -> this.dismissWithAnimation(btnStop));

        this.setCancelable(false);

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void dismissWithAnimation(Runnable btnStop) {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW, btnStop);
    }
}
