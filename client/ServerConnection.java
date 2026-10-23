package client;

import common.MessageType;
import common.NetworkMessage;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

final class ServerConnection {
    private final Socket socket;
    private final ObjectOutputStream output;
    private volatile Consumer<NetworkMessage> listener;
    private volatile boolean closed;
    private final ThreadPoolExecutor writer =
            new ThreadPoolExecutor(
                    1,
                    1,
                    0,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(64),
                    r -> {
                        Thread t = new Thread(r, "server-writer");
                        t.setDaemon(true);
                        return t;
                    });

    private ServerConnection(Socket socket, Consumer<NetworkMessage> listener) throws IOException {
        this.socket = socket;
        this.listener = listener;
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
        socket.setSoTimeout(0);
        Thread reader = new Thread(() -> readLoop(input), "server-reader");
        reader.setDaemon(true);
        reader.start();
    }

    static ServerConnection connect(String host, int port, Consumer<NetworkMessage> listener)
            throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 4000);
            socket.setSoTimeout(4000);
            socket.setTcpNoDelay(true);
            return new ServerConnection(socket, listener);
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    boolean isOpen() {
        return !closed;
    }

    void send(NetworkMessage message) {
        if (closed) return;
        try {
            writer.execute(
                    () -> {
                        try {
                            output.writeObject(message);
                            output.flush();
                            output.reset();
                        } catch (IOException e) {
                            failed("Mất kết nối tới server.");
                        }
                    });
        } catch (RejectedExecutionException e) {
            failed("Kết nối đang quá tải hoặc đã đóng.");
        }
    }

    void setListener(Consumer<NetworkMessage> listener) {
        this.listener = listener;
    }

    void close() {
        closed = true;
        writer.shutdownNow();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void readLoop(ObjectInputStream input) {
        try (input) {
            while (!closed) {
                Object value = input.readObject();
                if (value instanceof NetworkMessage message) {
                    if (message.protocolVersion != NetworkMessage.PROTOCOL_VERSION) {
                        failed(
                                "Client/server khác phiên bản. Hãy cập nhật cả hai lên Color Match"
                                    + " Arena 2.2.");
                        return;
                    }
                    notifyMessage(message);
                }
            }
        } catch (InvalidClassException e) {
            failed("Client/server khác phiên bản. Hãy cập nhật cả hai lên Color Match Arena 2.2.");
        } catch (IOException | ClassNotFoundException e) {
            failed("Kết nối tới server đã đóng.");
        }
    }

    private synchronized void failed(String text) {
        if (closed) return;
        close();
        NetworkMessage message = NetworkMessage.of(MessageType.ERROR);
        message.text = text;
        notifyMessage(message);
    }

    private void notifyMessage(NetworkMessage message) {
        SwingUtilities.invokeLater(
                () -> {
                    Consumer<NetworkMessage> current = listener;
                    if (current != null) current.accept(message);
                });
    }
}
