package dev.dect.kapture.recorder.utils;

import android.content.Context;
import android.media.AudioManager;

import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.utils.Utils;

/** @noinspection unused*/
public class BeforeStartOption {
    private final Context CONTEXT;

    private final KSettings KSETTINGS;

    private final OpenUrl OPEN_URL;

    private final MediaVolume MEDIA_VOLUME;

    private final LaunchApp LAUNCH_APP;

    private int PREVIOUS_MEDIA_VOLUME;

    public BeforeStartOption(Context ctx, KSettings ks) {
        this.CONTEXT = ctx;
        this.KSETTINGS = ks;

        this.OPEN_URL = new OpenUrl(ctx, ks);
        this.MEDIA_VOLUME = new MediaVolume(ctx, ks);
        this.LAUNCH_APP = new LaunchApp(ctx, ks);
    }

    public void start() {
        OPEN_URL.start();

        MEDIA_VOLUME.start();

        LAUNCH_APP.start();
    }

    public void destroy() {
        OPEN_URL.destroy();

        MEDIA_VOLUME.destroy();

        LAUNCH_APP.destroy();
    }

    private static class OpenUrl {
        private final Context CONTEXT;

        private final KSettings KSETTINGS;

        public OpenUrl(Context ctx, KSettings ks) {
            this.CONTEXT = ctx;
            this.KSETTINGS = ks;
        }

        public void start() {
            if(KSETTINGS.isToOpenUrlBeforeStart() && !KSETTINGS.isToSetLaunchAppBeforeStart()) {
                Utils.ExternalActivity.requestOpenUrlOnBrowser(CONTEXT, KSETTINGS.getBeforeStartUrl());
            }
        }

        public void destroy() {}
    }

    private static class MediaVolume {
        private final Context CONTEXT;

        private final KSettings KSETTINGS;

        private int PREVIOUS_MEDIA_VOLUME;

        public MediaVolume(Context ctx, KSettings ks) {
            this.CONTEXT = ctx;
            this.KSETTINGS = ks;
        }

        public void start() {
            if(KSETTINGS.isToSetMediaVolumeBeforeStart()) {
                final AudioManager audioManager = (AudioManager) CONTEXT.getSystemService(Context.AUDIO_SERVICE);

                PREVIOUS_MEDIA_VOLUME = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    (KSETTINGS.getBeforeStartMediaVolumePercentage() * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100,
                    0
                );

            }
        }

        public void destroy() {
            if(KSETTINGS.isToSetMediaVolumeBeforeStart()) {
                ((AudioManager) CONTEXT.getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(AudioManager.STREAM_MUSIC, PREVIOUS_MEDIA_VOLUME,0);
            }
        }
    }

    private static class LaunchApp {
        private final Context CONTEXT;

        private final KSettings KSETTINGS;

        public LaunchApp(Context ctx, KSettings ks) {
            this.CONTEXT = ctx;
            this.KSETTINGS = ks;
        }

        public void start() {
            if(KSETTINGS.isToSetLaunchAppBeforeStart()) {
                Utils.ExternalActivity.requestApp(CONTEXT, KSETTINGS.getBeforeStartLaunchAppPackage(), false);
            }
        }

        public void destroy() {}
    }
}
