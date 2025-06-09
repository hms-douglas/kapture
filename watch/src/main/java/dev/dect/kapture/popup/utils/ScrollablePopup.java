package dev.dect.kapture.popup.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import com.google.android.wearable.input.RotaryEncoderHelper;

import dev.dect.kapture.data.Constants;

public class ScrollablePopup extends Dialog {
    private NestedScrollView NESTED_SCROLLER_EL;

    @Override
    public boolean onGenericMotionEvent(@NonNull MotionEvent event) {
        final int scrollTo = (int) (NESTED_SCROLLER_EL.getY() + (RotaryEncoderHelper.getRotaryAxisValue(event) < 0 ? Constants.BEZEL_SCROLL_BY : (Constants.BEZEL_SCROLL_BY * -1)));

        NESTED_SCROLLER_EL.smoothScrollBy(0, scrollTo);

        return true;
    }

    public ScrollablePopup(@NonNull Context context) {
        super(context);
    }

    public ScrollablePopup(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setNestedScroller(NestedScrollView nestedScrollView) {
        this.NESTED_SCROLLER_EL = nestedScrollView;
    }
}
