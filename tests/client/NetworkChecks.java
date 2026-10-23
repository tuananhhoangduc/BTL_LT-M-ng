package client;

import common.*;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import javax.swing.SwingUtilities;

/** TCP and ObjectStream boundary test. Authentication/persistence are not mocked as verified. */
public final class NetworkChecks {
    private static void check(boolean condition, String text) {
        if (!condition) throw new AssertionError(text);
    }

    private static BoardState board(long sequence, int score) {
        return new BoardState(
                new int[9][9],
                new int[9][9],
                50 - (int) sequence,
                15,
                "BLUE",
                score,
                2,
                "tcp-match",
                sequence,
                4,
                3,
                1,
                List.of(new CaptureRay(4, 0, 4, 8)));
    }

    private static void write(ObjectOutputStream output, NetworkMessage message)
            throws IOException {
        output.writeObject(message);
        output.flush();
        output.reset();
    }

    private static final class View implements GameView {
        final CountDownLatch finished = new CountDownLatch(1);
        final CountDownLatch disconnected = new CountDownLatch(1);
        final CountDownLatch readiness = new CountDownLatch(1);
        boolean rejected;
        int score;
        boolean disabled, finalBeforeDialog;
        boolean waiting;

        public void onMove(BiConsumer<Integer, Integer> action) {}

        public void onSurrender(Runnable action) {}

        public void onLeave(Runnable action) {}

        public void onChat(Consumer<String> action) {}

        public void showBoard(BoardState b, String color) {
            check(SwingUtilities.isEventDispatchThread(), "Board update on EDT");
            check(b.captureRays.size() == 1, "Capture metadata survived serialization");
            score = b.redScore;
        }

        public boolean askRematch(String result) {
            finalBeforeDialog = score == 12 && disabled;
            return true;
        }

        public void showMessage(String text) {
            throw new AssertionError(text);
        }

        public void moveRejected(String reason) {
            check(SwingUtilities.isEventDispatchThread(), "Rejection delivered on EDT");
            rejected = "Occupied".equals(reason);
        }

        public void rematchStatus(RematchStatus state) {
            check(
                    state.redAccepted() && !state.blueAccepted() && !state.closed(),
                    "Typed readiness survives ObjectStream");
            readiness.countDown();
        }

        public void addChatMessage(String sender, String text) {}

        public void setActionsEnabled(boolean enabled) {
            disabled = !enabled;
        }

        public void updateSpectatorCount(int count) {}

        public boolean isSpectator() {
            return false;
        }

        public void close() {}

        public void waitingForRematch() {
            waiting = true;
            finished.countDown();
        }

