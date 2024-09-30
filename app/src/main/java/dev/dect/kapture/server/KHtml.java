package dev.dect.kapture.server;

import android.content.Context;

import org.json.JSONObject;

import java.util.ArrayList;

import dev.dect.kapture.R;
import dev.dect.kapture.model.Kapture;
import dev.dect.kapture.utils.KFile;

public class KHtml {
    private final Context CONTEXT;

    private final KCss CSS;

    private final KJavascript JAVASCRIPT;

    public KHtml(Context ctx) {
        this.CONTEXT = ctx;

        this.CSS = new KCss(ctx);

        this.JAVASCRIPT = new KJavascript();
    }

    private String getTags() {
        return "<meta charset='UTF-8'>"
            + "<title>" + CONTEXT.getString(R.string.app_name) + "</title>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0'>"
            + "<link rel='icon' type='image/x-icon' href='/favicon.ico'>";
    }

    public String getList(ArrayList<Kapture> kaptures) {
        String body = getHeaderString(kaptures.size())
            + "<table><tr>"
            + "<th>" + CONTEXT.getString(R.string.html_name) + "</th>"
            + "<th>" + CONTEXT.getString(R.string.html_date) + "</th>"
            + "<th>" + CONTEXT.getString(R.string.html_duration) + "</th>"
            + "<th>" + CONTEXT.getString(R.string.html_resolution) + "</th>"
            + "<th>" + CONTEXT.getString(R.string.html_size) + "</th>"
            + "<th></th></tr>";

        boolean hasExtra = false;

        for(Kapture kapture : kaptures) {
            boolean b = kapture.hasExtras();

            body += "<tr>"
                + "<td>" + kapture.getName() + "</td>"
                + "<td>" + KFile.formatFileDate(kapture.getCreationTime()) + "</td>"
                + "<td>" + KFile.formatFileDuration(kapture.getDuration()) + "</td>"
                + "<td>" + kapture.getThumbnail().getWidth() + "x" + kapture.getThumbnail().getHeight() + "</td>"
                + "<td>" + KFile.formatFileSize(kapture.getSize()) + "</td>"
                + "<td>";

            if(b) {
                hasExtra = true;

                String extrasBody = "";

                final ArrayList<Kapture.Extra> extras = kapture.getExtras();

                for(int i = 0; i < extras.size(); i++) {
                    Kapture.Extra extra = extras.get(i);

                    try {
                        final JSONObject json = new JSONObject();

                        json.put("n", extra.getTypeName(CONTEXT));
                        json.put("l", extra.getLocation());

                        extrasBody += json + ((i == extras.size() - 1) ? "" : ",");
                    } catch (Exception ignore) {
                        hasExtra = false;
                    }
                }

                body += "<div class='extra' data-extras='[" + extrasBody + "]'></div>";
            }

            body += "<div class='play' data-href='" + kapture.getLocation() + "' data-type='video'></div><a class='download' href='" + kapture.getLocation() + "'></a></td></tr>";
        }

        body += "</table>";

        CSS.setList();

        JAVASCRIPT.addList();

        if(hasExtra) {
            CSS.addExtra();

            JAVASCRIPT.addExtra();

            body += getExtraString();
        }

        body += getPlayersString();

        return "<html><head>" + getTags() + "</head><body><style>" + CSS.getCss() + "</style>" + body + "<script>" + JAVASCRIPT.getJavascript() + "</script></body></html>";
    }

    private String getHeaderString(int amount) {
        return "<div id='title'><div><span>" + CONTEXT.getString(R.string.app_name) + "</span><span>" + amount + " " + CONTEXT.getString((amount == 1 ? R.string.kapture : R.string.kapture_plural)) + "</span></div></div>";
    }

    private String getExtraString() {
        return "<div id=\"extras\"><div id=\"extras-body\"><span id=\"extras-title\">" + CONTEXT.getString(R.string.popup_title_extra) + "</span><div id=\"closeExtras\">" + CONTEXT.getString(R.string.popup_btn_close) + "</div></div></div>";
    }

    private String getPlayersString() {
        return "<div id=\"players\"><video controls id=\"video\"></video><audio controls id=\"audio\"></audio><div id=\"closePlayers\">" + CONTEXT.getString(R.string.popup_btn_close) + "</div></div>";
    }

    private String getDefaultError(int number, int message) {
        return "<html><head>" + getTags() + "</head><body><style>" + CSS.getCss() + "</style><div id='containerError'><span id='number'>" + number + "</span><span id='message'>" + CONTEXT.getString(message) + "</span></div></body></html>";
    }

    public String get403() {
        CSS.set403();

        return getDefaultError(403, R.string.html_403);
    }

    public String get500() {
        CSS.set500();

        return getDefaultError(500, R.string.html_500);
    }
}
