package server;

import common.BoardState;
import common.CaptureRay;
import common.MessageType;
import common.NetworkMessage;
import common.RematchStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

final class GameRoom {
    private static final int SIZE = 9;
    private static final int TOTAL_TURNS = 50;
    private static final int TURN_SECONDS = 15;

    private final ServerState server;
    private final ClientSession red;
    private final ClientSession blue;
    private final List<ClientSession> spectators = new CopyOnWriteArrayList<>();
    private final int[][] board = new int[SIZE][SIZE];
    private final int[][] areas = new int[SIZE][SIZE];
    private String matchId = UUID.randomUUID().toString();
    private long moveSequence;
    private int moveRow = -1, moveColumn = -1, moveColor;
    private List<CaptureRay> captureRays = List.of();
    private ScheduledFuture<?> timerTask;
    private int currentColor = 1;
    private int turnsRemaining = TOTAL_TURNS;
    private int secondsRemaining = TURN_SECONDS;
    private boolean finished;
    private String winner;
    private String finishReason;
    private boolean redRematch;
    private boolean blueRematch;
    private boolean resultRecorded;

    GameRoom(ServerState server, ClientSession red, ClientSession blue) {
        this.server = server;
        this.red = red;
        this.blue = blue;
    }

    synchronized void start() {
        red.setRoom(this);
        blue.setRoom(this);
        sendStart(red, "RED", blue.username());
        sendStart(blue, "BLUE", red.username());
        broadcastBoard();
        scheduleTimer();
    }

    synchronized boolean addSpectator(ClientSession spectator) {
        if (finished) {
            return false;
        }
        if (!spectators.contains(spectator)) {
            spectators.add(spectator);
        }
        spectator.setRoom(this);

        NetworkMessage message = NetworkMessage.of(MessageType.SPECTATE_START);
        message.username = red.username();
        message.target = blue.username();
        message.color = "SPECTATOR";
        message.payload = snapshot();
        message.row = spectators.size();
        spectator.send(message);

        broadcastSystemChat(spectator.username() + " đã vào xem trận đấu.");
        broadcastSpectatorCount();
        server.broadcastOnline();
        return true;
    }

    synchronized void removeSpectator(ClientSession spectator) {
        if (spectators.remove(spectator)) {
            spectator.setRoom(null);
            broadcastSystemChat(spectator.username() + " đã rời phòng xem.");
            broadcastSpectatorCount();
            server.broadcastOnline();
        }
    }

    synchronized void move(ClientSession player, int row, int column) {
        if (finished || playerColor(player) != currentColor) {
            rejectMove(player, "Không phải lượt của bạn hoặc ván đã kết thúc");
            return;
        }
        List<CaptureRay> rays = new ArrayList<>();
        if (!GameRules.applyMove(board, areas, currentColor, row, column, rays)) {
            rejectMove(player, "Nước đi không hợp lệ. Chọn ô trống hoặc vùng của bạn.");
            return;
        }
        moveSequence++;
        moveRow = row;
        moveColumn = column;
        moveColor = currentColor;
        captureRays = List.copyOf(rays);
        turnsRemaining--;
        cancelTimer();
        if (turnsRemaining == 0) {
            finish(null, "Hết 50 lượt");
            return;
        }
        nextTurn();
    }

    synchronized void surrender(ClientSession player) {
        if (!finished) {
            finish(player == red ? "BLUE" : "RED", player.username() + " xin thua");
        }
    }

    private void rejectMove(ClientSession player, String reason) {
        NetworkMessage message = NetworkMessage.of(MessageType.MOVE_REJECTED);
        message.text = reason;
        player.send(message);
    }

    synchronized void leave(ClientSession player) {
        if (player == red || player == blue) {
            if (!finished) {
                finish(player == red ? "BLUE" : "RED", player.username() + " thoát giữa chừng");
            }
            closeAfterRematch(player.username() + " đã rời phòng đấu.");
        } else if (spectators.contains(player)) {
            removeSpectator(player);
        }
    }

    synchronized void onTimeout() {
        if (finished) {
            return;
        }
        secondsRemaining--;
        if (secondsRemaining <= 0) {
            turnsRemaining--;
            cancelTimer();
            if (turnsRemaining == 0) {
                finish(null, "Hết 50 lượt");
            } else {
                nextTurn();
            }
        } else {
            broadcastBoard();
        }
    }

    private void nextTurn() {
        currentColor = currentColor == 1 ? 2 : 1;
        secondsRemaining = TURN_SECONDS;
        broadcastBoard();
        scheduleTimer();
    }

