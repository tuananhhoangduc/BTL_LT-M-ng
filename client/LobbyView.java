package client;

import common.PlayerInfo;
import common.MatchHistory;
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
}
