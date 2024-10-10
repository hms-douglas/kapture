package dev.dect.kapture.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import dev.dect.kapture.R;

public class OutlineTextView extends androidx.appcompat.widget.AppCompatTextView {

    public OutlineTextView(Context context) {
        super(context);
    }

    public OutlineTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OutlineTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final TextPaint paint = this.getPaint();

        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(super.getContext().getResources().getDimension(R.dimen.countdown_border));

        setTextColor(super.getContext().getColor(R.color.countdown_border));

        super.onDraw(canvas);

        setTextColor(super.getContext().getColor(R.color.countdown_text));

        paint.setStyle(Paint.Style.FILL);
    }
}