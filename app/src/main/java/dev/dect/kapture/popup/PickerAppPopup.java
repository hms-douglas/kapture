package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.PickerAppItemAdapter;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class PickerAppPopup extends Dialog {
    public interface OnPickerAppPopupListener {
        void onPicked(ArrayList<String> packages);
    }
    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    public static final int NO_TEXT = -1;

    public PickerAppPopup(Context ctx, int title, int btnYesText, int max, boolean atLeastOne, ArrayList<String> selected, OnPickerAppPopupListener l) {
        this(ctx, ctx.getString(title), btnYesText, max, atLeastOne, selected, l);
    }

    public PickerAppPopup(Context ctx, String title, int btnYesText, int max, boolean atLeastOne, ArrayList<String> selected, OnPickerAppPopupListener l) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_picker_app, null);

        this.POPUP_CONTAINER = view.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = view.findViewById(R.id.popup);

        POPUP_CONTAINER.setOnClickListener((v) -> this.dismissWithAnimation());

        ((TextView) view.findViewById(R.id.popupTitle)).setText(title);

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        final PickerAppItemAdapter pickerItemAdapter = new PickerAppItemAdapter(ctx, max, selected);

        if(max == 1) {
            view.findViewById(R.id.popupBtnYes).setVisibility(View.GONE);
            view.findViewById(R.id.popupBtnDivisor).setVisibility(View.GONE);

            recyclerView.setOnClickListener((v) -> {
                this.dismissWithAnimation();
                l.onPicked(selected);
            });
        } else {
            final AppCompatButton btnYes = view.findViewById(R.id.popupBtnYes);

            btnYes.setText(btnYesText);

            btnYes.setOnClickListener((v) -> {
                if(atLeastOne && selected.isEmpty()) {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_error_select_one_app), Toast.LENGTH_SHORT).show();
                } else {
                    this.dismissWithAnimation();
                    l.onPicked(selected);
                }
            });
        }

        view.findViewById(R.id.popupBtnNo).setOnClickListener((v) -> dismissWithAnimation());

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        recyclerView.setAdapter(pickerItemAdapter);

        Utils.Popup.setMaxHeight(ctx, view.findViewById(R.id.popup));

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);
    }
}
