package dev.dect.kapture.service.utils;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.data.KSettings;

@SuppressLint("InflateParams")
public class CountdownHelper {
    private final KSettings KSETTINGS;

    private final Runnable COMPLETED;

    public CountdownHelper(KSettings ks, Runnable completed) {
        this.KSETTINGS = ks;
        this.COMPLETED = completed;
    }

    public void start() {
        if(KSETTINGS.isToDelayStartRecording()) {
            new CountDownTimer(KSETTINGS.getMilliSecondsToStartRecording(), 1000) {
                @Override
                public void onTick(long l) {
                    if(MainActivity.getInstance() != null) {
                        MainActivity.getInstance().updateTimeCountdown((int) (l / 1000) + 1);
                    }
                }

                @Override
                public void onFinish() {
                    COMPLETED.run();
                }
            }.start();
        } else {
            new Handler(Looper.getMainLooper()).post(COMPLETED);
        }
    }
}
