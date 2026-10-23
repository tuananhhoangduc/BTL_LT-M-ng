package client;

import java.util.UUID;
import java.util.prefs.Preferences;

/** Two active sessions share saved defaults, but never share live presentation state. */
public final class SettingsChecks {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        Preferences node =
                Preferences.userNodeForPackage(SettingsChecks.class)
                        .node("tests/session-" + UUID.randomUUID());
        try {
            SessionSettings first = new SessionSettings(node);
            SessionSettings second = new SessionSettings(node);
            first.flag("muted", true);
            check(first.muted() && !second.muted(), "Muting client one must not mute client two");
            check(node.getBoolean("muted", false), "Mute still saved for next launch");
            first.volume("master", 20);
            first.volume("music", 0);
            first.volume("sfx", 0);
            first.flag("reducedMotion", true);
            check(
                    second.master() == 70
                            && second.music() == 14
                            && second.sfx() == 65
                            && !second.reducedMotion(),
                    "Other running client's settings stay unchanged");
            second.volume("music", 55);
            check(
                    first.music() == 0 && second.music() == 55,
                    "Volume can differ per active client");
            SessionSettings reopened = new SessionSettings(node);
            check(
                    reopened.muted()
                            && reopened.master() == 20
                            && reopened.music() == 55
                            && reopened.sfx() == 0
                            && reopened.reducedMotion(),
                    "Next launch loads latest saved values per key");
            second.flag("muted", false);
            check(first.muted() && reopened.muted(), "Unmute cannot change another active session");
            first.volume("master", 300);
            first.volume("sfx", -10);
            check(first.master() == 100 && first.sfx() == 0, "Volume remains bounded");
            System.out.println(
                    "PASS: client-local mute/volume/motion, saved defaults on reopen, volume"
                        + " bounds");
        } finally {
            node.removeNode();
            node.flush();
        }
    }
}
