package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;

@SuppressLint({"InflateParams", "SetTextI18n"})
public class ProgressPopup extends Dialog {
    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    private final ProgressBar PROGRESS_BAR;

    private final TextView PROGRESS;

    private Runnable CANCEL_RUNNABLE;

    public ProgressPopup(Context ctx, int title) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_progress, null);

        ((TextView) view.findViewById(R.id.popupTitle)).setText(title);

        final AppCompatButton btn = view.findViewById(R.id.popupBtnNo);

        btn.setOnClickListener((v) -> {
            this.dismissWithAnimation();

            if(this.CANCEL_RUNNABLE != null) {
                this.CANCEL_RUNNABLE.run();
            }
        });

        this.POPUP_CONTAINER = view.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = view.findViewById(R.id.popup);
        this.PROGRESS = view.findViewById(R.id.popupProgress);
        this.PROGRESS_BAR = view.findViewById(R.id.popupProgressBar);

        PROGRESS_BAR.setMin(0);
        PROGRESS_BAR.setMax(100);
        PROGRESS_BAR.setProgress(0);

        PROGRESS.setText("0%");

        Utils.Popup.setMaxHeight(ctx, view.findViewById(R.id.popup));

        this.setContentView(view);

        this.setCancelable(false);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void setCancelRunnable(Runnable runnable) {
        this.CANCEL_RUNNABLE = runnable;
    }

    public void setMax(int max) {
        this.PROGRESS_BAR.setMax(max);
    }

    public void setValue(double value) {
        setValue((int) value);
    }

    public void setValue(int value) {
        PROGRESS_BAR.setProgress(value, true);

        PROGRESS.setText(((value * 100) / this.PROGRESS_BAR.getMax()) + "%");
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);
    }
}
