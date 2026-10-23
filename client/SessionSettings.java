package client;

import java.util.prefs.Preferences;

/** Load saved defaults once. Another running client must not change this client's mixer. */
final class SessionSettings {
    private final Preferences store;
    private volatile int master, music, sfx;
    private volatile boolean muted, reducedMotion;
    private volatile String host;

    SessionSettings(Preferences store) {
        this.store = store;
        master = bounded(store.getInt("master", 70));
        music = bounded(store.getInt("music", 14));
        sfx = bounded(store.getInt("sfx", 65));
        muted = store.getBoolean("muted", false);
        reducedMotion = store.getBoolean("reducedMotion", false);
        host = store.get("host", "127.0.0.1");
    }

    int master() {
        return master;
    }

    int music() {
        return music;
    }

    int sfx() {
        return sfx;
    }

    boolean muted() {
        return muted;
    }

    boolean reducedMotion() {
        return reducedMotion;
    }

    String host() {
        return host;
    }

    private static int bounded(int value) {
        return Math.max(0, Math.min(100, value));
    }

    synchronized void volume(String key, int value) {
        int level = bounded(value);
        switch (key) {
            case "master" -> master = level;
            case "music" -> music = level;
            case "sfx" -> sfx = level;
            default -> throw new IllegalArgumentException("Unknown volume key: " + key);
        }
        store.putInt(key, level);
    }

    synchronized void flag(String key, boolean value) {
        switch (key) {
            case "muted" -> muted = value;
            case "reducedMotion" -> reducedMotion = value;
            default -> throw new IllegalArgumentException("Unknown flag key: " + key);
        }
        store.putBoolean(key, value);
    }

    synchronized void host(String value) {
        store.put("host", value);
        host = value;
    }
}
