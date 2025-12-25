package dev.dect.kapture.model;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import dev.dect.kapture.R;

public class Adb {
    private final String TAG = Adb.class.getSimpleName();

    private final Context CONTEXT;

    private final String ADB_PATH,
                         HOME_PATH,
                         TEMP_PATH;

    private String CONNECTION_IP_PORT;

    public Adb(Context ctx) {
        this.CONTEXT = ctx;

        this.ADB_PATH = ctx.getApplicationInfo().nativeLibraryDir + "/libadb.so";
        this.HOME_PATH = CONTEXT.getFilesDir().getPath();
        this.TEMP_PATH = CONTEXT.getCacheDir().getPath();
    }

    public String pair(String address, String code) {
        return runCommand(
            new String[]{ADB_PATH, "pair", address, code}
        );
    }

    public String disconnectALL() {
        this.CONNECTION_IP_PORT = "";

        return runCommand(
            new String[]{ADB_PATH, "disconnect"}
        );
    }

    public String connect(String address) {
        this.CONNECTION_IP_PORT = address;

        return runCommand(
            new String[]{ADB_PATH, "connect", address}
        );
    }

    public String install(File file) {
        if(CONNECTION_IP_PORT == null || CONNECTION_IP_PORT.isEmpty()) {
            return null;
        }

        if(!file.exists()) {
            return CONTEXT.getString(R.string.progress_installing_not_found);
        }

        if(!file.getName().toLowerCase(Locale.ROOT).endsWith(".apk")) {
            return CONTEXT.getString(R.string.progress_installing_wrong_type);
        }

        return runCommand(
            new String[]{ADB_PATH, "-s", CONNECTION_IP_PORT, "install", file.getAbsolutePath()}
        );
    }

    public String addSecureSettings() {
        return runCommand(
            new String[]{ADB_PATH, "-s", CONNECTION_IP_PORT, "shell", "pm", "grant", CONTEXT.getPackageName(), "android.permission.WRITE_SECURE_SETTINGS"}
        );
    }

    private String runCommand(String[] command) {
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder(command);

            processBuilder.redirectErrorStream(true);

            final Map<String, String> environment = processBuilder.environment();

            environment.put("HOME", HOME_PATH);
            environment.put("TMPDIR", TEMP_PATH);

            final Process process = processBuilder.start();

            final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));

            final String output = stdOut.lines().collect(Collectors.joining(System.lineSeparator()));

            process.waitFor();
            process.destroy();

            return output;
        } catch (Exception e) {
            Log.e(TAG, "runCommand: " + e.getMessage());
        }

        return null;
    }
}
