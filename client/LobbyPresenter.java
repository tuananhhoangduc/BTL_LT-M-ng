package client;

import common.MatchHistory;
import common.MessageType;
import common.NetworkMessage;
import common.PlayerInfo;

import java.util.List;

final class LobbyPresenter {
    private final LobbyView view;
    private final ServerConnection connection;
    private final String username;
    private boolean waitingForChallenge;
    private final javax.swing.Timer challengeTimeout;

    LobbyPresenter(LobbyView view, ServerConnection connection, String username) {
        this.view = view;
        this.connection = connection;
        this.username = username;
        challengeTimeout =
                new javax.swing.Timer(
                        45000,
                        e -> {
                            waitingForChallenge = false;
                            view.challengeStatus(
                                    "Chưa nhận phản hồi. Bạn có thể thử lại; lời mời cũ vẫn có thể"
                                        + " được trả lời.",
                                    false);
                        });
        challengeTimeout.setRepeats(false);
        view.onChallenge(this::challengeSelected);
        view.onSpectate(this::spectatePlayer);
        view.onRanking(() -> connection.send(NetworkMessage.of(MessageType.GET_RANKING)));
        view.onHistory(() -> connection.send(NetworkMessage.of(MessageType.GET_HISTORY)));
        view.onLogout(this::logout);
        connection.setListener(this::handleMessage);
        connection.send(NetworkMessage.of(MessageType.GET_ONLINE_LIST));
    }

    private void challengeSelected() {
        if (waitingForChallenge || !connection.isOpen()) return;
        PlayerInfo selected = view.selectedPlayer();
        if (selected == null || selected.username().equals(username)) {
            return;
        }
        NetworkMessage message = NetworkMessage.of(MessageType.CHALLENGE);
        message.target = selected.username();
        waitingForChallenge = true;
        view.challengeStatus("Đang gửi lời mời tới " + selected.username() + "…", true);
        challengeTimeout.restart();
        connection.send(message);
    }

    private void spectatePlayer(String target) {
        if (target == null || target.isBlank() || target.equals(username)) {
            return;
        }
        NetworkMessage message = NetworkMessage.of(MessageType.SPECTATE);
        message.target = target;
        connection.send(message);
    }

    private void logout() {
        challengeTimeout.stop();
        connection.send(NetworkMessage.of(MessageType.LOGOUT));
        connection.close();
        view.close();
        LoginFrame login = new LoginFrame();
        new LoginPresenter(login);
        login.setVisible(true);
    }

    private void handleMessage(NetworkMessage message) {
        switch (message.type) {
            case ONLINE_LIST -> view.setPlayers(castPlayers(message.payload));
            case CHALLENGE_REQUEST -> replyChallenge(message.username);
            case CHALLENGE_RESULT -> {
                if (message.accepted)
                    view.challengeStatus("Đã gửi lời mời. Đang chờ đối thủ…", true);
                else {
                    challengeTimeout.stop();
                    waitingForChallenge = false;
                    view.challengeStatus(message.text, false);
                }
            }
            case RANKING_DATA ->
                    view.showRanking(
                            castPlayers(message.payload),
                            () -> connection.send(NetworkMessage.of(MessageType.GET_HISTORY)));
            case HISTORY_DATA -> view.showHistory(castHistory(message.payload));
            case GAME_START -> startGame(message);
            case SPECTATE_START -> startSpectate(message);
            case ERROR -> {
                if (!connection.isOpen()) {
                    challengeTimeout.stop();
                    view.connectionLost(this::logout);
                } else view.showMessage(message.text, true);
            }
            default -> {}
        }
    }

    private void replyChallenge(String challenger) {
        NetworkMessage reply = NetworkMessage.of(MessageType.CHALLENGE_REPLY);
        reply.username = challenger;
        reply.accepted = view.confirmChallenge(challenger);
        connection.send(reply);
    }

    private void startGame(NetworkMessage message) {
        challengeTimeout.stop();
        GameFrame gameView = new GameFrame(username, message.target);
        new GamePresenter(gameView, connection, username, message.color).start(message);
        gameView.setVisible(true);
        view.close();
    }

    private void startSpectate(NetworkMessage message) {
        challengeTimeout.stop();
        GameFrame gameView = new GameFrame(message.username, message.target, true, username);
        new GamePresenter(gameView, connection, username, "SPECTATOR").start(message);
        gameView.setVisible(true);
        view.close();
    }

    @SuppressWarnings("unchecked")
    private List<PlayerInfo> castPlayers(Object payload) {
        return (List<PlayerInfo>) payload;
    }

    @SuppressWarnings("unchecked")
    private List<MatchHistory> castHistory(Object payload) {
        return (List<MatchHistory>) payload;
    }
}
