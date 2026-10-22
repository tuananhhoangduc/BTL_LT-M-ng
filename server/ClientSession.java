package server;

import common.MessageType;
import common.NetworkMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ClientSession implements Runnable {
    private final ServerState server;
    private final Socket socket;
    private ObjectOutputStream output;
    private String username;
    private volatile GameRoom room;

    ClientSession(ServerState server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (socket) {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Object value = input.readObject();
                if (value instanceof NetworkMessage message) {
                    server.handle(this, message);
                }
            }
        } catch (IOException | ClassNotFoundException ignored) {
            server.disconnect(this);
        }
    }

    synchronized void send(NetworkMessage message) {
        try {
            if (output != null) {
                output.writeObject(message);
                output.flush();
                output.reset();
            }
        } catch (IOException exception) {
            server.disconnect(this);
        }
    }

    String username() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    GameRoom room() {
        return room;
    }

    void setRoom(GameRoom room) {
        this.room = room;
    }

    void error(String text) {
        NetworkMessage message = NetworkMessage.of(MessageType.ERROR);
        message.text = text;
        send(message);
    }
}
