package dev.dect.kapture.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import dev.dect.kapture.R;

@SuppressLint("ClickableViewAccessibility")
public class KTimePicker extends NumberPicker {
    public KTimePicker(Context context) {
        super(context);

        setInitialStyle();

        setMinMax();
    }

    public KTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        setInitialStyle();

        setMinMax();
    }

    public KTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInitialStyle();

        setMinMax();
    }

    @Override
    public void addView(View child) {
        super.addView(child);

        setBold(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);

        setBold(child);
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);

        setBold(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);

        setBold(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        setBold(child);
    }

    private void setMinMax() {
        this.setMinValue(0);
        this.setMaxValue(59);
    }

    private void setInitialStyle() {
        this.setTextColor(this.getContext().getColor(R.color.popup_time_picker_font_not_focused));
        this.setTextSize(this.getContext().getResources().getDimension(R.dimen.popup_time_picker_font));
    }

    private void setBold(View view) {
        if(view instanceof EditText) {
            ((EditText) view).setTypeface(null, Typeface.BOLD);
        }
    }
}
