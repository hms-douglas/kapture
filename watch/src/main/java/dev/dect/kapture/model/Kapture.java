package dev.dect.kapture.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.utils.KFile;

/** @noinspection ResultOfMethodCallIgnored*/
public class Kapture {
    public static final String FROM_WATCH = "w",
                               FROM_PHONE = "p";

    private final Context CONTEXT;

    private long ID;

    private String LOCATION;

    private File FILE;

    private long DURATION = -1;

    private Bitmap THUMBNAIL = null;

    private int[] VIDEO_SIZE;

    private boolean HAS_MEDIA_DATA = false;

    private String PROFILE_ID,
                   FROM;

    public Kapture(Context ctx) {
        this(ctx, -1, "", null, FROM_WATCH);
    }

    public Kapture(Context ctx, long id, String location, String profileId, String from) {
        this.CONTEXT = ctx;
        this.ID = id;
        this.LOCATION = location;
        this.PROFILE_ID = profileId;
        this.FROM = from;

        this.FILE = new File(location);
    }

    public String getName() {
        return FILE.getName();
    }

    public long getCreationTime() {
        return FILE.lastModified();
    }

    public long getSize() {
        return FILE.length();
    }

    public long getDuration() {
        if(DURATION == -1) {
            retrieveAllMediaData();
        }

        return DURATION;
    }

    public Bitmap getThumbnail() {
        if(THUMBNAIL == null) {
            retrieveAllMediaData();
        }

        return THUMBNAIL;
    }

    public int[] getVideoSize() {
        if(VIDEO_SIZE == null) {
            retrieveAllMediaData();
        }

        return VIDEO_SIZE;
    }

    public long getId() {
        return ID;
    }

    public String getLocation() {
        return LOCATION;
    }

    public File getFile() {
        return FILE;
    }

    public String getProfileId() {
        return PROFILE_ID;
    }

    public String getFrom() {
        return FROM;
    }

    public boolean isFromWatch() {
        return isFromWatch(this);
    }

    public void setFrom(String from) {
        this.FROM = from;
    }

    public void setId(long id) {
        this.ID = id;
    }

    public void setLocation(String l) {
        this.FILE = new File(l);

        this.LOCATION = l;
    }

    public void setFile(File f) {
        this.FILE = f;

        this.LOCATION = f.getAbsolutePath();
    }

    public void setProfileId(String profileId) {
        this.PROFILE_ID = profileId;
    }

    public void retrieveAllMediaData() {
        if(HAS_MEDIA_DATA) {
            return;
        }

        try {
            final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

            mediaMetadataRetriever.setDataSource(LOCATION);

            DURATION = Long.parseLong(Objects.requireNonNull(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

            VIDEO_SIZE = new int[] {
                Integer.parseInt(Objects.requireNonNull(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))),
                Integer.parseInt(Objects.requireNonNull(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)))
            };

            if(ID == -1) {
                THUMBNAIL = generateThumbnail();
            } else {
                final File cachedThumbnail = getThumbnailCachedFile();

                if(cachedThumbnail.exists()) {
                    THUMBNAIL = BitmapFactory.decodeFile(cachedThumbnail.getPath());
                } else {
                    THUMBNAIL = generateThumbnail();

                    cachedThumbnail.createNewFile();

                    final FileOutputStream fos = new FileOutputStream(cachedThumbnail);

                    THUMBNAIL.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    fos.flush();
                    fos.close();
                }
            }

            mediaMetadataRetriever.close();

            HAS_MEDIA_DATA = true;
        } catch (Exception ignore) {}
    }

    public void retrieveAllMediaData(Runnable runnable) {
        final HandlerThread handlerThread = new HandlerThread("RMD");

        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());

        handler.post(() -> {
            retrieveAllMediaData();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                runnable.run();

                handlerThread.quit();
            }, 0);
        });
    }

    private Bitmap generateThumbnail() throws IOException {
        return ThumbnailUtils.createVideoThumbnail(
            new File(LOCATION),
            new Size(Math.min(KSettings.VIDEO_RESOLUTIONS[1], VIDEO_SIZE[0]), Math.min(KSettings.VIDEO_RESOLUTIONS[1], VIDEO_SIZE[1])),
            new CancellationSignal()
        );
    }

    public File getThumbnailCachedFile() {
        return new File(CONTEXT.getCacheDir(), "thumbnail_" + ID + ".jpeg");
    }

    public void notifyAllMediaScanner() {
        KFile.notifyMediaScanner(CONTEXT, FILE);
    }

    public static boolean isFromWatch(Kapture kapture) {
        return kapture.getFrom().equals(FROM_WATCH);
    }
}
