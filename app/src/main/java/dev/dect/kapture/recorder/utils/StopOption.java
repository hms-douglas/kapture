package dev.dect.kapture.recorder.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.CountDownTimer;

import dev.dect.kapture.data.KSettings;

/** @noinspection unused*/
public class StopOption {
    public interface OnStopOption {
        void onStopTriggered();
    }

    private final TimeLimit TIME_LIMIT;

    private final ScreenOff SCREEN_OFF;

    private final Shake SHAKE;

    private final BatteryLevel BATTERY_LEVEL;

    public StopOption(Context ctx, KSettings ks, OnStopOption onStopOption) {
        this.TIME_LIMIT = new TimeLimit(ks, onStopOption);

        this.SCREEN_OFF = new ScreenOff(ctx, ks, onStopOption);

        this.SHAKE = new Shake(ctx, ks, onStopOption);

        this.BATTERY_LEVEL = new BatteryLevel(ctx, ks, onStopOption);
    }

    public void start() {
        TIME_LIMIT.start();

        SCREEN_OFF.start();

        SHAKE.start();

        BATTERY_LEVEL.start();
    }

    public void destroy() {
        TIME_LIMIT.destroy();

        SCREEN_OFF.destroy();

        SHAKE.destroy();

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

    public static class ScreenOff extends BroadcastReceiver {
        private final KSettings KSETTINGS;

        private final OnStopOption LISTENER;

        private final Context CONTEXT;

        public ScreenOff() { //DON'T USE
            this.CONTEXT = null;
            this.KSETTINGS = null;
            this.LISTENER = null;
        }

        public ScreenOff(Context ctx, KSettings ks, OnStopOption onStopOption) {
            this.CONTEXT = ctx;
            this.KSETTINGS = ks;

            this.LISTENER = onStopOption;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                LISTENER.onStopTriggered();
            }
        }

        public void start() {
            if(KSETTINGS.isToStopOnScreenOff()) {
                CONTEXT.registerReceiver(this, new IntentFilter(Intent.ACTION_SCREEN_OFF));
            }
        }

        public void destroy() {
            if(KSETTINGS.isToStopOnScreenOff()) {
                CONTEXT.unregisterReceiver(this);
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

    private static class Shake {
        private final KSettings KSETTINGS;

        private final OnStopOption LISTENER;

        private final SensorManager SENSOR_MANAGER;

        private final SensorEventListener2 SENSOR_LISTENER;

        private boolean HAS_TRIGGERED_ONCE = false;

        public Shake(Context ctx, KSettings ks, OnStopOption onStopOption) {
            this.KSETTINGS = ks;

            this.LISTENER = onStopOption;

            this.SENSOR_MANAGER = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);

            this.SENSOR_LISTENER = new SensorEventListener2() {
                @Override
                public void onFlushCompleted(Sensor sensor) {}

                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    if(sensorEvent != null) {
                        final float x = sensorEvent.values[0],
                                    y = sensorEvent.values[1],
                                    z = sensorEvent.values[2];

                        final double acceleration = Math.sqrt(x * x + y * y + z * z);

                        if(acceleration > 20 && !HAS_TRIGGERED_ONCE) {
                            HAS_TRIGGERED_ONCE = true;

                            LISTENER.onStopTriggered();
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {}
            };
        }

        public void start() {
            if(KSETTINGS.isToStopOnShake() && SENSOR_MANAGER != null) {
                SENSOR_MANAGER.registerListener(
                    SENSOR_LISTENER,
                    SENSOR_MANAGER.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL
                );
            }
        }

        public void destroy() {
            if(KSETTINGS.isToStopOnShake() && SENSOR_MANAGER != null) {
                SENSOR_MANAGER.unregisterListener(SENSOR_LISTENER);
            }
        }
    }
}
