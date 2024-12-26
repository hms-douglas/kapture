package dev.dect.kapture.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.data.DB;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.WiFiSharePopup;
import dev.dect.kapture.utils.KFile;
import fi.iki.elonen.NanoHTTPD;

public class WifiShare extends NanoHTTPD {
    public interface OnWifiShareListener {
        void onStop();
    }

    public static final int PORT = 8080;

    private final Context CONTEXT;

    private final ArrayList<Kapture> KAPTURES;

    private final ArrayList<String> LOCATIONS = new ArrayList<>();

    private OnWifiShareListener LISTENER;

    public WifiShare(Context ctx) {
        this(ctx, new DB(ctx).selectAllKaptures(true));
    }

    public WifiShare(Context ctx, ArrayList<Kapture> kaptures) {
        super(PORT);

        this.CONTEXT = ctx;
        this.KAPTURES = kaptures;

        for(Kapture kapture : kaptures) {
            LOCATIONS.add(kapture.getLocation());

            for(Kapture.Extra extra : kapture.getExtras()) {
                LOCATIONS.add(extra.getLocation());
            }

            for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {
                LOCATIONS.add(screenshot.getLocation());
            }
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        final String path = session.getUri();

        if(path.equals("/favicon.ico")) {
            try {
                return newChunkedResponse(Response.Status.OK,  "image/x-icon", CONTEXT.getResources().openRawResource(R.raw.favicon));
            } catch(Exception e) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "image/x-icon", "");
            }
        }

        if(path.equals("/")) {
            return newFixedLengthResponse(new KHtml(CONTEXT).getList(KAPTURES));
        } else {
            final File file = new File(path);

            if(LOCATIONS.contains(file.getAbsolutePath())) {
                if(session.getHeaders().get("accept").equals("*/*")) {
                    return playFile(file);
                } else {
                    return downloadFile(file);
                }
            } else {
                return newFixedLengthResponse(new KHtml(CONTEXT).get403());
            }
        }
    }

    public WifiShare setListener(OnWifiShareListener listener) {
        this.LISTENER = listener;

        return this;
    }

    public void start() {
        final String ip = getIdAddress();

        if(ip == null) {
            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_no_wifi), Toast.LENGTH_SHORT).show();

            return;
        } else if(ip.equals("?")) {
            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            return;
        }

        try {
            super.start();

            new WiFiSharePopup(
                CONTEXT,
                ip,
                PORT,
                KAPTURES.size(),
                    () -> {
                        stop();

                        if(LISTENER != null) {
                            LISTENER.onStop();
                        }
                    }
            ).show();

        } catch (Exception ignore) {
            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            super.stop();
        }
    }

    private Response downloadFile(File file) {
        FileInputStream fileInputStream;

        try {
            fileInputStream = new FileInputStream(file);
        } catch (Exception ignore) {
            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            return newFixedLengthResponse(new KHtml(CONTEXT).get500());
        }

        final Response response = NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "application/octet-stream", fileInputStream, file.length());

        response.addHeader("Accept-Ranges", "bytes");

        return response;
    }

    private Response playFile(File file) {
        FileInputStream fileInputStream;

        try {
            fileInputStream = new FileInputStream(file);
        } catch (Exception ignore) {
            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_generic), Toast.LENGTH_SHORT).show();

            return newFixedLengthResponse(new KHtml(CONTEXT).get500());
        }

        final Response response = NanoHTTPD.newFixedLengthResponse(Response.Status.OK, MimeTypeMap.getSingleton().getMimeTypeFromExtension(KFile.getFileExtension(file)), fileInputStream, file.length());

        response.addHeader("Accept-Ranges", "bytes");

        return response;
    }

    private String getIdAddress() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) CONTEXT.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager == null) {
            return null;
        }

        final LinkProperties linkProperties = connectivityManager.getLinkProperties(connectivityManager.getActiveNetwork());

        for(LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
            if(!linkAddress.getAddress().getHostAddress().contains(":")) {
                return linkAddress.getAddress().getHostAddress();
            }
        }

        return "?";
    }
}
