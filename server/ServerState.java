package server;

import common.MessageType;
import common.NetworkMessage;
import common.PlayerInfo;
import common.MatchHistory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.sql.SQLException;

final class ServerState {
    private final Map<String, Account> accounts = new HashMap<>();
    private final UserDAO userDAO = new UserDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> pendingChallenges = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    synchronized void handle(ClientSession session, NetworkMessage message) {
        switch (message.type) {
            case REGISTER -> register(session, message);
            case LOGIN -> login(session, message);
            case LOGOUT -> disconnect(session);
            case CHALLENGE -> challenge(session, message.target);
            case CHALLENGE_REPLY -> challengeReply(session, message);
            case MOVE -> move(session, message);
            case SURRENDER -> roomAction(session, true);
            case LEAVE_GAME -> roomAction(session, false);
            case REMATCH_REPLY -> rematch(session, message.accepted);
            case CHAT -> chat(session, message);
            case SPECTATE -> spectate(session, message.target);
            case LEAVE_SPECTATE -> leaveSpectate(session);
            case GET_ONLINE_LIST -> sendOnlineList(session);
            case GET_RANKING -> ranking(session);
            case GET_HISTORY -> history(session);
            default -> session.error("Thong diep khong duoc ho tro");
        }
    }

    synchronized void disconnect(ClientSession session) {
        if (session.room() != null) {
            session.room().leave(session);
        }
        if (session.username() != null) {
            sessions.remove(session.username());
            broadcastOnline();
        }
    }

    ScheduledExecutorService scheduler() {
        return scheduler;
    }

    synchronized void recordResult(GameRoom room, String winner) {
        Account red = accounts.get(room.red().username());
        Account blue = accounts.get(room.blue().username());
        try {
            if ("RED".equals(winner)) {
                userDAO.updateStats(red.username, 1, 1);
                red.points += 1;
                red.wins++;
            } else if ("BLUE".equals(winner)) {
                userDAO.updateStats(blue.username, 1, 1);
                blue.points += 1;
                blue.wins++;
            } else {
                userDAO.updateStats(red.username, 0.5, 0);
                userDAO.updateStats(blue.username, 0.5, 0);
                red.points += 0.5;
                blue.points += 0.5;
            }
        } catch (SQLException exception) {
            System.err.println("Khong the luu ket qua tran dau: " + exception.getMessage());
        }
        try {
            matchDAO.save(room, winner);
        } catch (SQLException exception) {
            System.err.println("Khong the luu lich su tran dau: " + exception.getMessage());
        }
        broadcastOnline();
    }

    synchronized void closeRoom(GameRoom room) {
        room.red().setRoom(null);
        room.blue().setRoom(null);
        for (ClientSession spec : room.spectators()) {
            spec.setRoom(null);
        }
        broadcastOnline();
    }

    private void register(ClientSession session, NetworkMessage message) {
        if (message.username == null || message.username.isBlank() || message.password == null
                || message.password.isBlank()) {
            result(session, MessageType.REGISTER_RESULT, false, "Ten dang nhap khong hop le hoac da ton tai");
            return;
        }
        try {
            if (userDAO.find(message.username) != null) {
                result(session, MessageType.REGISTER_RESULT, false, "Ten dang nhap da ton tai");
                return;
            }
            userDAO.insert(message.username, message.password);
            result(session, MessageType.REGISTER_RESULT, true, "Dang ky thanh cong");
        } catch (SQLException exception) {
            result(session, MessageType.REGISTER_RESULT, false, "Khong the ghi tai khoan vao MySQL");
        }
    }

    private void login(ClientSession session, NetworkMessage message) {
        if (message.username == null || message.password == null || sessions.containsKey(message.username)) {
            result(session, MessageType.LOGIN_RESULT, false, "Sai tai khoan, mat khau hoac tai khoan dang online");
            return;
        }
        try {
            UserDAO.UserRecord record = userDAO.find(message.username);
            if (record == null || !record.password().equals(message.password)) {
                result(session, MessageType.LOGIN_RESULT, false, "Sai tai khoan hoac mat khau");
                return;
            }
            accounts.put(message.username, new Account(record.username(), record.password(),
                    record.points(), record.wins()));
        } catch (SQLException exception) {
            result(session, MessageType.LOGIN_RESULT, false, "Khong the ket noi MySQL");
            return;
        }
        session.setUsername(message.username);
        sessions.put(message.username, session);
        result(session, MessageType.LOGIN_RESULT, true, "Dang nhap thanh cong");
        broadcastOnline();
    }

    private void challenge(ClientSession challenger, String target) {
        ClientSession opponent = sessions.get(target);
        if (opponent == null || opponent == challenger || opponent.room() != null) {
            result(challenger, MessageType.CHALLENGE_RESULT, false, "Doi thu khong online hoac dang ban");
            return;
        }
        pendingChallenges.put(target, challenger.username());
        NetworkMessage request = NetworkMessage.of(MessageType.CHALLENGE_REQUEST);
        request.username = challenger.username();
        opponent.send(request);
        result(challenger, MessageType.CHALLENGE_RESULT, true, "Da gui loi moi");
    }

