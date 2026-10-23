package client;

interface LoginView {
    default void setBusy(boolean busy) {}

    String host();

    int port();

    String username();

    String password();

    String confirmPassword();

    void onLogin(Runnable action);

    void onRegister(Runnable action);

    void showError(String message);

    void showMessage(String message, boolean error);

    void openLobby(ServerConnection connection, String username);
}
