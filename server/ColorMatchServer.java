package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public final class ColorMatchServer {
    public static final int PORT = 5000;

    private ColorMatchServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? PORT : Integer.parseInt(args[0]);
        try {
            DBConnection.test();
        } catch (SQLException exception) {
            System.err.println("Không kết nối được MySQL color_mind_game: " + exception.getMessage());
            System.err.println("Kiểm tra XAMPP MySQL, cổng 3306 và MySQL Connector/J.");
            return;
        }
        ServerState state = new ServerState();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Color Match Arena Server đang chạy tại cổng " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                Thread clientThread = new Thread(new ClientSession(state, socket), "client-handler");
                clientThread.start();
            }
        }
    }
}