        public void connectionLost(Runnable login) {
            check(disabled, "Board disabled before disconnect UI");
            check(SwingUtilities.isEventDispatchThread(), "Disconnect delivered on EDT");
            disconnected.countDown();
        }
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) -> {
                    error.printStackTrace();
                    System.exit(1);
                });
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try (ServerSocket server = new ServerSocket(0, 2, InetAddress.getLoopbackAddress())) {
            CountDownLatch ready = new CountDownLatch(1);
            CountDownLatch received = new CountDownLatch(1), closed = new CountDownLatch(1);
            Future<Boolean> remote =
                    worker.submit(
                            () -> {
                                try (Socket socket = server.accept()) {
                                    socket.setSoTimeout(5000);
                                    ObjectOutputStream output =
                                            new ObjectOutputStream(socket.getOutputStream());
                                    output.flush();
                                    ObjectInputStream input =
                                            new ObjectInputStream(socket.getInputStream());
                                    if (!ready.await(5, TimeUnit.SECONDS))
                                        throw new AssertionError("Presenter setup timeout");
                                    NetworkMessage update =
                                            NetworkMessage.of(MessageType.BOARD_UPDATE);
                                    update.payload = board(1, 3);
                                    write(output, update);
                                    NetworkMessage invalid =
                                            NetworkMessage.of(MessageType.MOVE_REJECTED);
                                    invalid.text = "Occupied";
                                    write(output, invalid);
                                    NetworkMessage end = NetworkMessage.of(MessageType.GAME_END);
                                    end.payload = board(2, 12);
                                    end.text = "RED|Hết 50 lượt";
                                    write(output, end);
                                    NetworkMessage reply = (NetworkMessage) input.readObject();
                                    NetworkMessage status =
                                            NetworkMessage.of(MessageType.REMATCH_STATUS);
                                    status.payload =
                                            new RematchStatus(
                                                    "tcp-match", true, false, false, "Waiting");
                                    write(output, status);
                                    received.countDown();
                                    if (!closed.await(5, TimeUnit.SECONDS))
                                        throw new AssertionError("Client close timeout");
                                    return reply.type == MessageType.REMATCH_REPLY
                                            && reply.accepted;
                                }
                            });
            ServerConnection connection =
                    ServerConnection.connect("127.0.0.1", server.getLocalPort(), m -> {});
            View view = new View();
            SwingUtilities.invokeAndWait(
                    () -> {
                        GamePresenter presenter = new GamePresenter(view, connection, "red", "RED");
                        NetworkMessage start = NetworkMessage.of(MessageType.GAME_START);
                        start.payload = board(0, 0);
                        presenter.start(start);
                    });
            ready.countDown();
            check(view.finished.await(5, TimeUnit.SECONDS), "Game-end handling timeout");
            check(view.finalBeforeDialog, "Final board and disabled actions precede result dialog");
            check(view.waiting, "Accepted rematch gets visible waiting state");
            check(view.rejected, "Move rejection routed without generic modal");
            check(view.readiness.await(5, TimeUnit.SECONDS), "Peer readiness delivered over TCP");
            check(received.await(5, TimeUnit.SECONDS), "Rematch reply crosses actual TCP stream");
            connection.close();
            closed.countDown();
            check(remote.get(5, TimeUnit.SECONDS), "Remote received correct rematch reply");
            SwingUtilities.invokeAndWait(() -> {});
            disconnectCheck(worker);
            versionCheck(worker);
            System.out.println(
                    "PASS: TCP ObjectStreams, capture serialization, EDT delivery, final score"
                            + " before dialog, rematch reply");
        } finally {
            worker.shutdownNow();
        }
    }

    private static void versionCheck(ExecutorService worker) throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            Future<?> peer =
                    worker.submit(
                            () -> {
                                try (Socket socket = server.accept()) {
                                    ObjectOutputStream output =
                                            new ObjectOutputStream(socket.getOutputStream());
                                    output.flush();
                                    new ObjectInputStream(socket.getInputStream());
                                    NetworkMessage outdated = NetworkMessage.of(MessageType.ERROR);
                                    outdated.protocolVersion = NetworkMessage.PROTOCOL_VERSION - 1;
                                    write(output, outdated);
                                } catch (Exception error) {
                                    throw new RuntimeException(error);
                                }
                            });
            CountDownLatch notice = new CountDownLatch(1);
            AtomicReference<String> reason = new AtomicReference<>();
            ServerConnection connection =
                    ServerConnection.connect(
                            "127.0.0.1",
                            server.getLocalPort(),
                            message -> {
                                check(
                                        SwingUtilities.isEventDispatchThread(),
                                        "Version error on EDT");
                                reason.set(message.text);
                                notice.countDown();
                            });
            check(notice.await(5, TimeUnit.SECONDS), "Version mismatch notice timeout");
            check(
                    !connection.isOpen() && reason.get().contains("khác phiên bản"),
                    "Old protocol closes with update instruction");
            peer.get(5, TimeUnit.SECONDS);
            System.out.println("PASS: protocol mismatch closes connection with version notice");
        }
    }

    private static void disconnectCheck(ExecutorService worker) throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            CountDownLatch ready = new CountDownLatch(1);
            Future<?> peer =
                    worker.submit(
                            () -> {
                                try (Socket socket = server.accept()) {
                                    ObjectOutputStream output =
                                            new ObjectOutputStream(socket.getOutputStream());
                                    output.flush();
                                    new ObjectInputStream(socket.getInputStream());
                                    if (!ready.await(5, TimeUnit.SECONDS))
                                        throw new AssertionError("Disconnect setup timeout");
                                } catch (Exception error) {
                                    throw new RuntimeException(error);
                                }
                            });
            ServerConnection connection =
                    ServerConnection.connect("127.0.0.1", server.getLocalPort(), message -> {});
            View view = new View();
            SwingUtilities.invokeAndWait(() -> new GamePresenter(view, connection, "red", "RED"));
            ready.countDown();
            check(
                    view.disconnected.await(5, TimeUnit.SECONDS),
                    "Real peer close reaches disconnect UI");
            check(!connection.isOpen(), "Closed socket is not reusable");
            peer.get(5, TimeUnit.SECONDS);
        }
    }
}
