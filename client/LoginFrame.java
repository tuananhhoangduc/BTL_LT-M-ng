package client;

import java.awt.*;

import javax.swing.*;

final class LoginFrame extends JFrame implements LoginView {
    private final JTextField server = new JTextField(ClientSettings.host());
    private final JTextField port = new JTextField("5000");
    private final JTextField user = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirm = new JPasswordField();
    private final JLabel error = new JLabel(" ");
    private final JLabel heading = new JLabel("Đăng nhập");
    private final JLabel confirmLabel = new JLabel("Nhập lại mật khẩu");
    private final JButton submit = UITheme.createGradientButton("Đăng nhập", 320, 42);
    private final JButton switchMode = UITheme.secondaryButton("Tạo tài khoản", this::toggle);
    private Runnable loginAction = () -> {}, registerAction = () -> {};
    private boolean register, busy;

    LoginFrame() {
        super(AppBrand.NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(UITheme.BG_PAGE);
        root.add(banner());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 18, 0);
        heading.setFont(UITheme.FONT_TITLE);
        form.add(heading, c);
        JLabel intro = new JLabel("Kết nối máy chủ để vào sảnh đấu.");
        intro.setForeground(UITheme.TEXT_MUTED);
        c.gridy++;
        form.add(intro, c);
        JPanel network = new JPanel(new BorderLayout(10, 0));
        network.setOpaque(false);
        UITheme.styleInput(server);
        UITheme.styleInput(port);
        server.getAccessibleContext().setAccessibleName("Địa chỉ máy chủ");
        port.getAccessibleContext().setAccessibleName("Cổng máy chủ");
        port.setPreferredSize(new Dimension(72, 36));
        network.add(server, BorderLayout.CENTER);
        network.add(port, BorderLayout.EAST);
        addField(form, c, "Máy chủ / cổng", network);
        addField(form, c, "Tên tài khoản", user);
        addField(form, c, "Mật khẩu", password);
        c.gridy++;
        c.insets = new Insets(0, 0, 4, 0);
        form.add(confirmLabel, c);
        UITheme.styleInput(confirm);
        confirm.setPreferredSize(new Dimension(300, 38));
        c.gridy++;
        c.insets = new Insets(0, 0, 12, 0);
        form.add(confirm, c);
        confirmLabel.setLabelFor(confirm);
        confirmLabel.setVisible(false);
        confirm.setVisible(false);
        error.setFont(UITheme.FONT_SMALL);
        error.setForeground(UITheme.LOSS_COLOR);
        error.setPreferredSize(new Dimension(320, 36));
        c.gridy++;
        form.add(error, c);
        c.gridy++;
        form.add(submit, c);
        c.gridy++;
        form.add(switchMode, c);
        JButton settings =
                UITheme.secondaryButton("Âm thanh & hiển thị", () -> SettingsDialog.show(this));
        c.gridy++;
        c.insets = new Insets(12, 0, 0, 0);
        form.add(settings, c);
        submit.addActionListener(
                e -> {
                    if (!busy) {
                        error.setText(" ");
                        SoundManager.playClick();
                        if (register) registerAction.run();
                        else loginAction.run();
                    }
                });
        getRootPane().setDefaultButton(submit);
        root.add(form);
        setContentPane(root);
        setSize(960, 660);
        setMinimumSize(new Dimension(860, 620));
        setLocationRelativeTo(null);
        SoundManager.scene("menu");
    }

    private void addField(JPanel panel, GridBagConstraints c, String name, JComponent field) {
        JLabel label = new JLabel(name);
        label.setFont(UITheme.FONT_SMALL_B);
        label.setLabelFor(field);
        c.gridy++;
        c.insets = new Insets(0, 0, 6, 0);
        panel.add(label, c);
        if (field instanceof JTextField text) {
            UITheme.styleInput(text);
            text.setPreferredSize(new Dimension(300, 38));
        }
        c.gridy++;
        c.insets = new Insets(0, 0, 14, 0);
        panel.add(field, c);
    }

