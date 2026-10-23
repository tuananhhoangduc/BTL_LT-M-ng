package client;

import common.MessageType;
import common.NetworkMessage;

import javax.swing.*;

final class LoginPresenter {
    private final LoginView view;
    private ServerConnection connection;
    private String currentHost = "";
    private int currentPort;
    private boolean busy;
    private String requestUsername;
    private final Timer timeout;

    LoginPresenter(LoginView view) {
        this.view = view;
        timeout =
                new Timer(
                        10000,
                        e -> {
                            if (connection != null) connection.close();
                            connection = null;
                            done();
                            view.showError("Máy chủ không phản hồi. Hãy thử kết nối lại.");
                        });
        timeout.setRepeats(false);
        view.onLogin(() -> authenticate(MessageType.LOGIN));
        view.onRegister(() -> authenticate(MessageType.REGISTER));
    }

    private void done() {
        timeout.stop();
        busy = false;
        view.setBusy(false);
    }

    private void authenticate(MessageType type) {
        if (busy) return;
        String host = view.host(), user = view.username(), pass = view.password();
        int port = view.port();
        if (host.isBlank() || port < 1 || port > 65535) {
            view.showError("Nhập địa chỉ máy chủ và cổng từ 1 đến 65535.");
            return;
        }
        if (user.isBlank() || pass.isBlank()) {
            view.showError("Nhập tên tài khoản và mật khẩu.");
            return;
        }
        if (type == MessageType.REGISTER && !pass.equals(view.confirmPassword())) {
            view.showError("Mật khẩu nhập lại không khớp.");
            return;
        }
        busy = true;
        view.setBusy(true);
        requestUsername = user;
        NetworkMessage message = NetworkMessage.of(type);
        message.username = user;
        message.password = pass;
        if (connection != null
                && connection.isOpen()
                && host.equals(currentHost)
                && port == currentPort) {
            timeout.restart();
            connection.send(message);
            return;
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
        new SwingWorker<ServerConnection, Void>() {
            @Override
            protected ServerConnection doInBackground() throws Exception {
                return ServerConnection.connect(host, port, LoginPresenter.this::handleMessage);
            }

            @Override
            protected void done() {
                try {
                    connection = get();
                    currentHost = host;
                    currentPort = port;
                    timeout.restart();
                    connection.send(message);
                } catch (Exception e) {
                    LoginPresenter.this.done();
                    view.showError(
                            "Không kết nối được " + host + ":" + port + ". Kiểm tra IP và server.");
                }
            }
        }.execute();
    }

    private void handleMessage(NetworkMessage message) {
        if (message.type == MessageType.REGISTER_RESULT) {
            done();
            view.showMessage(
                    message.accepted
                            ? "Tạo tài khoản thành công. Bạn có thể đăng nhập."
                            : message.text,
                    !message.accepted);
        } else if (message.type == MessageType.LOGIN_RESULT) {
            done();
            if (message.accepted) view.openLobby(connection, requestUsername);
            else view.showError(message.text);
        } else if (message.type == MessageType.ERROR) {
            done();
            view.showError(message.text);
        }
    }
}
