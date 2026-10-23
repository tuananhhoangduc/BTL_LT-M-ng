package client;

import common.BoardState;
import common.MessageType;
import common.NetworkMessage;
import common.RematchStatus;

final class GamePresenter {
    private final GameView view;
    private final ServerConnection connection;
    private final String username;
    private final String ownColor;
    private boolean departed;

    GamePresenter(GameView view, ServerConnection connection, String username, String ownColor) {
        this.view = view;
        this.connection = connection;
        this.username = username;
        this.ownColor = ownColor;
        view.onMove(this::move);
        view.onSurrender(() -> send(MessageType.SURRENDER));
        view.onLeave(
                () -> {
                    view.setActionsEnabled(false);
                    if (view.isSpectator()) {
                        returnToLobby();
                        send(MessageType.LEAVE_SPECTATE);
                    } else {
                        returnToLobby();
                        send(MessageType.LEAVE_GAME);
                    }
                });
        view.onChat(
                text -> {
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
        returnToLobby(null);
    }

    private void returnToLobby(String notice) {
        if (departed) return;
        departed = true;
        view.close();
        LobbyFrame lobby = new LobbyFrame(username);
        new LobbyPresenter(lobby, connection, username);
        if (notice != null) lobby.challengeStatus(notice, false);
        lobby.setVisible(true);
    }

    private void handleMessage(NetworkMessage message) {
        if (departed) return;
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
                if (message.payload instanceof BoardState finalBoard)
                    view.showBoard(finalBoard, ownColor);
                view.setActionsEnabled(false);
                if (view.isSpectator()) {
                    boolean returnToLobby = !view.askRematch(message.text);
                    if (departed) return;
                    if (returnToLobby) {
                        returnToLobby();
                        send(MessageType.LEAVE_SPECTATE);
                    }
                } else {
                    NetworkMessage reply = NetworkMessage.of(MessageType.REMATCH_REPLY);
                    reply.accepted = view.askRematch(message.text);
                    if (departed) return;
                    connection.send(reply);
                    if (reply.accepted) view.waitingForRematch();
                }
            }
            case REMATCH_STATUS -> {
                if (message.payload instanceof RematchStatus status) {
                    view.rematchStatus(status);
                    if (status.closed()) returnToLobby(status.reason());
                }
            }
            case MOVE_REJECTED -> view.moveRejected(message.text);
            case REMATCH_RESULT -> {
                if (message.text != null
                        && (message.text.startsWith("Van dau da ket thuc")
                                || message.text.startsWith("Ván đấu đã kết thúc"))) {
                    if (!view.isSpectator()) {
                        view.showMessage(message.text);
                        returnToLobby();
                    }
                }
            }
            case CHAT -> view.addChatMessage(message.username, message.text);
            case ERROR -> {
                if (!connection.isOpen()) {
                    view.setActionsEnabled(false);
                    view.connectionLost(
                            () -> {
                                departed = true;
                                view.close();
                                LoginFrame login = new LoginFrame();
                                new LoginPresenter(login);
                                login.setVisible(true);
                            });
                } else view.showMessage(message.text);
            }
            default -> {}
        }
    }
}