    private JPanel banner() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                UITheme.applyQualityHints(g);
                int w = getWidth(), h = getHeight();
                ArenaArt.backdrop(g, w, h);
                g.setColor(UITheme.ACCENT);
                g.setFont(UITheme.FONT_SMALL_B);
                g.drawString("GIAO ĐẤU 1 ĐỐI 1", 36, 48);
                g.setColor(UITheme.TEXT_PRIMARY);
                g.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 28));
                g.drawString(AppBrand.NAME, 36, 112);
                g.setFont(UITheme.FONT_BODY);
                g.setColor(UITheme.TEXT_MUTED);
                g.drawString(AppBrand.TAGLINE, 36, 156);
                int tile = Math.min(48, (w - 96) / 6), bx = (w - tile * 6) / 2, by = 230;
                for (int r = 0; r < 6; r++)
                    for (int col = 0; col < 6; col++) {
                        int x = bx + col * tile, y = by + r * tile;
                        g.setColor(r == 2 ? UITheme.RED_AREA : UITheme.BG_CARD);
                        g.fillRoundRect(x + 2, y + 2, tile - 4, tile - 4, 6, 6);
                        SpriteCatalog.draw(
                                g,
                                r == 2 ? "tile.red" : "tile.neutral",
                                x + 2,
                                y + 2,
                                tile - 4,
                                tile - 4);
                        g.setColor(UITheme.BORDER);
                        g.drawRoundRect(x + 2, y + 2, tile - 4, tile - 4, 6, 6);
                    }
                for (int col = 1; col < 4; col++)
                    ArenaArt.crystal(g, bx + (col + .5) * tile, by + 2.5 * tile, tile * .6, 1);
                ArenaArt.crystal(g, bx + 4.5 * tile, by + 4.5 * tile, tile * .65, 2);
                ArenaArt.crystal(g, bx + 3.5 * tile, by + 5.5 * tile, tile * .65, 2);
                g.setColor(UITheme.TEXT_MUTED);
                g.setFont(UITheme.FONT_SMALL);
                g.drawString("9 × 9 ô     /     15 giây mỗi lượt", 36, h - 38);
                g.dispose();
            }
        };
    }

    private void toggle() {
        if (busy) return;
        register = !register;
        heading.setText(register ? "Tạo tài khoản" : "Đăng nhập");
        submit.setText(register ? "Đăng ký" : "Đăng nhập");
        switchMode.setText(register ? "Đã có tài khoản? Đăng nhập" : "Tạo tài khoản");
        confirmLabel.setVisible(register);
        confirm.setVisible(register);
        error.setText(" ");
        revalidate();
        repaint();
    }

    @Override
    public String host() {
        return server.getText().trim();
    }

    @Override
    public int port() {
        try {
            return Integer.parseInt(port.getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String username() {
        return user.getText().trim();
    }

    @Override
    public String password() {
        return new String(password.getPassword());
    }

    @Override
    public String confirmPassword() {
        return new String(confirm.getPassword());
    }

    @Override
    public void onLogin(Runnable action) {
        loginAction = action;
    }

    @Override
    public void onRegister(Runnable action) {
        registerAction = action;
    }

    @Override
    public void setBusy(boolean value) {
        busy = value;
        submit.setEnabled(!value);
        switchMode.setEnabled(!value);
        for (JTextField field : new JTextField[] {server, port, user, password, confirm})
            field.setEnabled(!value);
        submit.setText(value ? "Đang kết nối…" : register ? "Đăng ký" : "Đăng nhập");
    }

    @Override
    public void showError(String text) {
        error.setText(text == null ? " " : text);
        error.setToolTipText(text);
    }

    @Override
    public void showMessage(String text, boolean isError) {
        if (isError) {
            showError(text);
            return;
        }
        if (register) toggle();
        showError(text);
    }

    @Override
    public void openLobby(ServerConnection connection, String username) {
        ClientSettings.host(host());
        SoundManager.playGameStart();
        LobbyFrame lobby = new LobbyFrame(username);
        new LobbyPresenter(lobby, connection, username);
        lobby.setVisible(true);
        dispose();
    }
}
