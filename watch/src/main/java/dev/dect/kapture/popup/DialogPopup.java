package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import dev.dect.kapture.R;
import dev.dect.kapture.popup.utils.ScrollablePopup;

@SuppressLint("InflateParams")
public class DialogPopup extends ScrollablePopup {
    public static final int NO_TEXT = -1,
                            RES_ID_DEFAULT = -1;

    public DialogPopup(Context ctx, int title, int text, int resIdYes, Runnable btnYes, int resIdNo, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
        this(ctx, title, ctx.getString(text), resIdYes, btnYes, resIdNo, btnNo, dismissible, dismissibleCallsNo, isDangerousAction);
    }

    public DialogPopup(Context ctx, int title, String text, int resIdYes, Runnable btnYes, int resIdNo, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
        super(ctx);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_dialog, null);

        super.setNestedScroller(view.findViewById(R.id.popupContainer));

        final TextView titleEl = view.findViewById(R.id.popupTitle),
                       textEl = view.findViewById(R.id.popupText);

        if(title == NO_TEXT) {
            titleEl.setText("");

            if(isDangerousAction) {
                textEl.setTextColor(ctx.getColor(R.color.popup_btn_text_dangerous));
            }
        } else {
            titleEl.setText(title);

            if(isDangerousAction) {
                titleEl.setTextColor(ctx.getColor(R.color.popup_btn_text_dangerous));
            }
        }

        textEl.setText(text);

        final ImageButton buttonYes = view.findViewById(R.id.popupBtnYes);

        if(resIdYes != RES_ID_DEFAULT) {
            buttonYes.setImageResource(resIdYes);
        }

        buttonYes.setOnClickListener((v) -> {
            buttonYes.setTag("button");

            this.dismiss();

            if(btnYes != null) {
                btnYes.run();
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
