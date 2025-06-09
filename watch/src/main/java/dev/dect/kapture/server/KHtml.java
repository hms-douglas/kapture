package dev.dect.kapture.server;

import android.content.Context;

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

    private String getLoginTags() {
        return "<meta charset='UTF-8'>"
            + "<title>" + CONTEXT.getString(R.string.app_name) + " - " + CONTEXT.getString(R.string.html_login) + "</title>"
            + "<meta name='viewport' content='user-scalable=no'>"
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

        for(Kapture kapture : kaptures) {
            body += "<tr>"
                + "<td>" + kapture.getName() + "</td>"
                + "<td>" + KFile.formatFileDate(kapture.getCreationTime()) + "</td>"
                + "<td>" + KFile.formatFileDuration(kapture.getDuration()) + "</td>"
                + "<td>" + kapture.getVideoSize()[0] + "x" + kapture.getVideoSize()[1]+ "</td>"
                + "<td>" + KFile.formatFileSize(kapture.getSize()) + "</td>"
                + "<td>";


            body += "<div class='play' data-href='" + kapture.getLocation() + "' data-type='video'></div><a class='download' href='" + kapture.getLocation() + "'></a></td></tr>";
        }

        body += "</table>";

        CSS.setList();

        JAVASCRIPT.addList();

        body += getPlayersString();

        return "<html><head>" + getTags() + "</head><body><style>" + CSS.getCss() + "</style>" + body + "<script>" + JAVASCRIPT.getJavascript() + "</script></body></html>";
    }

    private String getHeaderString(int amount) {
        return "<div id='title'><div><span>" + CONTEXT.getString(R.string.app_name) + "</span><span>" + amount + " " + CONTEXT.getString((amount == 1 ? R.string.kapture : R.string.kapture_plural)) + "</span></div></div>";
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

    public String getLogin() {
        JAVASCRIPT.setLogin();

        String inputs = "";

        for(int i = 0; i < KSecurity.PASSWORD_LENGTH; i++) {
            inputs += "<input class=\"pass\"type=\"number\"/>";
        }

        return "<html><head>" + getLoginTags() + "</head><body><style>" + CSS.getLogin()  + "</style><div id=\"cp\"><label>" + CONTEXT.getString(R.string.html_enter_password) + "</label><div id=\"cpi\">" + inputs + "</div></div><form enctype=\"multipart/form-data\"method=\"post\"id=\"l\"><input type=\"hidden\"name=\"p\"id=\"p\"/></form><div id=\"h\"></div><div class=\"lo\"></div><script>" + JAVASCRIPT.getJavascript() + "</script></body></html>";
    }
}
