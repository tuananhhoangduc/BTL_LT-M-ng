package client;

import common.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public final class PresentationChecks {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static BoardState state(
            String match, long sequence, int seconds, int turns, String color) {
        return new BoardState(
                new int[9][9],
                new int[9][9],
                turns,
                seconds,
                color,
                0,
                0,
                match,
                sequence,
                4,
                4,
                1,
                List.of());
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) -> {
                    error.printStackTrace();
                    System.exit(1);
                });
        BoardFeedback tracker = new BoardFeedback();
        SpriteCatalog.preload();
        check(
                SpriteCatalog.count() == 10,
                "All ten PNG assets must load; no silent vector fallback in delivery");
        check(
                tracker.accept(state("a", 7, 15, 40, "RED"), "RED").reset(),
                "Late spectator needs static baseline");
        check(
                !tracker.accept(state("a", 7, 14, 40, "RED"), "RED").move(),
                "Timer must not replay move");
        check(
                tracker.accept(state("a", 8, 15, 39, "BLUE"), "RED").move(),
                "New move must produce feedback");
        check(
                !tracker.accept(state("a", 8, 15, 39, "BLUE"), "RED").move(),
                "Duplicate must not replay");
        check(
                tracker.accept(state("b", 0, 15, 50, "RED"), "RED").reset(),
                "Rematch resets sequence");
        tracker.accept(state("b", 0, 6, 50, "RED"), "RED");
        check(
                tracker.accept(state("b", 0, 5, 50, "RED"), "RED").tick(),
                "Warn at five seconds on own turn");
        check(
                !tracker.accept(state("b", 0, 5, 50, "RED"), "RED").tick(),
                "Do not duplicate countdown cue");
        check(
                !tracker.accept(state("b", 0, 4, 50, "RED"), "SPECTATOR").tick(),
                "Spectator should not hear urgent own-turn cue");
        int assets = 0;
        for (String name :
                new String[] {
                    "click",
                    "move",
                    "capture",
                    "chat",
                    "challenge",
                    "tick",
                    "win",
                    "lose",
                    "start",
                    "bgm/puzzle_bright"
                }) {
            try (InputStream raw = AssetLoader.open("audio/" + name + ".wav");
                    AudioInputStream audio =
                            AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                check(audio.getFrameLength() > 0, "Empty audio " + name);
                check(audio.getFormat().getSampleSizeInBits() == 16, "PCM 16 " + name);
                assets++;
            }
        }
        SwingUtilities.invokeAndWait(
                () -> {
                    UITheme.installDefaults();
                    BoardPanel panel = new BoardPanel(false, System::nanoTime, () -> false);
                    panel.setSize(560, 560);
                    panel.update(
                            state("board", 0, 15, 50, "RED"),
                            "RED",
                            new BoardFeedback.Change(true, false, false, false));
                    panel.update(
                            state("board", 1, 15, 49, "BLUE"),
                            "RED",
                            new BoardFeedback.Change(false, true, true, false));
                    check(panel.animationRunning(), "Move starts one animation timer");
                    panel.stopEffect();
                    check(!panel.animationRunning(), "Dispose stops timer");
                    java.util.concurrent.atomic.AtomicInteger submitted =
                            new java.util.concurrent.atomic.AtomicInteger();
                    panel.onMove((row, col) -> submitted.incrementAndGet());
                    panel.update(
                            state("input", 0, 15, 50, "RED"),
                            "RED",
                            new BoardFeedback.Change(true, false, false, false));
                    panel.getActionMap().get("ENTER").actionPerformed(null);
                    panel.getActionMap().get("ENTER").actionPerformed(null);
                    check(submitted.get() == 1, "Only one outstanding move request");
                    check(
                            panel.isPending(),
                            "Pending visual state is exposed while request is in flight");
                    panel.update(
                            state("input", 0, 14, 50, "RED"),
                            "RED",
                            new BoardFeedback.Change(false, false, false, false));
                    panel.getActionMap().get("SPACE").actionPerformed(null);
                    check(submitted.get() == 1, "Timer must not unlock pending request");
                    panel.releasePending();
                    check(!panel.isPending(), "Error releases pending visual state");
                    panel.getActionMap().get("SPACE").actionPerformed(null);
                    check(submitted.get() == 2, "Rejected request allows retry");
                    panel.active(false);
                    check(!panel.isPending(), "Finished/disconnected board clears pending marker");
                    panel.releasePending();
                    panel.getActionMap().get("ENTER").actionPerformed(null);
                    check(submitted.get() == 2, "Finished/disconnected board cannot submit");
                    BufferedImage image = new BufferedImage(560, 560, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = image.createGraphics();
                    long start = System.nanoTime();
                    for (int i = 0; i < 120; i++) panel.paint(g);
                    System.out.printf(
                            "Board render average: %.2f ms (offscreen, not device FPS)%n",
                            (System.nanoTime() - start) / 120_000_000.0);
                    g.dispose();
                    motionChecks();
                });
        System.out.println(
                "PASS: snapshot feedback, countdown dedupe, timer lifecycle, pending input gate, "
                        + assets
                        + " WAV resources");
        if (args.length > 0 && args[0].equals("--screenshots")) screenshots();
    }

    private static void motionChecks() {
        check(
                BoardMotion.areaBlend(.18) == 0 && BoardMotion.areaBlend(.40) == 1,
                "Territory changes only during 180-400ms");
        check(
                BoardMotion.sourceAlpha(0) == 1 && BoardMotion.sourceAlpha(.12) == 0,
                "Source fades during first 120ms");
        check(
                BoardMotion.front(3, 0, .08) == 3 && BoardMotion.front(3, 5, .08) == 3,
                "Both fronts start at authoritative origin");
        check(
                BoardMotion.front(3, 0, .28) == 0 && BoardMotion.front(3, 5, .28) == 5,
                "Both fronts reach blocker endpoints at 280ms");
        check(BoardMotion.beamAlpha(.44) == 0, "Beams finish without lingering");
        check(
                SoundManager.cueLevel("tick") < SoundManager.cueLevel("capture")
                        && SoundManager.cueLevel("chat") < SoundManager.cueLevel("move"),
                "Cue hierarchy");
        var now = new java.util.concurrent.atomic.AtomicLong();
        BoardPanel panel = new BoardPanel(false, now::get, () -> false);
        panel.setSize(320, 320);
        panel.update(
                RuleExample.step(1), "RED", new BoardFeedback.Change(true, false, false, false));
        panel.update(
                RuleExample.step(2), "RED", new BoardFeedback.Change(false, true, true, false));
        BufferedImage timeline = new BufferedImage(320 * 4, 350, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = timeline.createGraphics();
        g.setColor(UITheme.BG_PAGE);
        g.fillRect(0, 0, timeline.getWidth(), timeline.getHeight());
        double[] times = {.0, .15, .30, .95};
        for (int i = 0; i < times.length; i++) {
            now.set((long) (times[i] * 1_000_000_000L));
            panel.advanceAnimation();
            Graphics2D frame = (Graphics2D) g.create(i * 320, 30, 320, 320);
            panel.paint(frame);
            frame.dispose();
            g.setColor(UITheme.TEXT_PRIMARY);
            g.drawString((int) (times[i] * 1000) + " ms", i * 320 + 18, 23);
        }
        g.dispose();
        check(!panel.animationRunning(), "Capture clock stops after 900ms");
        panel.update(
                RuleExample.step(2), "RED", new BoardFeedback.Change(false, false, false, false));
        check(!panel.animationRunning(), "Repeated snapshot cannot restart capture");
        BoardPanel reduced = new BoardPanel(false, now::get, () -> true);
        reduced.update(
                RuleExample.step(1), "RED", new BoardFeedback.Change(true, false, false, false));
        reduced.update(
                RuleExample.step(2), "RED", new BoardFeedback.Change(false, true, true, false));
        check(!reduced.animationRunning(), "Reduced motion applies final state without a timer");
        JPanel result =
                GameEndDialog.content(
                        "RED|Fixture", "RED", "Red", "Blue", 6, 0, () -> {}, () -> {});
        GameEndDialog.updateReadiness(
                result, "RED", new RematchStatus("fixture", false, true, false, ""));
        check(
                ((JLabel) result.getClientProperty("rematchStatus"))
                        .getText()
                        .contains("Đối thủ: sẵn sàng"),
                "Result panel shows actual peer readiness");
        try {
            Path output = Path.of(System.getProperty("review.output", ".work/review"));
            Files.createDirectories(output);
            ImageIO.write(timeline, "png", output.resolve("capture-timeline.png").toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        System.out.println(
                "PASS: capture origin/timelines, idle timer, reduced motion, cue hierarchy, live"
                    + " rematch panel");
    }

    private static void capture(JFrame frame, String name) throws Exception {
        frame.addNotify();
        frame.validate();
        Container content = frame.getContentPane();
        BufferedImage image =
                new BufferedImage(
                        content.getWidth(), content.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        content.printAll(g);
        g.dispose();
        Path output = Path.of(System.getProperty("review.output", ".work/review"));
        Files.createDirectories(output);
        ImageIO.write(image, "png", output.resolve(name + ".png").toFile());
        frame.dispose();
    }

    private static void screenshots() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        capture(new LoginFrame(), "login");
                        JFrame tutorialFrame = new JFrame();
                        tutorialFrame.setContentPane(HelpDialog.content(() -> {}));
                        tutorialFrame.pack();
                        capture(tutorialFrame, "tutorial");
                        JFrame settingsFrame = new JFrame();
                        settingsFrame.setContentPane(SettingsDialog.content(null, () -> {}));
                        settingsFrame.pack();
                        capture(settingsFrame, "settings");
                        JFrame resultFrame = new JFrame();
                        resultFrame.setContentPane(
                                GameEndDialog.content(
                                        "RED|Hết 50 lượt",
                                        "RED",
                                        "Minh Anh",
                                        "Tuấn Anh",
                                        24,
                                        19,
                                        () -> {},
                                        () -> {}));
                        resultFrame.pack();
                        capture(resultFrame, "result");
                        LobbyFrame lobby = new LobbyFrame("Minh Anh");
                        lobby.setPlayers(
                                List.of(
                                        new PlayerInfo("Minh Anh", 12, 5, false),
                                        new PlayerInfo("Tuấn Anh", 10, 4, false),
                                        new PlayerInfo("Ngọc Hà", 8, 3, true)));
                        // setPlayers normally marshals to EDT; drain is avoided here by capturing
                        // on next event.
                        SwingUtilities.invokeLater(
                                () -> {
                                    try {
                                        capture(lobby, "lobby");
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                        capture(
                                new RankingFrame(
                                        List.of(
                                                new PlayerInfo("Minh Anh", 12, 5, false),
                                                new PlayerInfo("Tuấn Anh", 10, 4, false)),
                                        "Minh Anh",
                                        () -> {}),
                                "ranking");
                        capture(
                                new HistoryFrame(
                                        "Minh Anh",
                                        List.of(
                                                new MatchHistory(
                                                        "23/10 08:30",
                                                        "Tuấn Anh",
                                                        "RED",
                                                        24,
                                                        19,
                                                        "WIN",
                                                        1))),
                                "history");
                        GameFrame game = new GameFrame("Minh Anh", "Tuấn Anh");
                        int[][] board = new int[9][9], areas = new int[9][9];
                        for (int c = 0; c < 9; c++) {
                            board[3][c] = 3;
                            areas[3][c] = 3;
                        }
                        board[2][2] = 1;
                        board[4][4] = 2;
                        board[5][3] = 2;
                        areas[5][3] = 4;
                        for (int row = 6; row < 8; row++) {
                            for (int col = 4; col < 9; col++) {
                                board[row][col] = 4;
                                areas[row][col] = 4;
                            }
                        }
                        board[6][6] = 2;
                        board[3][2] = 1;
                        game.showBoard(
                                new BoardState(
                                        board,
                                        areas,
                                        32,
                                        12,
                                        "RED",
                                        9,
                                        11,
                                        "preview",
                                        3,
                                        3,
                                        4,
                                        1,
                                        List.of(new CaptureRay(3, 0, 3, 8))),
                                "RED");
                        game.addChatMessage("Tuấn Anh", "Chào bạn, bắt đầu nhé!");
                        game.addChatMessage("Minh Anh", "Sẵn sàng.");
                        capture(game, "game");
                        GameFrame compact = new GameFrame("Minh Anh", "Tuấn Anh");
                        compact.setSize(960, 620);
                        compact.showBoard(
                                new BoardState(
                                        board, areas, 32, 4, "BLUE", 9, 11, "preview", 3, 3, 4, 1,
                                        List.of()),
                                "BLUE");
                        capture(compact, "game-compact");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        SwingUtilities.invokeAndWait(() -> {});
        SoundManager.shutdown();
        System.out.println("Screenshots saved: .work/review");
    }
}
