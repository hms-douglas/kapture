package dev.dect.kapture.communication;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.data.Constants;

public class DataLayerListener extends WearableListenerService {
    private static boolean IS_THE_ONE_SENDING = false;

    public static void sending() {
        IS_THE_ONE_SENDING = true;
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        if(IS_THE_ONE_SENDING) {
            IS_THE_ONE_SENDING = false;

            return;
        }

        try {
            for(DataEvent event : dataEventBuffer) {
                if(event.getType() == DataEvent.TYPE_CHANGED) {
                    final DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                    switch(Objects.requireNonNull(dataMap.getString(Constants.DataKey.ACTION))) {
                        case Constants.DataKey.Action.INFORM_RECEIVED_SUCCESS:
                            if(MainActivity.getInstance() != null) {
                                MainActivity.getInstance().updateSendingButton(true);

                                Toast.makeText(this, getString(R.string.toast_success_sending), Toast.LENGTH_SHORT).show();
                            }

                            break;

                        case Constants.DataKey.Action.INFORM_RECEIVED_ERROR:
                            if(MainActivity.getInstance() != null) {
                                MainActivity.getInstance().updateSendingButton(true);

                                Toast.makeText(this, getString(R.string.toast_error_sending_receiving_phone), Toast.LENGTH_SHORT).show();
                            }

                            break;
                    }
                }
            }
        } catch (Exception ignore) {}
    }
}
