package dev.dect.kapture.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.utils.KFile;

public class Kapture {
    private long ID;

    private String LOCATION;

    private ArrayList<Extra> EXTRAS;

    private File FILE;

    private long DURATION = -1;

    private Bitmap THUMBNAIL = null;

    public Kapture() {
        this(-1, "", null);
    }

    public Kapture(File file) {
        this(-1, file.getAbsolutePath(), null);
    }

    public Kapture(long id, String location, ArrayList<Extra> extras) {
        this.ID = id;
        this.LOCATION = location;
        this.EXTRAS = extras == null ? new ArrayList<>() : extras;

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
            try {
                final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

                mediaMetadataRetriever.setDataSource(LOCATION);

                DURATION = Long.parseLong(Objects.requireNonNull(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

                mediaMetadataRetriever.close();
            } catch (Exception ignore) {}
        }

        return DURATION;
    }

    public Bitmap getThumbnail() {
        if(THUMBNAIL == null) {
            try {
                final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

                mediaMetadataRetriever.setDataSource(LOCATION);

                THUMBNAIL = mediaMetadataRetriever.getFrameAtTime();

                mediaMetadataRetriever.close();
            } catch (Exception ignore) {}
        }

        return THUMBNAIL;
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

    public boolean hasExtras() {
        return !EXTRAS.isEmpty();
    }

    public void setId(long id) {
        this.ID = id;
    }

    public void setLocation(String l) {
        this.FILE = new File(l);

        this.LOCATION = l;
    }

    public void setExtras(ArrayList<Extra> extras) {
        this.EXTRAS = extras == null ? new ArrayList<>() : extras;
    }

    public void addExtra(Extra extra) {
        if(extra.getIdKapture() == -1) {
            extra.setIdKapture(this.ID);
        }

        this.EXTRAS.add(extra);
    }

    public static class Extra {
        public static final int EXTRA_MP3_AUDIO = 0,
                                EXTRA_MP3_INTERNAL_ONLY = 1,
                                EXTRA_MP3_MIC_ONLY = 2,
                                EXTRA_MP4_NO_AUDIO = 3,
                                EXTRA_MP4_MIC_ONLY = 4,
                                EXTRA_MP4_INTERNAL_ONLY = 5;

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

        public String getFileNameComplement(Context ctx) {
            return getFileNameComplementByType(ctx, TYPE);
        }

        public static String getFileNameComplementByType(Context ctx, int type) {
            switch(type) {
                case EXTRA_MP3_AUDIO:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_mp3_audio);

                case EXTRA_MP3_INTERNAL_ONLY:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_mp3_internal);

                case EXTRA_MP3_MIC_ONLY:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_mp3_mic);

                case EXTRA_MP4_NO_AUDIO:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_mp4_no_audio);

                case EXTRA_MP4_MIC_ONLY:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_mp4_only_mic);

                case EXTRA_MP4_INTERNAL_ONLY:
                    return KFile.formatStringResource(ctx, R.string.file_name_extra_mp4_only_internal);

                default:
                    return null;
            }
        }
    }
}
