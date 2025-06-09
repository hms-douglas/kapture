package dev.dect.kapture.utils;

import java.util.Timer;
import java.util.TimerTask;

import dev.dect.kapture.activity.MainActivity;

public class KTimer {
    private static int TIME;

    private final KTimerTask KTASK;

    private final Timer TIMER;

    private boolean PAUSED;

    public KTimer() {
        this.KTASK = new KTimerTask();

        this.TIMER = new Timer();
    }

    public void start() {
        TIME = 0;

        PAUSED = false;

        TIMER.schedule(KTASK, 0, 1000);
    }

    public void stop() {
        TIMER.cancel();
        TIMER.purge();
    }

    public void pause() {
        PAUSED = true;
    }

    public void resume() {
        PAUSED = false;
    }

    public static int getTime() {
        return TIME;
    }

    private class KTimerTask extends TimerTask {
        @Override
        public void run() {
            if(PAUSED) {
                return;
            }

            TIME++;

            if(MainActivity.getInstance() != null) {
                MainActivity.getInstance().updateTime(TIME);
            }
        }
    }
}