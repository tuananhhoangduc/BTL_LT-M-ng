package client;

import javax.swing.SwingUtilities;

public final class ClientApp {
    private ClientApp() {}

    public static void main(String[] args) {
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    SoundManager.shutdown();
                                    SpriteCatalog.shutdown();
                                },
                                "presentation-shutdown"));
        SpriteCatalog.preload();
        SwingUtilities.invokeLater(
                () -> {
                    UITheme.installDefaults();
                    SoundManager.init();
                    LoginFrame view = new LoginFrame();
                    new LoginPresenter(view);
                    view.setVisible(true);
                });
    }
}
