package dev.dect.kapture.recorder.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;

import dev.dect.kapture.data.KSettings;

/** @noinspection unused*/
public class StopOption {
    public interface OnStopOption {
        void onStopTriggered();
    }

    private final TimeLimit TIME_LIMIT;

    private final BatteryLevel BATTERY_LEVEL;

    public StopOption(Context ctx, KSettings ks, OnStopOption onStopOption) {
        this.TIME_LIMIT = new TimeLimit(ks, onStopOption);

        this.BATTERY_LEVEL = new BatteryLevel(ctx, ks, onStopOption);
    }

    public void start() {
        TIME_LIMIT.start();

        BATTERY_LEVEL.start();
    }

    public void destroy() {
        TIME_LIMIT.destroy();

        BATTERY_LEVEL.destroy();
    }

    private static class TimeLimit {
        private final KSettings KSETTINGS;

        private final OnStopOption LISTENER;

        private CountDownTimer COUNTDOWN;

        public TimeLimit(KSettings ks, OnStopOption onStopOption) {
            this.KSETTINGS = ks;

            this.LISTENER = onStopOption;
        }

        public void start() {
            if(KSETTINGS.isToUseTimeLimit()) {
                COUNTDOWN = new CountDownTimer(KSETTINGS.getMilliSecondsTimeLimit(), 1000) {
                    @Override
                    public void onTick(long l) {}

                    @Override
                    public void onFinish() {
                        LISTENER.onStopTriggered();
                    }
                }.start();
            }
        }

        public void destroy() {
            if(COUNTDOWN != null) {
                try {
                    COUNTDOWN.cancel();

                    COUNTDOWN = null;
                } catch (Exception ignore) {}
            }
        }
    }

    public static class BatteryLevel extends BroadcastReceiver {
        private final KSettings KSETTINGS;

        private final OnStopOption LISTENER;

        private final Context CONTEXT;

        public BatteryLevel() { //DON'T USE
            this.CONTEXT = null;
            this.KSETTINGS = null;
            this.LISTENER = null;
        }

        public BatteryLevel(Context ctx, KSettings ks, OnStopOption onStopOption) {
            this.CONTEXT = ctx;
            this.KSETTINGS = ks;

            this.LISTENER = onStopOption;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getIntExtra("level", 0) <= KSETTINGS.getStopOnBatteryLevelLevel()) {
                LISTENER.onStopTriggered();
            }
        }

        public void start() {
            if(KSETTINGS.isToStopOnBatteryLevel()) {
                CONTEXT.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
        }

        public void destroy() {
            if(KSETTINGS.isToStopOnBatteryLevel()) {
                CONTEXT.unregisterReceiver(this);
            }
        }
    }
}
