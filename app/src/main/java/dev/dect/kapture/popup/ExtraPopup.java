package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.adapter.ExtraItemAdapter;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class ExtraPopup extends Dialog {
    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    private final Runnable BTN_CLOSE;

    public ExtraPopup(Context ctx, ArrayList<Kapture.Extra> extras, @Nullable Runnable btnClose) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_extra, null);

        this.POPUP_CONTAINER = view.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = view.findViewById(R.id.popup);

        this.BTN_CLOSE = btnClose;

        final AppCompatButton close = view.findViewById(R.id.popupBtnMain);

        close.setOnClickListener((v) -> dismissWithAnimation());

        POPUP_CONTAINER.setOnClickListener((v) -> close.callOnClick());

        final ExtraItemAdapter extraItemAdapter = new ExtraItemAdapter(extras, this);

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        recyclerView.setAdapter(extraItemAdapter);

        Utils.Popup.setMaxHeight(ctx, view.findViewById(R.id.popup));

        Utils.Popup.setInAnimation(this, POPUP_CONTAINER, POPUP_VIEW);

        this.setCancelable(false);

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW, () -> {
            if(BTN_CLOSE != null) {
                BTN_CLOSE.run();
            }
        });
    }
}