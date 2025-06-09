package dev.dect.kapture.communication;

import android.content.Context;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import dev.dect.kapture.data.Constants;

public class Sender {
    private Context CONTEXT;

    public Sender(Context ctx) {
        this.CONTEXT = ctx;
    }

    public void informReceivingSuccess() {
        inform(Constants.DataKey.Action.INFORM_RECEIVED_SUCCESS);
    }

    public void informReceivingError() {
        inform(Constants.DataKey.Action.INFORM_RECEIVED_ERROR);
    }

    private void inform(String action) {
        PutDataMapRequest dataRequest = PutDataMapRequest.create(Constants.DataKey.DATA_PATH);

        dataRequest.getDataMap().putString(Constants.DataKey.ACTION, action);

        send(dataRequest);
    }

    private void send(PutDataMapRequest dataRequest) {
        dataRequest.getDataMap().putLong(Constants.DataKey.TIMESTAMP, System.currentTimeMillis());

        dataRequest.setUrgent();

        DataLayerListener.sending();

        Wearable.getDataClient(CONTEXT).putDataItem(dataRequest.asPutDataRequest());
    }
}
