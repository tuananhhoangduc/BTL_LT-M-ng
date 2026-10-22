package client;

import common.MessageType;
import common.NetworkMessage;
import java.io.IOException;

final class LoginPresenter {
    private final LoginView view;
    private ServerConnection connection;
    private String currentHost = "";
    private int currentPort = 0;

    LoginPresenter(LoginView view) {
        this.view = view;
        view.onLogin(() -> authenticate(MessageType.LOGIN));
        view.onRegister(this::handleRegister);
    }

    private boolean ensureConnection() {
        String host = view.host();
        if (host.isBlank()) {
            host = "127.0.0.1";
        }
        int port = view.port();
        if (port <= 0) {
            port = 5000;
        }
        if (connection != null && host.equalsIgnoreCase(currentHost) && port == currentPort) {
            return true;
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
        try {
            connection = ServerConnection.connect(host, port, this::handleMessage);
            currentHost = host;
            currentPort = port;
            return true;
        } catch (IOException exception) {
            view.showError("Không thể kết nối đến máy chủ " + host + ":" + port);
            return false;
        }
    }

    private void authenticate(MessageType type) {
        if (view.username().isBlank() || view.password().isBlank()) {
            view.showError("Sai tên đăng nhập hoặc mật khẩu!");
            return;
        }
        if (!ensureConnection()) {
            return;
        }
        NetworkMessage message = NetworkMessage.of(type);
        message.username = view.username();
        message.password = view.password();
        connection.send(message);
    }

    private void handleRegister() {
        String user = view.username();
        String pass = view.password();
        String confirm = view.confirmPassword();

        if (user.isBlank()) {
            view.showError("Vui lòng nhập tên đăng nhập!");
            return;
        }
        if (pass.isBlank()) {
            view.showError("Vui lòng nhập mật khẩu!");
            return;
        }
        if (!pass.equals(confirm)) {
            view.showError("Mật khẩu nhập lại không khớp!");
            return;
        }
        if (!ensureConnection()) {
            return;
        }
        NetworkMessage message = NetworkMessage.of(MessageType.REGISTER);
        message.username = user;
        message.password = pass;
        connection.send(message);
    }

    private void handleMessage(NetworkMessage message) {
        if (message.type == MessageType.REGISTER_RESULT) {
            if (message.accepted) {
                view.showMessage("Đăng ký tài khoản thành công!", false);
            } else {
                view.showError(message.text != null ? message.text : "Tên đăng nhập đã tồn tại!");
            }
        } else if (message.type == MessageType.LOGIN_RESULT) {
            if (message.accepted) {
                view.openLobby(connection, view.username());
            } else {
                view.showError(message.text != null ? message.text : "Sai tên đăng nhập hoặc mật khẩu!");
            }
        } else if (message.type == MessageType.ERROR) {
            view.showError(message.text);
        }
    }
}