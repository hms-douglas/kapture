package dev.dect.kapture.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.arthenica.ffmpegkit.FFmpegKit;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import dev.dect.kapture.R;
import dev.dect.kapture.activity.viewer.AudioActivity;
import dev.dect.kapture.activity.viewer.VideoActivity;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.KSettings;

/** @noinspection ResultOfMethodCallIgnored*/
public class KFile {
    public static final String FILE_SEPARATOR = "_";

    private static final String VIDEO_EXTENSION = ".mp4",
                                INTERNAL_STORAGE_PATH = "/storage/emulated/0";

    public static File generateNewEmptyKaptureFile(Context ctx, KSettings ks) {
        final SharedPreferences sp = ctx.getSharedPreferences(Constants.SP, Context.MODE_PRIVATE);

        final int fileId = sp.getInt(Constants.SP_KEY_LAST_FILE_ID, 0) + 1;

        sp.edit().putInt(Constants.SP_KEY_LAST_FILE_ID, fileId).apply();

        final String fileName =
                String.format(Locale.getDefault(), "%03d", fileId)
                + FILE_SEPARATOR
                + getDefaultKaptureFileName(ctx)
                + FILE_SEPARATOR
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));

        return new File(ks.getSavingLocationFile(), fileName + VIDEO_EXTENSION);
    }

    public static String uriToAbsolutPath(Context ctx, Uri uri) {
        final String uriString = uri.toString();

        String path;

        if(uriString.startsWith("content://com.android.externalstorage.documents/tree/primary%3A")) {
            path = uriString.replaceFirst("content://com.android.externalstorage.documents/tree/primary%3A", "");
        } else {
            path = uriString.replaceFirst("content://com.sec.android.app.myfiles.FileProvider/device_storage/0/", "");
        }

        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception ingore) {
            Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
        }

        File folder = new File(INTERNAL_STORAGE_PATH, path);

        if(!folder.exists()) {
            folder = getDefaultFileLocation(ctx);

            Toast.makeText(ctx, ctx.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();
        }

        return folder.getAbsolutePath();
    }

    public static String formatAndroidPathToUser(Context ctx, String path) {
        return path.replaceFirst(INTERNAL_STORAGE_PATH, File.separator + ctx.getString(R.string.internal_storage));
    }

    public static String formatFileSize(long l) {
        try {
            final int i = l == 0 ? 0 : (int) Math.floor(Math.log(l) / Math.log(1024));

            final double result = (l / Math.pow(1024, i));

            final DecimalFormat format = new DecimalFormat("0.00");

            return  format.format(result) + " " + new String[]{"B", "kB", "MB", "GB", "TB"}[i];
        } catch (Exception ignore) {
            return "?";
        }
    }

    public static String formatFileDate(long l) {
        return Utils.Formatter.date(l);
    }

    public static String formatFileDuration(long milli) {
        return Utils.Formatter.time(milli);
    }

    public static File getDefaultFileLocation(Context ctx) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), formatStringResource(ctx, R.string.folder_name));
    }

    public static String formatStringResource(Context ctx, int resId) {
        return ctx.getString(resId).replaceAll(" ", FILE_SEPARATOR);
    }

    public static void openFile(Context ctx, String path) {
        openFile(ctx, new File(path));
    }

    public static void openFile(Context ctx, File f) {
        openFile(ctx, f, false);
    }

    public static void openFile(Context ctx, String path, boolean external) {
        openFile(ctx, new File(path), external);
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
            if(type.contains("audio/")) {
                i.setClass(ctx, AudioActivity.class);
                i.putExtra(AudioActivity.INTENT_URL, f.getAbsolutePath());
            } else if(type.contains("video/")) {
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

    public static void shareFile(Context ctx, String path) {
        shareFile(ctx, new File(path));
    }

    public static void shareFile(Context ctx, File f) {
        final Uri fileURI = FileProvider.getUriForFile(ctx, ctx.getPackageName(), f);

        final Intent i = new Intent(Intent.ACTION_SEND);

        i.putExtra(Intent.EXTRA_STREAM, fileURI);
        i.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(f)));

        ctx.startActivity(Intent.createChooser(i, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static void shareFiles(Context ctx, ArrayList<Uri> uris) {
        final Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);

        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        i.setType("video/*");

        ctx.startActivity(Intent.createChooser(i, null));
    }

    public static String getFileExtension(File f) {
        return f.getName().substring(f.getName().lastIndexOf(".") + 1);
    }

    public static String removeFileExtension(String name) {
        return name.substring(0, name.lastIndexOf("."));
    }

    public static File renameFile(String name, String path) {
        return renameFile(name, new File(path));
    }

    public static File renameFile(String name, File f) {
        final File newFile = new File(f.getParentFile(), name + "." + getFileExtension(f));

        f.renameTo(newFile);

        return newFile;
    }

    public static void copyFile(File from, File to) {
        try {
            Files.copy(from.toPath(), to.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pmcToMp3(File pmc, File mp3) {
        FFmpegKit.execute(
        "-f s16le -ar 44.1k -ac 2 -i " + pmc.getAbsolutePath() + " -y " + mp3.getAbsolutePath()
        );
    }

    public static String getDefaultKaptureFileName(Context ctx) {
        return formatStringResource(ctx, R.string.file_name);
    }
}
