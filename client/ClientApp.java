package client;

import javax.swing.SwingUtilities;

public final class ClientApp {
    private ClientApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame view = new LoginFrame();
            new LoginPresenter(view);
            view.setVisible(true);
        });
    }
}