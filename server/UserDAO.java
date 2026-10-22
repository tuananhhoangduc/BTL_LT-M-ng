package server;

import common.PlayerInfo;
import common.MatchHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

final class UserDAO {
    UserRecord find(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, total_points, total_wins "
                + "FROM users WHERE username = ?";
        try (Connection connection = DBConnection.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new UserRecord(result.getLong("id"), result.getString("username"), result.getString("password_hash"),
                        result.getDouble("total_points"), result.getInt("total_wins"));
            }
        }
    }

    void insert(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash) VALUES (?, ?)";
        try (Connection connection = DBConnection.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
        }
    }

    void updateStats(String username, double points, int wins) throws SQLException {
        String sql = "UPDATE users SET total_points = total_points + ?, total_wins = total_wins + ? "
                + "WHERE username = ?";
        try (Connection connection = DBConnection.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, points);
            statement.setInt(2, wins);
            statement.setString(3, username);
            statement.executeUpdate();
        }
    }

    List<PlayerInfo> ranking() throws SQLException {
        List<PlayerInfo> players = new ArrayList<>();
        String sql = "SELECT username, total_points, total_wins FROM users "
                + "ORDER BY total_points DESC, total_wins DESC, username ASC";
        try (Connection connection = DBConnection.open();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                players.add(new PlayerInfo(result.getString("username"),
                        result.getDouble("total_points"), result.getInt("total_wins"), false));
            }
        }
        return players;
    }

    List<MatchHistory> history(String username) throws SQLException {
        List<MatchHistory> history = new ArrayList<>();
        String sql = "SELECT m.ended_at, opponent.username AS opponent, mp.color, "
                + "mp.captured_cells, opponent_player.captured_cells AS opponent_cells, "
                + "mp.result, mp.points_earned "
                + "FROM match_players mp "
                + "JOIN matches m ON m.id = mp.match_id "
                + "JOIN match_players opponent_player ON opponent_player.match_id = mp.match_id "
                + "AND opponent_player.user_id <> mp.user_id "
                + "JOIN users opponent ON opponent.id = opponent_player.user_id "
                + "JOIN users AS requesting_user ON requesting_user.id = mp.user_id "
                + "WHERE requesting_user.username = ? ORDER BY m.ended_at DESC";
        try (Connection connection = DBConnection.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    history.add(new MatchHistory(result.getString("ended_at"),
                            result.getString("opponent"), result.getString("color"),
                            result.getInt("captured_cells"), result.getInt("opponent_cells"),
                            result.getString("result"),
                            result.getDouble("points_earned")));
                }
            }
        }
        return history;
    }

    record UserRecord(long id, String username, String password, double points, int wins) {
    }
}