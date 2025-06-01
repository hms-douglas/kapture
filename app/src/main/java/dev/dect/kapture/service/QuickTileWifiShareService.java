package dev.dect.kapture.service;

import android.content.Intent;
import android.service.quicksettings.TileService;

import dev.dect.kapture.activity.ActionActivity;
import dev.dect.kapture.data.Constants;

public class QuickTileWifiShareService extends TileService {
    @Override
    public void onClick() {
        super.onClick();

        final Intent intentWifiShare = new Intent(this, ActionActivity.class);

        intentWifiShare.putExtra(ActionActivity.INTENT_ACTION, Constants.QuickTile.Action.WIFI_SHARE);
        intentWifiShare.putExtra(ActionActivity.INTENT_FROM, ActionActivity.FROM_QUICK_TILE);

        intentWifiShare.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        startActivity(intentWifiShare);
    }
 }
