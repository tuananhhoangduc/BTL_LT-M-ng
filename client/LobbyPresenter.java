package client;

import common.MessageType;
import common.NetworkMessage;
import common.PlayerInfo;
import common.MatchHistory;
import java.util.List;

final class LobbyPresenter {
    private final LobbyView view;
    private final ServerConnection connection;
    private final String username;

    LobbyPresenter(LobbyView view, ServerConnection connection, String username) {
        this.view = view;
        this.connection = connection;
        this.username = username;
        view.onChallenge(this::challengeSelected);
        view.onSpectate(this::spectatePlayer);
        view.onRanking(() -> connection.send(NetworkMessage.of(MessageType.GET_RANKING)));
        view.onHistory(() -> connection.send(NetworkMessage.of(MessageType.GET_HISTORY)));
        view.onLogout(this::logout);
        connection.setListener(this::handleMessage);
        connection.send(NetworkMessage.of(MessageType.GET_ONLINE_LIST));
    }

    private void challengeSelected() {
        PlayerInfo selected = view.selectedPlayer();
        if (selected == null || selected.username().equals(username)) {
            return;
        }
        NetworkMessage message = NetworkMessage.of(MessageType.CHALLENGE);
        message.target = selected.username();
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
        connection.send(NetworkMessage.of(MessageType.LOGOUT));
        connection.close();
        view.close();
    }

    private void handleMessage(NetworkMessage message) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            switch (message.type) {
                case ONLINE_LIST -> view.setPlayers(castPlayers(message.payload));
                case CHALLENGE_REQUEST -> replyChallenge(message.username);
                case CHALLENGE_RESULT -> view.showMessage(message.text, !message.accepted);
                case RANKING_DATA -> view.showRanking(castPlayers(message.payload),
                        () -> connection.send(NetworkMessage.of(MessageType.GET_HISTORY)));
                case HISTORY_DATA -> view.showHistory(castHistory(message.payload));
                case GAME_START -> startGame(message);
                case SPECTATE_START -> startSpectate(message);
                case ERROR -> view.showMessage(message.text, true);
                default -> {
                }
            }
        });
    }

    private void replyChallenge(String challenger) {
        NetworkMessage reply = NetworkMessage.of(MessageType.CHALLENGE_REPLY);
        reply.username = challenger;
        reply.accepted = view.confirmChallenge(challenger);
        connection.send(reply);
    }

    private void startGame(NetworkMessage message) {
        GameFrame gameView = new GameFrame(username, message.target);
        new GamePresenter(gameView, connection, username, message.color).start(message);
        gameView.setVisible(true);
        view.close();
    }

    private void startSpectate(NetworkMessage message) {
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
