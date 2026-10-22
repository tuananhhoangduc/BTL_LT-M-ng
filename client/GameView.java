package client;

import common.BoardState;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

interface GameView {
    void onMove(BiConsumer<Integer, Integer> action);

    void onSurrender(Runnable action);

    void onLeave(Runnable action);

    void onChat(Consumer<String> action);

    void showBoard(BoardState state, String ownColor);

    boolean askRematch(String result);

    void showMessage(String message);

    void addChatMessage(String sender, String text);

    void setActionsEnabled(boolean enabled);

    void updateSpectatorCount(int count);

    boolean isSpectator();

    void close();
}
