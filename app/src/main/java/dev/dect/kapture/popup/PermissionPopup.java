package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import java.util.Objects;

import dev.dect.kapture.R;

@SuppressLint("InflateParams")
public class PermissionPopup extends Dialog {
    public PermissionPopup(Context ctx, Runnable actionContinue) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_permission, null);

        final AppCompatButton btnContinue = view.findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener((v) -> actionContinue.run());

        this.setCancelable(false);

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.activity_background));
    }
}
