package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.PickerAppItemAdapter;
import dev.dect.kapture.popup.utils.ScrollablePopup;

@SuppressLint("InflateParams")
public class PickerAppPopup extends ScrollablePopup {
    public interface OnPickerAppPopupListener {
        void onPicked(ArrayList<String> packages);
    }

    public static final int RES_ID_DEFAULT = -1;

    public PickerAppPopup(Context ctx, int title, int resIdYes, int max, boolean atLeastOne, ArrayList<String> selected, OnPickerAppPopupListener l) {
        this(ctx, ctx.getString(title), resIdYes, max, atLeastOne, selected, l);
    }

    public PickerAppPopup(Context ctx, String title, int resIdYes, int max, boolean atLeastOne, ArrayList<String> selected, OnPickerAppPopupListener l) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_picker_app, null);

        super.setNestedScroller(view.findViewById(R.id.popupContainer));

        ((TextView) view.findViewById(R.id.popupTitle)).setText(title);

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        if(max == 1) {
            view.findViewById(R.id.popupBtnYes).setVisibility(View.GONE);

            view.findViewById(R.id.spaceMiddle).setVisibility(View.GONE);

            recyclerView.setOnClickListener((v) -> {
                this.dismiss();
                l.onPicked(selected);
            });
        } else {
            final ImageButton btnYes = view.findViewById(R.id.popupBtnYes);

            if(resIdYes != RES_ID_DEFAULT) {
                btnYes.setImageResource(resIdYes);
            }

            btnYes.setOnClickListener((v) -> {
                if(atLeastOne && selected.isEmpty()) {
                    Toast.makeText(ctx, ctx.getString(R.string.toast_error_select_one_app), Toast.LENGTH_SHORT).show();
                } else {
                    this.dismiss();
                    l.onPicked(selected);
                }
            });
        }

        view.findViewById(R.id.popupBtnNo).setOnClickListener((v) -> dismiss());

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        recyclerView.setNestedScrollingEnabled(false);

        new Handler(Looper.getMainLooper()).post(() -> recyclerView.setAdapter(new PickerAppItemAdapter(ctx, max, selected)));

        this.setContentView(view);
    }
}
