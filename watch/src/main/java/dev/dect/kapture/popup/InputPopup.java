package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import dev.dect.kapture.R;
import dev.dect.kapture.popup.utils.ScrollablePopup;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class InputPopup extends ScrollablePopup {
    public interface OnInputPopupListener {
        default void onIntInputSet(int input) {}
    }

    public static final int NO_TEXT = -1,
                            RES_ID_DEFAULT = -1;

    private final EditText INPUT;

    private final ImageButton BTN_YES;

    public InputPopup(Context ctx, int title, int resIdYes, OnInputPopupListener btnYes, int resIdNo, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_input, null);

        super.setNestedScroller(view.findViewById(R.id.popupContainer));

        final TextView titleEl = view.findViewById(R.id.popupTitle);

        if(title == NO_TEXT) {
            titleEl.setText("");
        } else {
            titleEl.setText(title);

            if(isDangerousAction) {
                titleEl.setTextColor(ctx.getColor(R.color.popup_btn_text_dangerous));
            }
        }

        INPUT = view.findViewById(R.id.popupInput);

        BTN_YES = view.findViewById(R.id.popupBtnYes);

        if(resIdYes != RES_ID_DEFAULT) {
            BTN_YES.setImageResource(resIdYes);
        }

        final ImageButton buttonNo = view.findViewById(R.id.popupBtnNo);

        if(resIdNo != RES_ID_DEFAULT) {
            buttonNo.setImageResource(resIdNo);
        }

        buttonNo.setOnClickListener((v) -> {
            BTN_YES.setTag("button");

            this.dismiss();

            if(btnNo != null) {
                btnNo.run();
            }
        });

        INPUT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    BTN_YES.setEnabled(false);
                    BTN_YES.setAlpha(0.5f);
                    BTN_YES.setBackgroundTintList(ColorStateList.valueOf(ctx.getColor(R.color.popup_btn_disabled)));
                } else if(s.length() == 1) {
                    BTN_YES.setEnabled(true);
                    BTN_YES.setAlpha(1f);
                    BTN_YES.setBackgroundTintList(ColorStateList.valueOf(ctx.getColor(R.color.popup_btn_yes)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if(dismissible) {
            if(dismissibleCallsNo) {
                this.setOnDismissListener((dialogInterface) -> {
                    if(BTN_YES.getTag() == null) {
                        if(btnNo != null) {
                            btnNo.run();
                        }
                    }
                });
            }
        } else {
            this.setCancelable(false);
        }

        this.setOnShowListener((d) -> Utils.Keyboard.requestShowForInput(INPUT));

        this.setContentView(view);
    }

    public static class NumberInteger extends InputPopup {
        public NumberInteger(Context ctx, int title, int value, int min, int max, int resIdYes, OnInputPopupListener btnYes, int resIdNo, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
            super(ctx, title, resIdYes, btnYes, resIdNo, btnNo, dismissible, dismissibleCallsNo, isDangerousAction);

            super.INPUT.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE && super.BTN_YES.isEnabled()) {
                    super.BTN_YES.setTag("button");

                    this.dismiss();

                    btnYes.onIntInputSet(Integer.parseInt(super.INPUT.getText().toString()));

                    return true;
                }

                return false;
            });

            super.BTN_YES.setOnClickListener((v) -> {
                if(super.BTN_YES.isEnabled()) {
                    super.BTN_YES.setTag("button");

                    this.dismiss();

                    btnYes.onIntInputSet(Integer.parseInt(super.INPUT.getText().toString()));
                }
            });

            super.INPUT.setInputType(InputType.TYPE_CLASS_NUMBER);

            super.INPUT.setFilters(new InputFilter[]{ new MinMaxFilter(min, max)});

            super.INPUT.setText(String.valueOf(value));
        }

        private static class MinMaxFilter implements InputFilter {
            private final int MIN,
                              MAX;

            public MinMaxFilter(int min, int max) {
                this.MIN = min;
                this.MAX = max;
            }

            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                try {
                    final int value = Integer.parseInt(spanned.toString() + charSequence.toString());

                    if(Utils.isInRange(value, MIN, MAX)) {
                        return null;
                    }
                } catch (NumberFormatException ignore) {}

                return "";
            }
        }
    }
}
