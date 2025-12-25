package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class InputPopup extends Dialog {
    public interface OnInputPopupListener {
        default void onStringInputSet(String input) {}

        default void onIntInputSet(int input) {}
    }

    public static final int NO_TEXT = -1;

    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    private final EditText INPUT;

    private final AppCompatButton BTN_YES;

    public InputPopup(Context ctx, int title, int btnYesText, OnInputPopupListener btnYes, int btnNoText, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_input, null);

        if(title == NO_TEXT) {
            view.findViewById(R.id.popupTitle).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.popupTitle)).setText(title);
        }

        INPUT = view.findViewById(R.id.popupInput);

        BTN_YES = view.findViewById(R.id.popupBtnYes);

        BTN_YES.setText(btnYesText);

        if(isDangerousAction) {
            BTN_YES.setTextColor(ctx.getColor(R.color.popup_btn_text_dangerous));
        }

        if(btnNoText == NO_TEXT) {
            view.findViewById(R.id.popupBtnNo).setVisibility(View.GONE);

            BTN_YES.setAllCaps(true);
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

        INPUT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    BTN_YES.setEnabled(false);
                    BTN_YES.setTextColor(ctx.getColor(R.color.popup_btn_text_disabled));
                } else if(s.length() > 0) {
                    BTN_YES.setEnabled(true);
                    BTN_YES.setTextColor(ctx.getColor(R.color.popup_btn_text));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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

        this.setCancelable(dismissible);

        Utils.Popup.setMaxHeight(ctx, view.findViewById(R.id.popup));

        this.setOnShowListener((d) -> Utils.Keyboard.requestShowForInput(INPUT));

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);
    }

    public static class Text extends InputPopup {
        public Text(Context ctx, int title, String text, int btnYesText, OnInputPopupListener btnYes, int btnNoText, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
            super(ctx, title, btnYesText, btnYes, btnNoText, btnNo, dismissible, dismissibleCallsNo, isDangerousAction);

            super.INPUT.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE && super.BTN_YES.isEnabled()) {
                    this.dismiss();

                    btnYes.onStringInputSet(super.INPUT.getText().toString());

                    return true;
                }

                return false;
            });

            super.BTN_YES.setOnClickListener((v) -> {
                if(super.BTN_YES.isEnabled()) {
                    this.dismissWithAnimation();

                    btnYes.onStringInputSet(super.INPUT.getText().toString());
                }
            });

            if(text != null) {
                super.INPUT.setText(text);

                if(text.isEmpty()) {
                    super.BTN_YES.setEnabled(false);
                    super.BTN_YES.setTextColor(ctx.getColor(R.color.popup_btn_text_disabled));
                }
            }
        }
    }

    public static class TextMultiLine extends InputPopup {
        public TextMultiLine(Context ctx, int title, String text, int btnYesText, OnInputPopupListener btnYes, int btnNoText, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
            super(ctx, title, btnYesText, btnYes, btnNoText, btnNo, dismissible, dismissibleCallsNo, isDangerousAction);

            super.INPUT.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE && super.BTN_YES.isEnabled()) {
                    this.dismiss();

                    btnYes.onStringInputSet(super.INPUT.getText().toString());

                    return true;
                }

                return false;
            });

            super.BTN_YES.setOnClickListener((v) -> {
                if(super.BTN_YES.isEnabled()) {
                    this.dismissWithAnimation();

                    btnYes.onStringInputSet(super.INPUT.getText().toString());
                }
            });

            if(text != null) {
                super.INPUT.setText(text);

                if(text.isEmpty()) {
                    super.BTN_YES.setEnabled(false);
                    super.BTN_YES.setTextColor(ctx.getColor(R.color.popup_btn_text_disabled));
                }
            }

            super.INPUT.setSingleLine(false);
            super.INPUT.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }
    }

    public static class NumberInteger extends InputPopup {
        public NumberInteger(Context ctx, int title, int value, int btnYesText, OnInputPopupListener btnYes, int btnNoText, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
            super(ctx, title, btnYesText, btnYes, btnNoText, btnNo, dismissible, dismissibleCallsNo, isDangerousAction);

            super.INPUT.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE && super.BTN_YES.isEnabled()) {
                    this.dismiss();

                    btnYes.onIntInputSet(Integer.parseInt(super.INPUT.getText().toString()));

                    return true;
                }

                return false;
            });

            super.BTN_YES.setOnClickListener((v) -> {
                if(super.BTN_YES.isEnabled()) {
                    this.dismissWithAnimation();

                    btnYes.onIntInputSet(Integer.parseInt(super.INPUT.getText().toString()));
                }
            });

            super.INPUT.setInputType(InputType.TYPE_CLASS_NUMBER);

            super.INPUT.setText(String.valueOf(value));
        }

        public NumberInteger(Context ctx, int title, int value, int min, int max, int btnYesText, OnInputPopupListener btnYes, int btnNoText, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
            super(ctx, title, btnYesText, btnYes, btnNoText, btnNo, dismissible, dismissibleCallsNo, isDangerousAction);

            super.INPUT.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE && super.BTN_YES.isEnabled()) {
                    this.dismiss();

                    btnYes.onIntInputSet(Integer.parseInt(super.INPUT.getText().toString()));

                    return true;
                }

                return false;
            });

            super.BTN_YES.setOnClickListener((v) -> {
                if(super.BTN_YES.isEnabled()) {
                    this.dismissWithAnimation();

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
