package dev.dect.kapture.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.Utils;

@SuppressLint("InflateParams")
public class SortPopup extends Dialog {
    public interface OnSortPopupListener {
        void onSorted(int sortBy, boolean asc);
    }

    public static final int SORT_NAME = 0,
                            SORT_DATE_CAPTURING = 1,
                            SORT_SIZE = 2,
                            SORT_DURATION = 3;

    private final ConstraintLayout POPUP_VIEW,
                                   POPUP_CONTAINER;

    private final ImageView RADIO_ASC,
                            RADIO_DESC,
                            RADIO_NAME,
                            RADIO_DATE_CAPTURING,
                            RADIO_SIZE,
                            RADIO_DURATION;

    private int SORT_BY;

    private boolean ASC;

    public SortPopup(Context ctx, int sortBy, boolean asc, OnSortPopupListener l) {
        super(ctx, R.style.Theme_Translucent);

        final View view = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_sort, null);

        this.POPUP_CONTAINER = view.findViewById(R.id.popupContainer);
        this.POPUP_VIEW = view.findViewById(R.id.popup);

        RADIO_ASC = view.findViewById(R.id.radioIconAscending);
        RADIO_DESC = view.findViewById(R.id.radioIconDescending);
        RADIO_NAME = view.findViewById(R.id.radioIconName);
        RADIO_DATE_CAPTURING = view.findViewById(R.id.radioIconTime);
        RADIO_SIZE = view.findViewById(R.id.radioIconSize);
        RADIO_DURATION = view.findViewById(R.id.radioIconDuration);

        view.findViewById(R.id.ascending).setOnClickListener((v) -> order(true));
        view.findViewById(R.id.descending).setOnClickListener((v) -> order(false));
        view.findViewById(R.id.name).setOnClickListener((v) -> sortBy(SORT_NAME));
        view.findViewById(R.id.time).setOnClickListener((v) -> sortBy(SORT_DATE_CAPTURING));
        view.findViewById(R.id.size).setOnClickListener((v) -> sortBy(SORT_SIZE));
        view.findViewById(R.id.duration).setOnClickListener((v) -> sortBy(SORT_DURATION));

        view.findViewById(R.id.popupBtnYes).setOnClickListener((v) -> {
            this.dismissWithAnimation();

            l.onSorted(SORT_BY, ASC);
        });

        view.findViewById(R.id.popupBtnNo).setOnClickListener((v) -> this.dismissWithAnimation());

        POPUP_CONTAINER.setOnClickListener((v) -> this.dismissWithAnimation());

        Utils.Popup.setInAnimation(this, POPUP_CONTAINER, POPUP_VIEW);

        this.setContentView(view);

        Objects.requireNonNull(getWindow()).setStatusBarColor(ctx.getColor(R.color.popup_background_transparent));

        order(asc);
        sortBy(sortBy);
    }

    private void order(boolean asc) {
        ASC = asc;

        RADIO_ASC.setImageResource(asc ? R.drawable.radio_on : R.drawable.radio_off);
        RADIO_DESC.setImageResource(asc ? R.drawable.radio_off : R.drawable.radio_on);
    }

    private void sortBy(int sort) {
        SORT_BY = sort;

        toggleRadio(RADIO_NAME, SORT_NAME);
        toggleRadio(RADIO_DATE_CAPTURING, SORT_DATE_CAPTURING);
        toggleRadio(RADIO_SIZE, SORT_SIZE);
        toggleRadio(RADIO_DURATION, SORT_DURATION);
    }

    private void toggleRadio(ImageView i, int s) {
        i.setImageResource(SORT_BY == s ? R.drawable.radio_on : R.drawable.radio_off);
    }

    public void dismissWithAnimation() {
        Utils.Popup.callOutAnimation(this, POPUP_CONTAINER, POPUP_VIEW);
    }
}