    private void challengeReply(ClientSession target, NetworkMessage message) {
        String challengerName = pendingChallenges.remove(target.username());
        ClientSession challenger = sessions.get(challengerName);
        if (!message.accepted || challenger == null || challenger.room() != null || target.room() != null) {
            if (challenger != null) {
                result(challenger, MessageType.CHALLENGE_RESULT, false, "Loi moi bi tu choi");
            }
            return;
        }
        GameRoom room = new GameRoom(this, challenger, target);
        room.start();
        broadcastOnline();
    }

    private void move(ClientSession session, NetworkMessage message) {
        if (session.room() == null) {
            session.error("Ban chua o trong phong dau");
            return;
        }
        session.room().move(session, message.row, message.column);
    }

    private void roomAction(ClientSession session, boolean surrender) {
        if (session.room() == null) {
            return;
        }
        if (surrender) {
            session.room().surrender(session);
        } else {
            session.room().leave(session);
        }
    }

    private void rematch(ClientSession session, boolean accepted) {
        if (session.room() != null) {
            session.room().rematch(session, accepted);
        }
    }

    private void chat(ClientSession session, NetworkMessage message) {
        if (session.room() == null || message.text == null || message.text.isBlank()) {
            return;
        }
        NetworkMessage reply = NetworkMessage.of(MessageType.CHAT);
        boolean isSpectator = session != session.room().red() && session != session.room().blue();
        reply.username = isSpectator ? "[Khán giả] " + session.username() : session.username();
        reply.text = message.text;
        session.room().broadcast(reply);
    }

    private void spectate(ClientSession session, String target) {
        if (session.room() != null) {
            session.error("Bạn đang ở trong một phòng đấu");
            return;
        }
        if (target == null || target.isBlank()) {
            session.error("Chưa chọn người chơi để xem");
            return;
        }
        ClientSession targetSession = sessions.get(target);
        if (targetSession == null || targetSession.room() == null) {
            session.error("Người chơi hiện không ở trong trận đấu nào");
            return;
        }
        GameRoom room = targetSession.room();
        if (!room.addSpectator(session)) {
            session.error("Trận đấu đã kết thúc hoặc không thể tham gia");
        }
    }

    private void leaveSpectate(ClientSession session) {
        if (session.room() != null) {
            session.room().leave(session);
        }
        session.setRoom(null);
        sendOnlineList(session);
    }

    private void ranking(ClientSession session) {
        List<PlayerInfo> ranking;
        try {
            ranking = userDAO.ranking().stream()
                .map(player -> new PlayerInfo(player.username(), player.points(), player.wins(),
                    sessions.get(player.username()) != null
                        && sessions.get(player.username()).room() != null))
                .sorted(Comparator.comparingDouble(PlayerInfo::points).reversed())
                .toList();
        } catch (SQLException exception) {
            session.error("Khong the tai xep hang tu MySQL");
            return;
        }
        NetworkMessage message = NetworkMessage.of(MessageType.RANKING_DATA);
        message.payload = new ArrayList<>(ranking);
        session.send(message);
    }

    private void history(ClientSession session) {
        try {
            NetworkMessage message = NetworkMessage.of(MessageType.HISTORY_DATA);
            message.payload = new ArrayList<MatchHistory>(userDAO.history(session.username()));
            session.send(message);
        } catch (SQLException exception) {
            System.err.println("Khong the tai lich su tran dau: " + exception.getMessage());
            session.error("Khong the tai lich su tran dau tu MySQL: " + exception.getMessage());
        }
    }

    private List<PlayerInfo> buildOnlinePlayers() {
        return accounts.values().stream()
                .filter(account -> sessions.containsKey(account.username))
                .map(account -> {
                    ClientSession s = sessions.get(account.username);
                    boolean busy = s != null && s.room() != null;
                    return new PlayerInfo(account.username, account.points, account.wins, busy);
                })
                .toList();
    }

    synchronized void sendOnlineList(ClientSession session) {
        NetworkMessage message = NetworkMessage.of(MessageType.ONLINE_LIST);
        message.payload = new ArrayList<>(buildOnlinePlayers());
        session.send(message);
    }

    synchronized void broadcastOnline() {
        List<PlayerInfo> players = buildOnlinePlayers();
        for (ClientSession session : sessions.values()) {
            NetworkMessage message = NetworkMessage.of(MessageType.ONLINE_LIST);
            message.payload = new ArrayList<>(players);
            session.send(message);
        }
    }

    private void result(ClientSession session, MessageType type, boolean success, String text) {
        NetworkMessage message = NetworkMessage.of(type);
        message.accepted = success;
        message.text = text;
        session.send(message);
    }

    private static final class Account {
        private final String username;
        private final String password;
        private double points;
        private int wins;

        private Account(String username, String password) {
            this(username, password, 0, 0);
        }

        private Account(String username, String password, double points, int wins) {
            this.username = username;
            this.password = password;
            this.points = points;
            this.wins = wins;
        }
    }
}