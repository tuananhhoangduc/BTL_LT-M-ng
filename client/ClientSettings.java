package client;

import java.util.prefs.Preferences;

/** Local presentation preferences; never part of a match. */
final class ClientSettings {
    private static final SessionSettings SETTINGS =
            new SessionSettings(Preferences.userNodeForPackage(ClientSettings.class));

    private ClientSettings() {}

    static int master() {
        return SETTINGS.master();
    }

    static int music() {
        return SETTINGS.music();
    }

    static int sfx() {
        return SETTINGS.sfx();
    }

    static boolean muted() {
        return SETTINGS.muted();
    }

    static boolean reducedMotion() {
        return SETTINGS.reducedMotion();
    }

    static void volume(String key, int value) {
        SETTINGS.volume(key, value);
    }

    static void flag(String key, boolean value) {
        SETTINGS.flag(key, value);
    }

    static String host() {
        return SETTINGS.host();
    }

    static void host(String host) {
        SETTINGS.host(host);
    }
}
