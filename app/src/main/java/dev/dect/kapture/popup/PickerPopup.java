package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.PickerItemAdapter;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class PickerPopup extends Dialog {
    public interface OnPickerPopupListener {
        void onPickerPicked(int indexPicked);
    }

    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    public PickerPopup(Context ctx, int title, String[] displayNames, int activeIndex, OnPickerPopupListener l) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_picker, null);

        this.POPUP_CONTAINER = view.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = view.findViewById(R.id.popup);

        POPUP_CONTAINER.setOnClickListener((v) -> this.dismissWithAnimation());

        ((TextView) view.findViewById(R.id.popupTitle)).setText(title);

        final AppCompatButton btnMain = view.findViewById(R.id.popupBtnMain);

        btnMain.setOnClickListener((v) -> this.dismissWithAnimation());

        if(displayNames.length == 1) {
            btnMain.setText(R.string.popup_btn_ok);
            btnMain.setAllCaps(true);
        }

        final PickerItemAdapter pickerItemAdapter = new PickerItemAdapter(displayNames, activeIndex);

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        recyclerView.setAdapter(pickerItemAdapter);

        recyclerView.setOnClickListener((v) -> {
            final int i = pickerItemAdapter.getSelected();

            if(i != activeIndex) {
                l.onPickerPicked(i);

                this.dismissWithAnimation();
            }
        });

        Utils.Popup.setMaxHeight(ctx, view.findViewById(R.id.popup));

        Utils.Popup.setInAnimation(this, POPUP_CONTAINER, POPUP_VIEW);

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);
    }
}
