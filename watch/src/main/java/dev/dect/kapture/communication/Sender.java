package dev.dect.kapture.communication;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.util.ArrayList;

import dev.dect.kapture.data.Constants;
import dev.dect.kapture.model.Kapture;

public class Sender {
    private Context CONTEXT;

    public Sender(Context ctx) {
        this.CONTEXT = ctx;
    }

    public void sendKapturesFiles(ArrayList<Kapture> kaptures) {
        inform(Constants.DataKey.Action.INFORM_IS_SENDING_FILEs);

        final PutDataMapRequest dataRequest = PutDataMapRequest.create(Constants.DataKey.DATA_PATH);

        dataRequest.getDataMap().putString(Constants.DataKey.ACTION, Constants.DataKey.Action.FILE);
        dataRequest.getDataMap().putInt(Constants.DataKey.FILE_AMOUNT, kaptures.size());

        for(int i = 0; i < kaptures.size(); i++) {
            final File file = kaptures.get(i).getFile();

            dataRequest.getDataMap().putString(Constants.DataKey.FILE_NAME + i, file.getName());
            dataRequest.getDataMap().putAsset(Constants.DataKey.FILE_ASSET + i, Asset.createFromUri(Uri.fromFile(file)));
        }

        send(dataRequest);
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
