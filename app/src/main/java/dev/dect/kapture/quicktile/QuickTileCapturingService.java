package dev.dect.kapture.quicktile;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.graphics.drawable.Icon;

import dev.dect.kapture.R;

import dev.dect.kapture.data.Constants;
import dev.dect.kapture.service.CapturingService;

public class QuickTileCapturingService extends TileService {
    private UpdateReceiver UPDATE_RECEIVER;

    public static void requestUiUpdate(Context ctx) {
        TileService.requestListeningState(ctx, new ComponentName(ctx, QuickTileCapturingService.class));

        ctx.sendBroadcast(new Intent(Constants.QuickTile.Action.UPDATE));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        UPDATE_RECEIVER = new UpdateReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(UPDATE_RECEIVER != null) {
            try {
                unregisterReceiver(UPDATE_RECEIVER);
            } catch (Exception ignore) {}

            UPDATE_RECEIVER = null;
        }
    }

    @Override
    public void onStartListening() {
        if(UPDATE_RECEIVER != null) {
            registerReceiver(UPDATE_RECEIVER, new IntentFilter(Constants.QuickTile.Action.UPDATE), RECEIVER_NOT_EXPORTED);
        }

        super.onStartListening();

        updateTile();
    }

    @Override
    public void onStopListening() {
        if(UPDATE_RECEIVER != null) {
            unregisterReceiver(UPDATE_RECEIVER);

            UPDATE_RECEIVER = null;
        }

        updateTile();

        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        CapturingService.requestToggleRecording(this);
    }

    private void updateTile() {
        if(CapturingService.isProcessing()) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().setLabel(getString(R.string.quick_settings_tile_processing));
        } else if(CapturingService.isInCountdown()) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().setLabel(getString(R.string.quick_settings_tile_stop_recording));
        } else if(CapturingService.isRecording()) {
            getQsTile().setState(Tile.STATE_ACTIVE);
            getQsTile().setLabel(getString(R.string.quick_settings_tile_stop_recording));
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.icon_quick_settings_recording));
        } else {
            getQsTile().setState(Tile.STATE_INACTIVE);
            getQsTile().setLabel(getString(R.string.quick_settings_tile_start_recording));
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.icon_quick_settings_not_recording));
        }

        getQsTile().updateTile();
    }

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent i) {
            updateTile();
        }
    }
}
