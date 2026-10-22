package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class MatchDAO {
    void save(GameRoom room, String winner) throws SQLException {
        UserDAO.UserRecord red = new UserDAO().find(room.red().username());
        UserDAO.UserRecord blue = new UserDAO().find(room.blue().username());
        if (red == null || blue == null) {
            return;
        }

        try (Connection connection = DBConnection.open()) {
            connection.setAutoCommit(false);
            try {
                long matchId;
                String matchSql = "INSERT INTO matches(end_reason, started_at) VALUES (?, CURRENT_TIMESTAMP)";
                try (PreparedStatement statement = connection.prepareStatement(matchSql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, endReason(room.finishReason()));
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        keys.next();
                        matchId = keys.getLong(1);
                    }
                }

                String playerSql = "INSERT INTO match_players(match_id, user_id, color, captured_cells, result, points_earned) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(playerSql)) {
                    insertPlayer(statement, matchId, red.id(), "RED", room.redScore(), resultFor(winner, "RED"), pointsFor(winner, "RED"));
                    insertPlayer(statement, matchId, blue.id(), "BLUE", room.blueScore(), resultFor(winner, "BLUE"), pointsFor(winner, "BLUE"));
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void insertPlayer(PreparedStatement statement, long matchId, long userId, String color,
            int capturedCells, String result, double points) throws SQLException {
        statement.setLong(1, matchId);
        statement.setLong(2, userId);
        statement.setString(3, color);
        statement.setInt(4, capturedCells);
        statement.setString(5, result);
        statement.setDouble(6, points);
        statement.addBatch();
        statement.executeBatch();
    }

    private String resultFor(String winner, String color) {
        if (winner == null) {
            return "DRAW";
        }
        return winner.equals(color) ? "WIN" : "LOSS";
    }

    private double pointsFor(String winner, String color) {
        if (winner == null) {
            return 0.5;
        }
        return winner.equals(color) ? 1.0 : 0.0;
    }

    private String endReason(String reason) {
        if (reason != null && reason.contains("xin thua")) {
            return "SURRENDER";
        }
        if (reason != null && reason.contains("thoát")) {
            return "LEAVE_GAME";
        }
        return "TURNS_FINISHED";
    }
}