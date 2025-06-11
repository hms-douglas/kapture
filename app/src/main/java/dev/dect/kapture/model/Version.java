package dev.dect.kapture.model;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;

import dev.dect.kapture.data.Constants;

public class Version implements Serializable {
    private String VERSION_NAME,
                   LINK;

    private int VERSION_CODE = -1;

    private boolean ERROR;

    public Version(JSONObject jsonObject) {
        try {
            this.VERSION_NAME = jsonObject.getString(Constants.Url.App.KeyTag.LATEST_VERSION_VERSION_NAME);
            this.LINK = jsonObject.getString(Constants.Url.App.KeyTag.LATEST_VERSION_LINK);
            this.VERSION_CODE = Integer.parseInt(jsonObject.getString(Constants.Url.App.KeyTag.LATEST_VERSION_VERSION_CODE));
            this.ERROR = false;
        } catch (Exception e) {
            this.ERROR = true;
        }
    }

    public Version(File file) {
        final String[] info = file.getName().split(Constants.VERSION_FILE_SEPARATOR);

        try {
            this.VERSION_NAME = info[1];
            this.LINK = file.getAbsolutePath();
            this.VERSION_CODE = Integer.parseInt(info[2].substring(0, info[2].indexOf(".")));
            this.ERROR = false;
        } catch (Exception e) {
            this.ERROR = true;
        }
    }

    public boolean isValid() {
        return ERROR || (VERSION_NAME != null && LINK != null && VERSION_CODE != -1);
    }

    public boolean isFile() {
        return !LINK.startsWith("http");
    }

    public String getVersionName() {
        return VERSION_NAME;
    }

    public String getLinkPath() {
        return LINK;
    }

    public int getVersionCode() {
        return VERSION_CODE;
    }

    public String getFileName() {
        return Constants.VERSION_FILE_PREFIX + Constants.VERSION_FILE_SEPARATOR + VERSION_NAME + Constants.VERSION_FILE_SEPARATOR + VERSION_CODE + ".apk";
    }
}
