package dev.dect.kapture.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import fi.iki.elonen.NanoHTTPD;

public class KSecurity {
    public static final int PASSWORD_LENGTH = 5;

    private String PASSWORD;

    private final ArrayList<String> AUTHORIZED_IPS = new ArrayList<>();

    public KSecurity() {
        generateNewPassword();
    }

    public void generateNewPassword() {
        String r = "";

        for(int i = 0; i < PASSWORD_LENGTH; i++) {
            r += String.valueOf(new Random().nextInt(10));
        }

        PASSWORD = r;
    }

    public boolean hasAccess(NanoHTTPD.IHTTPSession session) {
        return AUTHORIZED_IPS.contains(session.getRemoteIpAddress());
    }

    public boolean validateSessionLogin(NanoHTTPD.IHTTPSession session) {
        if(session.getMethod() == NanoHTTPD.Method.POST) {
            try {
                session.parseBody(new HashMap<>());

                if(session.getParms().containsKey("p") && session.getParms().get("p").equals(PASSWORD)) {
                    AUTHORIZED_IPS.add(session.getRemoteIpAddress());

                    return true;
                }
            } catch (Exception ignore) {}
        }

        return false;
    }

    public String getPasswordFormated() {
        return PASSWORD.replace("", " ");
    }

    public String getHiddenPassword() {
        String r = "";

        for(int i = 0; i < PASSWORD_LENGTH; i++) {
            r += "•";
        }

        return r.replace("", " ");
    }
}
