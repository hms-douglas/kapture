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
import java.util.ArrayList;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KFile;

/** @noinspection ResultOfMethodCallIgnored*/
public class Kapture {
    private final Context CONTEXT;

    private long ID;

    private String LOCATION;

    private ArrayList<Extra> EXTRAS;

    private ArrayList<Screenshot> SCREENSHOTS;

    private File FILE;

    private long DURATION = -1;

    private Bitmap THUMBNAIL = null;

    private int[] VIDEO_SIZE;

    private boolean HAS_MEDIA_DATA = false;

    private String PROFILE_ID;

    public Kapture(Context ctx) {
        this(ctx, -1, "", null, null, null);
    }

    public Kapture(Context ctx, File file) {
        this(ctx, -1, file.getAbsolutePath(), null, null, null);
    }

    public Kapture(Context ctx, long id, String location, ArrayList<Extra> extras, ArrayList<Screenshot> screenshots, String profileId) {
        this.CONTEXT = ctx;
        this.ID = id;
        this.LOCATION = location;
        this.EXTRAS = extras == null ? new ArrayList<>() : extras;
        this.SCREENSHOTS = screenshots == null ? new ArrayList<>() : screenshots;
        this.PROFILE_ID = profileId;

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

    public ArrayList<Extra> getExtras() {
        return EXTRAS;
    }

    public ArrayList<Screenshot> getScreenshots() {
        return SCREENSHOTS;
    }

    public File getFile() {
        return FILE;
    }

    public String getProfileId() {
        return PROFILE_ID;
    }

    public boolean hasExtras() {
        return !EXTRAS.isEmpty();
    }

    public boolean hasScreenshots() {
        return !SCREENSHOTS.isEmpty();
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

    public void setExtras(ArrayList<Extra> extras) {
        this.EXTRAS = extras == null ? new ArrayList<>() : extras;
    }

    public void setScreenshots(ArrayList<Screenshot> screenshots) {
        this.SCREENSHOTS = screenshots == null ? new ArrayList<>() : screenshots;
    }

    public void setProfileId(String profileId) {
        this.PROFILE_ID = profileId;
    }

    public void addExtra(Extra extra) {
        if(extra.getIdKapture() == -1) {
            extra.setIdKapture(this.ID);
        }

        this.EXTRAS.add(extra);
    }

    public void addScreenshot(Screenshot screenshot) {
        if(screenshot.getIdKapture() == -1) {
            screenshot.setIdKapture(this.ID);
        }

        this.SCREENSHOTS.add(screenshot);
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
            new Size(Math.min(1080, VIDEO_SIZE[0]), Math.min(1080, VIDEO_SIZE[1])),
            new CancellationSignal()
        );
    }

    public File getThumbnailCachedFile() {
        return new File(CONTEXT.getCacheDir(), "thumbnail_" + ID + ".jpeg");
    }

    public void notifyAllMediaScanner() {
        KFile.notifyMediaScanner(CONTEXT, FILE);

        for(Extra extra : EXTRAS) {
            KFile.notifyMediaScanner(CONTEXT, extra.getLocation());
        }
    }

    public static class Extra {
        public static final int EXTRA_AUDIO_AUDIO = 0,
                                EXTRA_AUDIO_INTERNAL_ONLY = 1,
                                EXTRA_AUDIO_MIC_ONLY = 2,
                                EXTRA_VIDEO_NO_AUDIO = 3,
                                EXTRA_VIDEO_MIC_ONLY = 4,
                                EXTRA_VIDEO_INTERNAL_ONLY = 5;

        private long ID,
                     ID_KAPTURE;

        private int TYPE;

        private String LOCATION;

        public Extra() {
            this(-1, "");
        }

        public Extra(int type, File file) {
            this(type, file.getAbsolutePath());
        }

        public Extra(int type, String location) {
            this(-1, type, location, -1);
        }

        public Extra(long id, int type, String location, long idKapture) {
            this.ID = id;
            this.TYPE = type;
            this.LOCATION = location;
            this.ID_KAPTURE = idKapture;
        }

        public long getId() {
            return ID;
        }

        public long getIdKapture() {
            return ID_KAPTURE;
        }

        public String getLocation() {
            return LOCATION;
        }

        public int getType() {
            return TYPE;
        }

        public void setId(long id) {
            this.ID = id;
        }

        public void setIdKapture(long idKapture) {
            this.ID_KAPTURE = idKapture;
        }

        public void setLocation(String l) {
            this.LOCATION = l;
        }

        public void setType(int type) {
            this.TYPE = type;
        }

        public String getTypeName(Context ctx) {
            String name = "";

            switch(TYPE) {
                case Kapture.Extra.EXTRA_AUDIO_AUDIO:
                    name = ctx.getString(R.string.popup_extra_audio_audio);
                    break;

                case Kapture.Extra.EXTRA_AUDIO_INTERNAL_ONLY:
                    name = ctx.getString(R.string.popup_extra_audio_internal);
                    break;

                case Kapture.Extra.EXTRA_AUDIO_MIC_ONLY:
                    name = ctx.getString(R.string.popup_extra_audio_mic);
                    break;

                case Kapture.Extra.EXTRA_VIDEO_NO_AUDIO:
                    name = ctx.getString(R.string.popup_extra_video_no_audio);
                    break;

                case Kapture.Extra.EXTRA_VIDEO_INTERNAL_ONLY:
                    name = ctx.getString(R.string.popup_extra_video_only_internal);
                    break;

                case Kapture.Extra.EXTRA_VIDEO_MIC_ONLY:
                    name = ctx.getString(R.string.popup_extra_video_only_mic);
                    break;
            }

            return name;
        }

        public String getFileNameComplement(Context ctx) {
            return getFileNameComplementByType(ctx, TYPE);
        }

        public static String getFileNameComplementByType(Context ctx, int type) {
            switch(type) {
                case EXTRA_AUDIO_AUDIO:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_audio_audio);

                case EXTRA_AUDIO_INTERNAL_ONLY:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_audio_internal);

                case EXTRA_AUDIO_MIC_ONLY:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_audio_mic);

                case EXTRA_VIDEO_NO_AUDIO:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_video_no_audio);

                case EXTRA_VIDEO_MIC_ONLY:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_video_only_mic);

                case EXTRA_VIDEO_INTERNAL_ONLY:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_video_only_internal);

                default:
                    return null;
            }
        }
    }

    public static class Screenshot {
        private long ID,
                     ID_KAPTURE;

        private String LOCATION;

        public Screenshot() {
            this("");
        }

        public Screenshot(File file) {
            this(file.getAbsolutePath());
        }

        public Screenshot(String location) {
            this(-1, location, -1);
        }

        public Screenshot(long id, String location, long idKapture) {
            this.ID = id;
            this.LOCATION = location;
            this.ID_KAPTURE = idKapture;
        }

        public long getId() {
            return ID;
        }

        public long getIdKapture() {
            return ID_KAPTURE;
        }

        public String getLocation() {
            return LOCATION;
        }

        public void setId(long id) {
            this.ID = id;
        }

        public void setIdKapture(long idKapture) {
            this.ID_KAPTURE = idKapture;
        }

        public void setLocation(String l) {
            this.LOCATION = l;
        }
    }
}
