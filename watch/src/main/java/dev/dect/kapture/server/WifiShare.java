package dev.dect.kapture.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.data.Constants;
import dev.dect.kapture.data.DefaultSettings;
import dev.dect.kapture.data.KSharedPreferences;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.popup.DialogPopup;
import dev.dect.kapture.popup.WiFiSharePopup;
import dev.dect.kapture.utils.KFile;
import fi.iki.elonen.NanoHTTPD;

public class WifiShare extends NanoHTTPD {
    public static final int PORT = 8080;

    private final Context CONTEXT;

    private final ArrayList<Kapture> KAPTURES;

    private final ArrayList<String> LOCATIONS = new ArrayList<>();

    private final WiFiSharePopup POPUP;

    private final boolean IS_TO_REQUEST_PASSWORD,
                          IS_TO_REFRESH_PASSWORD;

    private int ATTEMPTS = 0;

    private KSecurity KSECURITY;

    public WifiShare(Context ctx, ArrayList<Kapture> kaptures) {
        super(PORT);

        this.CONTEXT = ctx;
        this.KAPTURES = kaptures;

        for(Kapture kapture : kaptures) {
            LOCATIONS.add(kapture.getLocation());
        }

        final SharedPreferences sp = KSharedPreferences.getAppSp(ctx);

        this.IS_TO_REQUEST_PASSWORD = sp.getBoolean(Constants.Sp.App.WIFI_SHARE_IS_TO_SHOW_PASSWORD, DefaultSettings.WIFI_SHARE_IS_TO_SHOW_PASSWORD);
        this.IS_TO_REFRESH_PASSWORD = sp.getBoolean(Constants.Sp.App.WIFI_SHARE_IS_TO_REFRESH_PASSWORD, DefaultSettings.WIFI_SHARE_IS_TO_REFRESH_PASSWORD);

        this.POPUP = new WiFiSharePopup(ctx);
        this.POPUP.setPort(PORT);
        this.POPUP.setListener(this::stop);

        if(IS_TO_REQUEST_PASSWORD) {
            this.KSECURITY = new KSecurity();
            this.POPUP.setKSecurity(this.KSECURITY);
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session) {
        final String path = session.getUri();

        if(path.equals("/favicon.ico")) {
            try {
                return newChunkedResponse(Response.Status.OK,  "image/x-icon", CONTEXT.getResources().openRawResource(R.raw.favicon));
            } catch(Exception e) {
                return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, "image/x-icon", "");
            }
        }

        if(IS_TO_REQUEST_PASSWORD && !KSECURITY.hasAccess(session)) {
            if(KSECURITY.validateSessionLogin(session)) {
                newDeviceConnected();
            } else {
                if(++ATTEMPTS == 3) {
                    requestNewPassword();
                }

                return newFixedLengthResponse(new KHtml(CONTEXT).getLogin());
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

    public void start() {
        if(KAPTURES.isEmpty()) {
            Toast.makeText(CONTEXT, CONTEXT.getString(R.string.toast_error_no_kaptures_found), Toast.LENGTH_SHORT).show();

            return;
        }

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

            POPUP.setIP(ip);

            POPUP.refreshPasswordField();

            POPUP.show();
        } catch (IOException e) {
            if(e.getMessage().contains("EADDRINUSE")) {
                new DialogPopup(
                    CONTEXT,
                    R.string.wifi_share_popup_error,
                    R.string.wifi_share_popup_error_message,
                    DialogPopup.RES_ID_DEFAULT,
                    () -> System.exit(0),
                    DialogPopup.RES_ID_DEFAULT,
                    null,
                    false,
                    false,
                    true
                ).show();

                return;
            }

            Toast.makeText(CONTEXT, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void newDeviceConnected() {
        if(IS_TO_REFRESH_PASSWORD) {
            requestNewPassword();

            ((Vibrator) CONTEXT.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void requestNewPassword() {
        ATTEMPTS = 0;

        KSECURITY.generateNewPassword();
        POPUP.refreshPasswordField();
    }
}
