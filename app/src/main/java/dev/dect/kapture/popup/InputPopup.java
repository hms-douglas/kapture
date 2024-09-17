package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
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
public class InputPopup {
    public interface OnInputPopupListener {
        void onTextSet(String input);
    }

    public static class Text extends Dialog {
        private final ConstraintLayout POPUP_VIEW,
                                       POPUP_CONTAINER;

        public Text(Context ctx, int title, String text, int btnYesText, OnInputPopupListener btnYes, int btnNoText, @Nullable Runnable btnNo, boolean dismissible, boolean dismissibleCallsNo, boolean isDangerousAction) {
            super(ctx, R.style.Theme_Translucent);

            final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_input, null);

            if(title == -1) {
                view.findViewById(R.id.popupTitle).setVisibility(View.GONE);
            } else {
                ((TextView) view.findViewById(R.id.popupTitle)).setText(title);
            }

            final EditText input = view.findViewById(R.id.popupInput);

            final AppCompatButton buttonYes = view.findViewById(R.id.popupBtnYes);

            buttonYes.setText(btnYesText);

            if(isDangerousAction) {
                buttonYes.setTextColor(ctx.getColor(R.color.popup_btn_text_dangerous));
            }

            buttonYes.setOnClickListener((v) -> {
                if(buttonYes.isEnabled()) {
                    this.dismissWithAnimation();

                    btnYes.onTextSet(input.getText().toString());
                }
            });

            if(btnNoText == -1) {
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

            input.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE && buttonYes.isEnabled()) {
                    this.dismiss();

                    btnYes.onTextSet(input.getText().toString());

                    return true;
                }

                return false;
            });

            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() == 0) {
                        buttonYes.setEnabled(false);
                        buttonYes.setTextColor(ctx.getColor(R.color.popup_btn_text_disabled));
                    } else if(s.length() == 1) {
                        buttonYes.setEnabled(true);
                        buttonYes.setTextColor(ctx.getColor(R.color.popup_btn_text));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            if(text != null) {
                input.setText(text);

                if(text.length() == 0) {
                    buttonYes.setEnabled(false);
                    buttonYes.setTextColor(ctx.getColor(R.color.popup_btn_text_disabled));
                }
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

            Utils.Popup.setInAnimation(this, POPUP_CONTAINER, POPUP_VIEW, () -> Utils.Keyboard.requestShowForInput(input));

            this.setContentView(view);

            Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
        }

        public void dismissWithAnimation() {
            Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);
        }
    }
}
