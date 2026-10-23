package server;

import common.*;

import java.net.Socket;
import java.util.*;

/** Real room/rules/timer logic with in-memory sessions; never touches the user's database. */
public final class RoomChecks {
    private static final class Session extends ClientSession {
        final List<NetworkMessage> messages = new ArrayList<>();

        Session(ServerState state, String name) {
            super(state, (Socket) null);
            setUsername(name);
        }

        @Override
        synchronized void send(NetworkMessage message) {
            messages.add(message);
        }

        synchronized BoardState latest() {
            for (int i = messages.size() - 1; i >= 0; i--)
                if (messages.get(i).payload instanceof BoardState b) return b;
            throw new AssertionError("No board received");
        }

        synchronized long errors() {
            return messages.stream()
                    .filter(m -> m.type == MessageType.ERROR || m.type == MessageType.MOVE_REJECTED)
                    .count();
        }
    }

    private static void check(boolean condition, String text) {
        if (!condition) throw new AssertionError(text);
    }

    public static void main(String[] args) throws Exception {
        ServerState server = new ServerState();
        try {
            Session red = new Session(server, "red"),
                    blue = new Session(server, "blue"),
                    viewer = new Session(server, "viewer");
            GameRoom room = new GameRoom(server, red, blue);
            room.start();
            check(red.latest().matchId.equals(blue.latest().matchId), "Shared match ID");
            room.move(red, 4, 1);
            room.move(blue, 0, 0);
            room.move(red, 4, 2);
            room.move(blue, 0, 2);
            room.move(red, 4, 3);
            BoardState state = red.latest();
            check(
                    state.moveSequence == 5 && state.redScore == 9,
                    "Five moves and authoritative capture score");
            check(
                    state.captureRays.size() == 1 && state.captureRays.get(0).startColumn() == 0,
                    "Server records capture ray");
            check(
                    state.moveRow == 4 && state.moveColumn == 3 && state.moveColor == 1,
                    "Last move metadata");
            check(blue.latest().redScore == state.redScore, "Both players see same score");
            room.onTimeout();
            BoardState tick = blue.latest();
            check(
                    tick.moveSequence == state.moveSequence && tick.secondsRemaining == 14,
                    "Timer preserves move sequence");
            check(room.addSpectator(viewer), "Spectator can join running match");
            check(viewer.latest().moveSequence == 5, "Late viewer receives current snapshot");
            room.move(red, 8, 8);
            check(red.errors() == 1, "Out-of-turn move rejected");
            room.move(blue, 7, 7);
            room.move(red, 4, 4);
            check(
                    red.latest().cells[4][4] == 1 && red.latest().areas[4][4] == 3,
                    "Snapshot preserves territory under new stone");
            room.removeSpectator(viewer);
            check(viewer.room() == null, "Viewer detached cleanly");
            check(room.addSpectator(viewer), "Viewer rejoins before result");
            // Skip only persistence in this fixture; exercise real finish/rematch lifecycle.
            var recorded = GameRoom.class.getDeclaredField("resultRecorded");
            recorded.setAccessible(true);
            recorded.setBoolean(room, true);
            String oldMatch = red.latest().matchId;
            room.surrender(blue);
            room.rematch(viewer, true);
            check(
                    red.messages.stream().noneMatch(m -> m.type == MessageType.REMATCH_STATUS),
                    "Spectator cannot accept rematch");
            room.rematch(red, true);
            RematchStatus ready =
                    red.messages.stream()
                            .filter(m -> m.payload instanceof RematchStatus)
                            .map(m -> (RematchStatus) m.payload)
                            .reduce((a, b) -> b)
                            .orElseThrow();
            check(
                    ready.redAccepted() && !ready.blueAccepted() && !ready.closed(),
                    "First acceptance exposes actual peer readiness");
            check(
                    blue.messages.stream().anyMatch(m -> ready.equals(m.payload))
                            && viewer.messages.stream().anyMatch(m -> ready.equals(m.payload)),
                    "Readiness broadcast to both player and spectator");
            room.rematch(blue, true);
            check(
                    !red.latest().matchId.equals(oldMatch)
                            && red.latest().moveSequence == 0
                            && red.latest().redScore == 0
                            && red.latest().turnsRemaining == 50,
                    "Both accepts reset match ID, sequence, score and turn budget");
            recorded.setBoolean(room, true);
            room.surrender(red);
            room.rematch(blue, false);
            check(
                    red.messages.stream()
                            .anyMatch(
                                    m ->
                                            m.payload instanceof RematchStatus s
                                                    && s.closed()
                                                    && s.reason().contains("blue")),
                    "Decline closes room with reason");
            check(
                    red.room() == null && blue.room() == null && viewer.room() == null,
                    "Closed rematch detaches everyone");
            System.out.println(
                    "PASS: real room, two players, capture metadata, timer, late spectator, invalid"
                            + " turn, territory layer");
        } finally {
            server.scheduler().shutdownNow();
        }
    }
}
