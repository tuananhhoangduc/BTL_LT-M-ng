package client;

import java.util.prefs.Preferences;

/** Local presentation preferences; never part of a match. */
final class ClientSettings {
    private static final Preferences STORE = Preferences.userNodeForPackage(ClientSettings.class);

    private ClientSettings() {}

    static int master() {
        return STORE.getInt("master", 70);
    }

    static int music() {
        return STORE.getInt("music", 14);
    }

    static int sfx() {
        return STORE.getInt("sfx", 65);
    }

    static boolean muted() {
        return STORE.getBoolean("muted", false);
    }

    static boolean reducedMotion() {
        return STORE.getBoolean("reducedMotion", false);
    }

    static void volume(String key, int value) {
        STORE.putInt(key, Math.max(0, Math.min(100, value)));
    }

    static void flag(String key, boolean value) {
        STORE.putBoolean(key, value);
    }

    static String host() {
        return STORE.get("host", "127.0.0.1");
    }

    static void host(String host) {
        STORE.put("host", host);
    }
}
