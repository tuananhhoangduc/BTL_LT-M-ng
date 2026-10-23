package client;

import java.io.BufferedInputStream;
import java.util.*;
import java.util.concurrent.*;

import javax.sound.sampled.*;

/** Mixer I/O runs on one bounded worker, never on Swing's EDT. */
final class SoundManager {
    private static final String[] NAMES = {
        "click", "move", "capture", "chat", "challenge", "win", "lose", "start", "tick"
    };
    private static final Map<String, List<Clip>> CLIPS = new HashMap<>();
    private static final ThreadPoolExecutor WORKER =
            new ThreadPoolExecutor(
                    1,
                    1,
                    0,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(24),
                    r -> {
                        Thread t = new Thread(r, "audio-mixer");
                        t.setDaemon(true);
                        return t;
                    },
                    new ThreadPoolExecutor.DiscardPolicy());
    private static Clip music;
    private static final ScheduledExecutorService FADER =
            Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r, "audio-fade");
                        t.setDaemon(true);
                        return t;
                    });
    private static float musicLevel;
    private static long duckUntil;
    private static float duckFactor = .35f;
    private static String scene = "menu";
    private static boolean loaded;
    private static long lastChat;
    private static long priorityUntil;

    private SoundManager() {}

    static void init() {
        WORKER.execute(SoundManager::load);
    }

    static void setEnabled(boolean on) {
        ClientSettings.flag("muted", !on);
        refresh();
    }

    static boolean isEnabled() {
        return !ClientSettings.muted();
    }

    static void playClick() {
        play("click");
    }

    static void playMove() {
        play("move");
    }

    static void playCapture() {
        play("capture");
    }

    static void playChallenge() {
        play("challenge");
    }

    static void playWin() {
        play("win");
    }

    static void playLose() {
        play("lose");
    }

    static void playGameStart() {
        play("start");
    }

    static void playTurn() {
        play("start");
    }

    static float cueLevel(String name) {
        return switch (name) {
            case "click", "start" -> .45f;
            case "chat", "tick" -> .30f;
            case "move", "win", "lose" -> .85f;
            case "challenge" -> .80f;
            default -> 1f;
        };
    }

    static void playTick() {
        play("tick");
    }

    static synchronized void playChat() {
        long now = System.nanoTime();
        if (now - lastChat > 500_000_000L) {
            lastChat = now;
            play("chat");
        }
    }

    static void scene(String name) {
        WORKER.execute(
                () -> {
                    scene = name;
                    updateMusic();
                });
    }

    static void refresh() {
        WORKER.execute(
                () -> {
                    float level = volume(false);
                    CLIPS.forEach(
                            (name, pool) ->
                                    pool.forEach(
                                            clip -> {
                                                if (level <= 0) clip.stop();
                                                else gain(clip, level * cueLevel(name));
                                            }));
                    updateMusic();
                });
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        for (String name : NAMES) {
            List<Clip> pool = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                Clip c = open(name);
                if (c != null) pool.add(c);
            }
            CLIPS.put(name, pool);
        }
        music = openPath("audio/bgm/puzzle_bright.wav");
        FADER.scheduleAtFixedRate(
                () -> {
                    if (!WORKER.isShutdown()) WORKER.execute(SoundManager::updateMusic);
                },
                0,
                50,
                TimeUnit.MILLISECONDS);
    }

    private static Clip open(String name) {
        return openPath("audio/" + name + ".wav");
    }

    private static Clip openPath(String path) {
        Clip clip = null;
        try (var input = AssetLoader.open(path)) {
            if (input == null) return null;
            try (AudioInputStream audio =
                    AudioSystem.getAudioInputStream(new BufferedInputStream(input))) {
                clip = AudioSystem.getClip();
                clip.open(audio);
                return clip;
            }
        } catch (Exception ignored) {
            if (clip != null) clip.close();
            return null;
        }
    }

    private static float volume(boolean background) {
        if (ClientSettings.muted()) return 0;
        float value =
                ClientSettings.master()
                        * (background ? ClientSettings.music() : ClientSettings.sfx())
                        / 10000f;
        return background && "game".equals(scene) ? value * .55f : value;
    }

    private static void gain(Clip clip, float value) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = value <= 0 ? control.getMinimum() : (float) (20 * Math.log10(value));
            control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), db)));
        }
    }

    private static void play(String name) {
        if (!isEnabled()) return;
        long requested = System.nanoTime();
        WORKER.execute(
                () -> {
                    load();
                    long now = System.nanoTime();
                    // Drop stale cues instead of playing a delayed burst after mixer startup.
                    if (now - requested > 500_000_000L) return;
                    if ((name.equals("chat") || name.equals("click") || name.equals("tick"))
                            && now < priorityUntil) return;
                    float value = volume(false) * cueLevel(name);
                    if (value <= 0) return;
                    if (name.equals("capture")
                            || name.equals("start")
                            || name.equals("win")
                            || name.equals("lose")) {
                        priorityUntil = now + 450_000_000L;
                        for (String quiet : List.of("chat", "tick"))
                            CLIPS.getOrDefault(quiet, List.of()).forEach(Clip::stop);
                    }
                    if (name.equals("capture") && now >= duckUntil) {
                        duckFactor = .70f;
                        duckUntil = now + 450_000_000L;
                    }
                    if (name.equals("win") || name.equals("lose") || name.equals("challenge")) {
                        duckFactor = .35f;
                        duckUntil = System.nanoTime() + 1_500_000_000L;
                    }
                    for (Clip c : CLIPS.getOrDefault(name, List.of())) {
                        if (!c.isRunning()) {
                            gain(c, value);
                            c.setFramePosition(0);
                            c.start();
                            break;
                        }
                    }
                });
    }

    private static void updateMusic() {
        if (music == null) return;
        float value = volume(true);
        if (value <= 0) {
            music.stop();
            musicLevel = 0;
            return;
        }
        if (System.nanoTime() < duckUntil) value *= duckFactor;
        musicLevel += (value - musicLevel) * .18f;
        gain(music, musicLevel);
        if (!music.isRunning()) music.loop(Clip.LOOP_CONTINUOUSLY);
    }

    static void shutdown() {
        FADER.shutdownNow();
        FutureTask<Void> closing =
                new FutureTask<>(
                        () -> {
                            CLIPS.values().forEach(pool -> pool.forEach(Clip::close));
                            if (music != null) music.close();
                            return null;
                        });
        WORKER.getQueue().clear();
        WORKER.execute(closing);
        WORKER.shutdown();
        try {
            closing.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException ignored) {
        }
    }
}
