package dev.dect.kapture.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.EditedMediaItemSequence;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.viewer.VideoActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.data.KSettings;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.service.CapturingService;

/** @noinspection ResultOfMethodCallIgnored*/
@SuppressLint("WearRecents")
public class KFile {
    public static final String FILE_SEPARATOR = "_";

    private static final String INTERNAL_STORAGE_PATH = "/storage/emulated/0";

    public static File generateNewEmptyKaptureFile(Context ctx, KSettings ks) {
        final int fileId = getFileIdNotGenerated(ctx);

        KSharedPreferences.getAppSp(ctx).edit().putInt(Constants.Sp.App.LAST_FILE_ID, fileId).apply();

        final String fileName =
            formatFileId(fileId)
            + FILE_SEPARATOR
            + getDefaultKaptureFileName(ctx)
            + FILE_SEPARATOR
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));

        return new File(ks.getSavingLocationFile(), fileName + "." + Constants.EXT_VIDEO_FORMAT);
    }

    public static int getFileIdNotGenerated(Context ctx) {
        return KSharedPreferences.getAppSp(ctx).getInt(Constants.Sp.App.LAST_FILE_ID, 0) + 1;
    }

    public static String formatFileId(int id) {
        return String.format(Locale.getDefault(), "%03d", id);
    }

    public static String formatAndroidPathToUser(Context ctx, String path) {
        return path.replaceFirst(INTERNAL_STORAGE_PATH, File.separator + ctx.getString(R.string.internal_storage));
    }

    public static String formatFileSize(long l) {
        try {
            final int i = l == 0 ? 0 : (int) Math.floor(Math.log(l) / Math.log(1024));

            final double result = (l / Math.pow(1024, i));

            final DecimalFormat format = new DecimalFormat("0.0");

            return  format.format(result) + new String[]{"B", "kB", "MB", "GB", "TB"}[i];
        } catch (Exception ignore) {
            return "?";
        }
    }

    public static String formatFileDate(long l) {
        return Utils.Formatter.date(l);
    }

    public static String formatFileDuration(long milli) {
        return Utils.Formatter.timeInMillis(milli);
    }

    public static File getSavingLocation(Context ctx) {
        return new File(KSharedPreferences.getAppSp(ctx).getString(Constants.Sp.App.FILE_SAVING_PATH, KFile.getDefaultFileLocation(ctx).getAbsolutePath()));
    }

    public static File getDefaultFileLocation(Context ctx) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), formatStringResource(ctx, R.string.folder_name));
    }

    public static String formatStringResource(Context ctx, int resId) {
        return ctx.getString(resId).replaceAll(" ", FILE_SEPARATOR);
    }

    public static void openFile(Context ctx, File f) {
        openFile(ctx, f, false);
    }

    public static void openFile(Context ctx, File f, boolean external) {
        final Intent i = new Intent();

        final String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(f));

        if(external) {
            i.setDataAndType(Uri.parse(f.getAbsolutePath()), type);
            i.setAction(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            assert type != null;

            try {
                if(((Activity) ctx).isInMultiWindowMode()) {
                    i.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                }
            } catch (Exception ignore) {}

            if(type.contains("video/")) {
                i.setClass(ctx, VideoActivity.class);
                i.putExtra(VideoActivity.INTENT_URL, f.getAbsolutePath());
            } else {
                openFile(ctx, f, true);

                return;
            }
        }

        try {
            ctx.startActivity(i);
        } catch (Exception ignore) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_viewer_external), Toast.LENGTH_SHORT).show();
        }
    }

    public static String getFileExtension(File f) {
        return f.getName().substring(f.getName().lastIndexOf(".") + 1);
    }

    public static void copyFile(File from, File to) {
        try {
            Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignore) {}
    }

    public static String getDefaultKaptureFileName(Context ctx) {
        return formatStringResource(ctx, R.string.file_name);
    }

    public static void notifyMediaScanner(Context ctx, File f) {
        notifyMediaScanner(ctx, f.getAbsolutePath());
    }

    public static void notifyMediaScanner(Context ctx, String path) {
        try {
            MediaScannerConnection.scanFile(ctx, new String[]{path}, null, null);
        } catch (Exception ignore) {}
    }

    public static void clearCache(Context ctx) {
        if(CapturingService.isRecording()) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_info_while_recording), Toast.LENGTH_SHORT).show();

            return;
        }

        deleteDirectory(ctx.getCacheDir());
        deleteDirectory(ctx.getExternalCacheDir());
    }

    public static long getCacheSize(Context ctx) {
        return getDirectorySize(ctx.getCacheDir()) + getDirectorySize(ctx.getExternalCacheDir());
    }

    public static long getAppTotalFilesSize(Context ctx, boolean includeCache) {
        long size = 0;

        if(includeCache) {
            size += getCacheSize(ctx);
        }

        for(Kapture kapture : new DB(ctx).selectAllKaptures(true)) {
            if(kapture.getFile().exists()) {
                size += kapture.getSize();
            }
        }

        return size;
    }

    private static long getDirectorySize(File f) {
        if(f == null) {
            return 0;
        }

        long size = 0;

        for(File file : Objects.requireNonNull(f.listFiles())) {
            if(file != null) {
                if(file.isDirectory()) {
                    size += getDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }

        return size;
    }

    private static boolean deleteDirectory(File directory) {
        try {
            directory.delete();
        } catch (Exception ignore) {}

        if(directory == null) {
            return false;
        }

        if(!directory.exists()) {
            return true;
        }

        if(!directory.isDirectory()) {
            return false;
        }

        String[] list = directory.list();

        if(list != null) {
            for(String s : list) {
                File entry = new File(directory, s);

                if(entry.isDirectory()) {
                    if(!deleteDirectory(entry)) {
                        return false;
                    }
                } else {
                    if(!entry.delete()) {
                        return false;
                    }
                }
            }
        }

        return directory.delete();
    }

    public static void pcmToWav(File pcm, File wav, KSettings ks) throws IOException {
        copyFile(pcm, wav);

        final int size = ((int) wav.length()) - 44,
                  sizeHelper = size + 36,
                  bitsPerSample = 16,
                  byteRate = ks.getAudioSampleRate() * ks.getInternalAudioChannelNumber() * bitsPerSample / 8;

        final RandomAccessFile randomAccessFile = new RandomAccessFile(wav, "rw");

        randomAccessFile.seek(0);

        final byte[] header = new byte[44];

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (sizeHelper & 0xff);
        header[5] = (byte) ((sizeHelper >> 8) & 0xff);
        header[6] = (byte) ((sizeHelper >> 16) & 0xff);
        header[7] = (byte) ((sizeHelper >> 24) & 0xff);

        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        header[20] = 1;
        header[21] = 0;

        header[22] = (byte) ks.getInternalAudioChannelNumber();
        header[23] = 0;

        header[24] = (byte) (ks.getAudioSampleRate() & 0xff);
        header[25] = (byte) ((ks.getAudioSampleRate() >> 8) & 0xff);
        header[26] = (byte) ((ks.getAudioSampleRate() >> 16) & 0xff);
        header[27] = (byte) ((ks.getAudioSampleRate() >> 24) & 0xff);

        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        header[32] = (byte) (ks.getInternalAudioChannelNumber() * (bitsPerSample / 8));
        header[33] = 0;

        header[34] = bitsPerSample;
        header[35] = 0;

        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        header[40] = (byte) (size & 0xff);
        header[41] = (byte) ((size >> 8) & 0xff);
        header[42] = (byte) ((size >> 16) & 0xff);
        header[43] = (byte) ((size >> 24) & 0xff);

        randomAccessFile.write(header);

        randomAccessFile.close();
    }

    @OptIn(markerClass = UnstableApi.class) @SuppressLint("WrongConstant")
    public static void combineAudioAndVideo(Context ctx, File audioM4a, File video, File dest, @Nullable Runnable onComplete, @Nullable Runnable onError) throws Exception {
        if(!dest.exists()) {
            dest.createNewFile();
        }

        final Transformer transformer = new Transformer.Builder(ctx)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .build();

        final EditedMediaItem editVideo = new EditedMediaItem.Builder(
            MediaItem.fromUri(video.getAbsolutePath())
        ).build();

        final EditedMediaItem editAudio = new EditedMediaItem.Builder(
            MediaItem.fromUri(audioM4a.getAbsolutePath())
        ).build();

        Composition composition = new Composition.Builder(
            new EditedMediaItemSequence(editVideo),
            new EditedMediaItemSequence(ImmutableList.of(editAudio),false)
        ).build();

        if(onComplete != null) {
            transformer.addListener(new Transformer.Listener() {
                @Override
                public void onCompleted(@NonNull Composition composition, @NonNull ExportResult exportResult) {
                    onComplete.run();
                }
            });
        }

        if(onError != null) {
            transformer.addListener(new Transformer.Listener() {
                @Override
                public void onError(Composition composition, ExportResult exportResult, ExportException exportException) {
                    onError.run();
                }
            });
        }

        transformer.start(composition, dest.getAbsolutePath());
    }
}