    private void scheduleTimer() {
        timerTask =
                server.scheduler()
                        .scheduleAtFixedRate(
                                this::onTimeout, 1, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void cancelTimer() {
        if (timerTask != null) {
            timerTask.cancel(false);
            timerTask = null;
        }
    }

    private void finish(String winner, String reason) {
        finished = true;
        cancelTimer();
        int redScore = GameRules.count(areas, 3);
        int blueScore = GameRules.count(areas, 4);
        if (winner == null && redScore != blueScore) {
            winner = redScore > blueScore ? "RED" : "BLUE";
        }
        this.winner = winner;
        finishReason = reason;
        recordResultOnce();
        NetworkMessage message = NetworkMessage.of(MessageType.GAME_END);
        message.text = (winner == null ? "DRAW" : winner) + "|" + reason;
        message.payload = snapshot();
        message.row = spectators.size();
        broadcast(message);
    }

    synchronized void rematch(ClientSession player, boolean accepted) {
        if (!finished || (player != red && player != blue)) {
            return;
        }
        if (player == red) {
            redRematch = accepted;
        } else if (player == blue) {
            blueRematch = accepted;
        }
        if (player == red || player == blue) {
            NetworkMessage acknowledgement = NetworkMessage.of(MessageType.REMATCH_RESULT);
            acknowledgement.text =
                    accepted ? "Đã ghi nhận đồng ý đấu lại" : "Đã ghi nhận từ chối đấu lại";
            player.send(acknowledgement);
        }
        if (redRematch && blueRematch) {
            broadcastRematchStatus(false, "Hai người chơi đã sẵn sàng.");
            resetForRematch();
        } else if ((player == red && !accepted) || (player == blue && !accepted)) {
            closeAfterRematch(player.username() + " không đấu lại.");
        } else {
            broadcastRematchStatus(false, "Đang chờ người chơi còn lại.");
        }
    }

    private void resetForRematch() {
        matchId = UUID.randomUUID().toString();
        moveSequence = 0;
        moveRow = moveColumn = -1;
        moveColor = 0;
        captureRays = List.of();
        for (int row = 0; row < SIZE; row++) {
            java.util.Arrays.fill(board[row], 0);
            java.util.Arrays.fill(areas[row], 0);
        }
        currentColor = 1;
        turnsRemaining = TOTAL_TURNS;
        secondsRemaining = TURN_SECONDS;
        finished = false;
        winner = null;
        finishReason = null;
        redRematch = false;
        blueRematch = false;
        resultRecorded = false;
        sendStart(red, "RED", blue.username());
        sendStart(blue, "BLUE", red.username());
        for (ClientSession spec : spectators) {
            NetworkMessage specMsg = NetworkMessage.of(MessageType.SPECTATE_START);
            specMsg.username = red.username();
            specMsg.target = blue.username();
            specMsg.color = "SPECTATOR";
            specMsg.payload = snapshot();
            specMsg.row = spectators.size();
            spec.send(specMsg);
        }
        broadcastBoard();
        scheduleTimer();
    }

    private void broadcastRematchStatus(boolean closed, String reason) {
        NetworkMessage status = NetworkMessage.of(MessageType.REMATCH_STATUS);
        status.payload = new RematchStatus(matchId, redRematch, blueRematch, closed, reason);
        broadcast(status);
    }

    private void closeAfterRematch(String reason) {
        broadcastRematchStatus(true, reason);
        NetworkMessage message = NetworkMessage.of(MessageType.REMATCH_RESULT);
        message.text = reason;
        broadcast(message);
        for (ClientSession spec : spectators) {
            spec.setRoom(null);
        }
        spectators.clear();
        server.closeRoom(this);
    }

    private void recordResultOnce() {
        if (!resultRecorded) {
            resultRecorded = true;
            server.recordResult(this, winner);
        }
    }

    String winner() {
        return winner;
    }

    String finishReason() {
        return finishReason;
    }

    private void sendStart(ClientSession player, String color, String opponent) {
        NetworkMessage message = NetworkMessage.of(MessageType.GAME_START);
        message.color = color;
        message.target = opponent;
        message.payload = snapshot();
        message.row = spectators.size();
        player.send(message);
    }

    void broadcast(NetworkMessage message) {
        red.send(message);
        blue.send(message);
        for (ClientSession spec : spectators) {
            spec.send(message);
        }
    }

    private void broadcastBoard() {
        NetworkMessage message = NetworkMessage.of(MessageType.BOARD_UPDATE);
        message.payload = snapshot();
        message.row = spectators.size();
        broadcast(message);
    }

    void broadcastSystemChat(String text) {
        NetworkMessage msg = NetworkMessage.of(MessageType.CHAT);
        msg.username = "Hệ thống";
        msg.text = text;
        broadcast(msg);
    }

    void broadcastSpectatorCount() {
        NetworkMessage msg = NetworkMessage.of(MessageType.SPECTATOR_COUNT);
        msg.row = spectators.size();
        broadcast(msg);
    }

    private BoardState snapshot() {
        return new BoardState(
                board,
                areas,
                turnsRemaining,
                secondsRemaining,
                currentColor == 1 ? "RED" : "BLUE",
                GameRules.count(areas, 3),
                GameRules.count(areas, 4),
                matchId,
                moveSequence,
                moveRow,
                moveColumn,
                moveColor,
                captureRays);
    }

    private int playerColor(ClientSession player) {
        return player == red ? 1 : player == blue ? 2 : 0;
    }

    ClientSession red() {
        return red;
    }

    ClientSession blue() {
        return blue;
    }

    List<ClientSession> spectators() {
        return spectators;
    }

    int spectatorCount() {
        return spectators.size();
    }

    int redScore() {
        return GameRules.count(areas, 3);
    }

    int blueScore() {
        return GameRules.count(areas, 4);
    }
}
