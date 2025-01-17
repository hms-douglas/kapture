package dev.dect.kapture.service;

import android.content.Intent;
import android.widget.RemoteViewsService;

import dev.dect.kapture.adapter.ProfileWidgetAdapter;

public class ProfileWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ProfileWidgetAdapter(this.getApplicationContext());
    }
}

