package client;

import common.BoardState;
import common.MessageType;
import common.NetworkMessage;

final class GamePresenter {
    private final GameView view;
    private final ServerConnection connection;
    private final String username;
    private final String ownColor;

    GamePresenter(GameView view, ServerConnection connection, String username, String ownColor) {
        this.view = view;
        this.connection = connection;
        this.username = username;
        this.ownColor = ownColor;
        view.onMove(this::move);
        view.onSurrender(() -> send(MessageType.SURRENDER));
        view.onLeave(() -> {
            view.setActionsEnabled(false);
            if (view.isSpectator()) {
                returnToLobby();
                send(MessageType.LEAVE_SPECTATE);
            } else {
                returnToLobby();
                send(MessageType.LEAVE_GAME);
            }
        });
        view.onChat(text -> {
            if (text != null && !text.isBlank()) {
                NetworkMessage msg = NetworkMessage.of(MessageType.CHAT);
                msg.text = text;
                connection.send(msg);
            }
        });
        connection.setListener(this::handleMessage);
    }

    void start(NetworkMessage gameStart) {
        if (gameStart.row >= 0) {
            view.updateSpectatorCount(gameStart.row);
        }
        view.showBoard((BoardState) gameStart.payload, ownColor);
    }

    private void move(int row, int column) {
        if (view.isSpectator()) {
            return;
        }
        NetworkMessage message = NetworkMessage.of(MessageType.MOVE);
        message.row = row;
        message.column = column;
        connection.send(message);
    }

    private void send(MessageType type) {
        connection.send(NetworkMessage.of(type));
    }

    private void returnToLobby() {
        view.close();
        LobbyFrame lobby = new LobbyFrame(username);
        new LobbyPresenter(lobby, connection, username);
        lobby.setVisible(true);
    }

    private void handleMessage(NetworkMessage message) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            switch (message.type) {
                case GAME_START, SPECTATE_START, BOARD_UPDATE -> {
                    view.setActionsEnabled(true);
                    if (message.row >= 0) {
                        view.updateSpectatorCount(message.row);
                    }
                    view.showBoard((BoardState) message.payload, ownColor);
                }
                case SPECTATOR_COUNT -> {
                    view.updateSpectatorCount(message.row);
                }
                case GAME_END -> {
                    view.setActionsEnabled(false);
                    if (view.isSpectator()) {
                        boolean returnToLobby = !view.askRematch(message.text);
                        if (returnToLobby) {
                            returnToLobby();
                            send(MessageType.LEAVE_SPECTATE);
                        }
                    } else {
                        NetworkMessage reply = NetworkMessage.of(MessageType.REMATCH_REPLY);
                        reply.accepted = view.askRematch(message.text);
                        connection.send(reply);
                    }
                }
                case REMATCH_RESULT -> {
                    if (message.text != null && (message.text.startsWith("Van dau da ket thuc") || message.text.startsWith("Ván đấu đã kết thúc"))) {
                        if (!view.isSpectator()) {
                            view.showMessage(message.text);
                            returnToLobby();
                        }
                    }
                }
                case CHAT -> view.addChatMessage(message.username, message.text);
                case ERROR -> view.showMessage(message.text);
                default -> {
                }
            }
        });
    }
}
