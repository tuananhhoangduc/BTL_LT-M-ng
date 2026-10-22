package client;

import common.NetworkMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

final class ServerConnection {
    private final Socket socket;
    private final ObjectOutputStream output;
    private volatile Consumer<NetworkMessage> listener;
    private volatile boolean closed;

    private ServerConnection(Socket socket) throws IOException {
        this.socket = socket;
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
        Thread reader = new Thread(() -> readLoop(input), "server-reader");
        reader.setDaemon(true);
        reader.start();
    }

    static ServerConnection connect(String host, int port, Consumer<NetworkMessage> listener) throws IOException {
        ServerConnection connection = new ServerConnection(new Socket(host, port));
        connection.listener = listener;
        return connection;
    }

    synchronized void send(NetworkMessage message) {
        try {
            output.writeObject(message);
            output.flush();
            output.reset();
        } catch (IOException exception) {
            if (!closed) {
                notifyMessage(error("Mất kết nối tới server"));
            }
        }
    }

    void setListener(Consumer<NetworkMessage> listener) {
        this.listener = listener;
    }

    void close() {
        closed = true;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void readLoop(ObjectInputStream input) {
        try (input) {
            while (true) {
                Object value = input.readObject();
                if (value instanceof NetworkMessage message) {
                    notifyMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException ignored) {
            if (!closed) {
                notifyMessage(error("Kết nối tới server đã đóng"));
            }
        }
    }

    private void notifyMessage(NetworkMessage message) {
        Consumer<NetworkMessage> currentListener = listener;
        if (currentListener != null) {
            currentListener.accept(message);
        }
    }

    private static NetworkMessage error(String text) {
        NetworkMessage message = NetworkMessage.of(common.MessageType.ERROR);
        message.text = text;
        return message;
    }
}