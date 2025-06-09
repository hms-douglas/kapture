package dev.dect.kapture.communication;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import dev.dect.kapture.activity.MainActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.notification.ReceivingNotification;
import dev.dect.kapture.utils.KFile;

public class DataLayerListener extends WearableListenerService {
    private static boolean IS_THE_ONE_SENDING = false;

    public static void sending() {
        IS_THE_ONE_SENDING = true;
    }

    private ReceivingNotification RECEIVING_NOTIFICATION;

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
                        case Constants.DataKey.Action.INFORM_IS_SENDING_FILEs:
                            RECEIVING_NOTIFICATION = new ReceivingNotification(this);

                            RECEIVING_NOTIFICATION.createAndShow();
                            break;

                        case Constants.DataKey.Action.FILE:
                            receiveFiles(dataMap);
                            break;
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    private void receiveFiles(DataMap dataMap) {
        if(RECEIVING_NOTIFICATION == null) {
            RECEIVING_NOTIFICATION = new ReceivingNotification(this);

            RECEIVING_NOTIFICATION.createAndShow();
        }

        try {
            final DB db = new DB(this);

            final int amount = dataMap.getInt(Constants.DataKey.FILE_AMOUNT, 0);

            for(int i = 0; i < amount; i++) {
                receiveFile(db, dataMap, i);
            }

            RECEIVING_NOTIFICATION.notifyReceived(amount);

            new Sender(this).informReceivingSuccess();
        } catch (Exception ignore) {
            RECEIVING_NOTIFICATION.notifyError();

            new Sender(this).informReceivingError();
        }

        RECEIVING_NOTIFICATION = null;
    }

    private void receiveFile(DB db, DataMap dataMap, int index) throws Exception {
        final Asset asset = dataMap.getAsset(Constants.DataKey.FILE_ASSET + index);

        final InputStream inputStream = Tasks.await(Wearable.getDataClient(this).getFdForAsset(asset)).getInputStream();

        final File file = KFile.renameIfNecessary(
            new File(
                KFile.getSavingLocation(this),
                dataMap.getString(Constants.DataKey.FILE_NAME + index, System.currentTimeMillis() + "." + Constants.EXT_VIDEO_FORMAT)
            )
        );

        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        final Kapture kapture = new Kapture(this);

        kapture.setFrom(Kapture.FROM_WATCH);
        kapture.setFile(file);
        kapture.setProfileId(Constants.NO_PROFILE);

        db.insertKapture(kapture);

        if(MainActivity.getInstance() != null) {
            MainActivity.getInstance().requestUiUpdate(kapture);
        }
    }
}
