package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.wearable.input.RotaryEncoderHelper;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;
import dev.dect.kapture.view.KTimePicker;

@SuppressLint({"InflateParams", "ClickableViewAccessibility"})
public class TimePopup extends Dialog {
    public interface OnTimePopupListener {
        default void onTimeSet(int seconds) {}
    }

    public static final int NO_TEXT = -1,
                            TYPE_FULL = 0,
                            TYPE_MINUTE_SECOND = 1,
                            TYPE_SECOND = 2,
                            RES_ID_DEFAULT = -1;

    private final String TAG_SELECTED = "tag";

    private final KTimePicker HOUR_EL,
                              MINUTE_EL,
                              SECOND_EL;

    @Override
    public boolean onGenericMotionEvent(@NonNull MotionEvent event) {
        final int i = RotaryEncoderHelper.getRotaryAxisValue(event) < 0 ? 1 : -1;

        if(HOUR_EL.getTag() != null) {
            HOUR_EL.setValue(HOUR_EL.getValue() + i);
        } else if(MINUTE_EL.getTag() != null) {
            MINUTE_EL.setValue(MINUTE_EL.getValue() + i);
        } else if(SECOND_EL.getTag() != null) {
            SECOND_EL.setValue(SECOND_EL.getValue() + i);
        }

        return true;
    }

    public TimePopup(Context ctx, int title, int type, int time, int resIdYes, OnTimePopupListener btnYes, int resIdNo, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_time, null);

        final TextView titleEl = view.findViewById(R.id.popupTitle);

        if(title == NO_TEXT) {
            titleEl.setText("");
        } else {
            titleEl.setText(title);
        }

        HOUR_EL = view.findViewById(R.id.hour);
        MINUTE_EL = view.findViewById(R.id.minute);
        SECOND_EL = view.findViewById(R.id.second);

        HOUR_EL.setOnTouchListener((view1, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                HOUR_EL.setTag(TAG_SELECTED);
                MINUTE_EL.setTag(null);
                SECOND_EL.setTag(null);

                HOUR_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_focused));
                MINUTE_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_not_focused));
                SECOND_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_not_focused));
            }

            return false;
        });

        MINUTE_EL.setOnTouchListener((view1, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                HOUR_EL.setTag(null);
                MINUTE_EL.setTag(TAG_SELECTED);
                SECOND_EL.setTag(null);

                HOUR_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_not_focused));
                MINUTE_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_focused));
                SECOND_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_not_focused));
            }

            return false;
        });

        SECOND_EL.setOnTouchListener((view1, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                HOUR_EL.setTag(null);
                MINUTE_EL.setTag(null);
                SECOND_EL.setTag(TAG_SELECTED);

                HOUR_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_not_focused));
                MINUTE_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_not_focused));
                SECOND_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_focused));
            }

            return false;
        });

        switch(type) {
            case TYPE_FULL:
                HOUR_EL.setTag(TAG_SELECTED);
                HOUR_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_focused));
                break;

            case TYPE_MINUTE_SECOND:
                HOUR_EL.setVisibility(View.GONE);

                MINUTE_EL.setTag(TAG_SELECTED);
                MINUTE_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_focused));

                view.findViewById(R.id.hourMinuteDivisor).setVisibility(View.GONE);
                break;

            case TYPE_SECOND:
                HOUR_EL.setVisibility(View.GONE);

                SECOND_EL.setTag(TAG_SELECTED);
                SECOND_EL.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_focused));

                view.findViewById(R.id.hourMinuteDivisor).setVisibility(View.GONE);

                MINUTE_EL.setVisibility(View.GONE);

                view.findViewById(R.id.minuteSecondDivisor).setVisibility(View.GONE);
                break;
        }

        final int[] hms = Utils.Converter.secondsToHMS(time);

        HOUR_EL.setValue(hms[0]);
        MINUTE_EL.setValue(hms[1]);
        SECOND_EL.setValue(hms[2]);

        final ImageButton buttonYes = view.findViewById(R.id.popupBtnYes);

        if(resIdYes != RES_ID_DEFAULT) {
            buttonYes.setImageResource(resIdYes);
        }

        buttonYes.setOnClickListener((v) -> {
            if(buttonYes.isEnabled()) {
                this.dismiss();

                btnYes.onTimeSet((HOUR_EL.getValue() * 3600) + (MINUTE_EL.getValue() * 60) + SECOND_EL.getValue());
            }
        });

        final ImageButton buttonNo = view.findViewById(R.id.popupBtnNo);

        if(resIdNo != RES_ID_DEFAULT) {
            buttonNo.setImageResource(resIdNo);
        }

        buttonNo.setOnClickListener((v) -> {
            buttonNo.setTag("button");

            this.dismiss();

            if(btnNo != null) {
                btnNo.run();
            }
        });

        if(dismissible) {
            if(dismissibleCallsNo) {
                this.setOnDismissListener((dialogInterface) -> {
                    if(buttonNo.getTag() == null) {
                        if(btnNo != null) {
                            btnNo.run();
                        }
                    }
                });
            }
        } else {
            this.setCancelable(false);
        }

        this.setContentView(view);
    }
}
