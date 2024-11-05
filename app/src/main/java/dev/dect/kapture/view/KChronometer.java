package dev.dect.kapture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Chronometer;

public class KChronometer extends Chronometer {
    private long TIME_HELPER = 0;

    public KChronometer(Context context) {
        super(context);
    }

    public KChronometer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KChronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void resume() {
        setBase(System.currentTimeMillis() - TIME_HELPER);
        start();
    }

    public void pause() {
        stop();
        TIME_HELPER = System.currentTimeMillis() - getBase();
    }
}
