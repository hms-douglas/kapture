package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.PickerItemAdapter;
import dev.dect.kapture.popup.utils.ScrollablePopup;

@SuppressLint("InflateParams")
public class PickerPopup extends ScrollablePopup {
    public interface OnPickerPopupListener {
        void onPickerPicked(int indexPicked);
    }

    public PickerPopup(Context ctx, int title, String[] displayNames, int activeIndex, OnPickerPopupListener l) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_picker, null);

        super.setNestedScroller(view.findViewById(R.id.popupContainer));

        ((TextView) view.findViewById(R.id.popupTitle)).setText(title);

        view.findViewById(R.id.popupBtnNo).setOnClickListener((v) -> this.dismiss());

        final PickerItemAdapter pickerItemAdapter = new PickerItemAdapter(displayNames, activeIndex);

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setAdapter(pickerItemAdapter);

        recyclerView.setOnClickListener((v) -> {
            final int i = pickerItemAdapter.getSelected();

            if(i != activeIndex) {
                l.onPickerPicked(i);

                this.dismiss();
            }
        });

        this.setContentView(view);
    }
}
