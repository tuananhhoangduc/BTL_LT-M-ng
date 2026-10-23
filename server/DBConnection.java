package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class DBConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/color_mind_game"
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv().getOrDefault("COLOR_MATCH_DB_PASSWORD", "");

    private DBConnection() {
    }

    static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static void test() throws SQLException {
        try (Connection ignored = open()) {
        }
    }
}
