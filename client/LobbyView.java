package client;

import common.MatchHistory;
import common.PlayerInfo;

import java.util.List;
import java.util.function.Consumer;

interface LobbyView {
    PlayerInfo selectedPlayer();

    void setPlayers(List<PlayerInfo> players);

    void onChallenge(Runnable action);

    void onSpectate(Consumer<String> action);

    void onRanking(Runnable action);

    void onHistory(Runnable action);

    void onLogout(Runnable action);

    boolean confirmChallenge(String challenger);

    void showRanking(List<PlayerInfo> ranking, Runnable historyAction);

    void showHistory(List<MatchHistory> history);

    void showMessage(String message, boolean error);

    void close();

    default void challengeStatus(String text, boolean waiting) {}

    default void connectionLost(Runnable login) {
        showMessage("Mất kết nối máy chủ.", true);
    }
}
