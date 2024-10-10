package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;
import dev.dect.kapture.view.KTimePicker;

@SuppressLint("InflateParams")
public class TimePopup extends Dialog {
    public interface OnTimePopupListener {
        default void onTimeSet(int seconds) {}
    }

    public static final int NO_TEXT = -1,
                            TYPE_FULL = 0,
                            TYPE_MINUTE_SECOND = 1,
                            TYPE_SECOND = 2;

    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    public TimePopup(Context ctx, int title, int type, int time, int btnYesText, OnTimePopupListener btnYes, int btnNoText, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_time, null);

        if(title == NO_TEXT) {
            view.findViewById(R.id.popupTitle).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.popupTitle)).setText(title);
        }

        final KTimePicker hour = view.findViewById(R.id.hour),
                          minute = view.findViewById(R.id.minute),
                          second = view.findViewById(R.id.second);

        switch(type) {
            case TYPE_MINUTE_SECOND:
                hour.setVisibility(View.GONE);
                view.findViewById(R.id.hourMinuteDivisor).setVisibility(View.GONE);
                break;

            case TYPE_SECOND:
                hour.setVisibility(View.GONE);
                view.findViewById(R.id.hourMinuteDivisor).setVisibility(View.GONE);

                minute.setVisibility(View.GONE);
                view.findViewById(R.id.minuteSecondDivisor).setVisibility(View.GONE);
                break;
        }

        final int[] hms = Utils.Converter.secondsToHMS(time);

        hour.setValue(hms[0]);
        minute.setValue(hms[1]);
        second.setValue(hms[2]);

        final AppCompatButton buttonYes = view.findViewById(R.id.popupBtnYes);

        buttonYes.setText(btnYesText);

        buttonYes.setOnClickListener((v) -> {
            if(buttonYes.isEnabled()) {
                this.dismissWithAnimation();

                btnYes.onTimeSet((hour.getValue() * 3600) + (minute.getValue() * 60) + second.getValue());
            }
        });

        if(btnNoText == NO_TEXT) {
            view.findViewById(R.id.popupBtnNo).setVisibility(View.GONE);

            buttonYes.setAllCaps(true);
        } else {
            final AppCompatButton buttonNo = view.findViewById(R.id.popupBtnNo);

            buttonNo.setText(btnNoText);

            buttonNo.setOnClickListener((v) -> {
                this.dismissWithAnimation();

                if(btnNo != null) {
                    btnNo.run();
                }
            });
        }

        this.POPUP_CONTAINER = view.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = view.findViewById(R.id.popup);

        if(dismissible) {
            POPUP_CONTAINER.setOnClickListener((v) -> {
                if(dismissibleCallsNo) {
                    if(btnNo != null) {
                        btnNo.run();
                    }
                } else {
                    this.dismissWithAnimation();
                }
            });
        }

        Utils.Popup.setMaxHeight(ctx, view.findViewById(R.id.popup));

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);
    }
}
