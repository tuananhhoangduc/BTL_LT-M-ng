package client;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

final class LoginFrame extends JFrame implements LoginView {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rightCardsPanel = new JPanel(cardLayout);

    // Login Fields
    private final JTextField serverField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("5000");
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel loginErrorLabel = new JLabel(" ");

    // Register Fields
    private final JTextField regUsernameField = new JTextField();
    private final JPasswordField regPasswordField = new JPasswordField();
    private final JPasswordField regConfirmPasswordField = new JPasswordField();
    private final JLabel regErrorLabel = new JLabel(" ");

    private Runnable loginAction = () -> {};
    private Runnable registerAction = () -> {};
    private String currentCard = "LOGIN";

    LoginFrame() {
        super("Đấu Trí Sắc Màu - Color Match Arena");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 580);
        setMinimumSize(new Dimension(860, 540)); // Khóa kích thước tối thiểu đảm bảo không bị vỡ layout
        setLocationRelativeTo(null);

        // Root chia đôi cân đối 50 - 50 bằng GridLayout(1, 2)
        JPanel root = new JPanel(new GridLayout(1, 2, 0, 0));
        root.setBackground(Color.WHITE);

        root.add(createLeftGameBanner());
        root.add(createRightFormPanel());

        setContentPane(root);
        setupKeyListeners();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CỘT TRÁI: BANNER ĐỒ HOẠ GAME NGHỆ THUẬT
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createLeftGameBanner() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                int w = getWidth();
                int h = getHeight();

                // 1. Sky Gradient Background
                GradientPaint sky = new GradientPaint(
                        0, 0, new Color(56, 189, 248),
                        0, h, new Color(186, 230, 253)
                );
                g2.setPaint(sky);
                g2.fillRect(0, 0, w, h);

                // 2. Mây bồng bềnh
                g2.setColor(new Color(255, 255, 255, 130));
                g2.fillOval(w - 180, 30, 140, 60);
                g2.fillOval(w - 220, 50, 100, 50);
                g2.fillOval(30, 80, 130, 55);
                g2.fillOval(70, 60, 90, 50);

                // 3. Logo Game 3D
                String title1 = "ĐẤU TRÍ";
                String title2 = "SẮC MÀU";
                int startY = 110;

                g2.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 38));
                FontMetrics fm = g2.getFontMetrics();

                // Đổ bóng chữ
                g2.setColor(new Color(30, 58, 138, 120));
                g2.drawString(title1, (w - fm.stringWidth(title1)) / 2 + 3, startY + 3);
                g2.drawString(title2, (w - fm.stringWidth(title2)) / 2 + 3, startY + 46 + 3);

                // Chữ chính
                g2.setColor(new Color(254, 240, 138));
                g2.drawString(title1, (w - fm.stringWidth(title1)) / 2, startY);
                g2.setColor(new Color(249, 115, 22));
                g2.drawString(title2, (w - fm.stringWidth(title2)) / 2, startY + 46);

                // Subtitle
                g2.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
                String sub = "COLOR MATCH ARENA • PVP ONLINE";
                FontMetrics fmSub = g2.getFontMetrics();
                g2.setColor(new Color(30, 64, 175));
                g2.drawString(sub, (w - fmSub.stringWidth(sub)) / 2, startY + 72);

                // 4. Các khối màu 3D xếp lớp
                int boardX = (w - 210) / 2;
                int boardY = startY + 115;
                Color[] blockColors = {
                        new Color(239, 68, 68), new Color(59, 130, 246), new Color(16, 185, 129),
                        new Color(245, 158, 11), new Color(168, 85, 247), new Color(236, 72, 153)
                };
                int idx = 0;
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 4; col++) {
                        int bx = boardX + col * 50 - row * 10;
                        int by = boardY + row * 34;
                        g2.setColor(new Color(0, 0, 0, 35));
                        g2.fillRoundRect(bx + 2, by + 4, 44, 28, 8, 8);
                        g2.setColor(blockColors[(idx++) % blockColors.length]);
                        g2.fillRoundRect(bx, by, 44, 28, 8, 8);
                        // Highlight
                        g2.setColor(new Color(255, 255, 255, 100));
                        g2.fillRoundRect(bx + 2, by + 2, 40, 10, 6, 6);
                    }
                }

                // 5. Hai linh vật đáng yêu
                drawMascot(g2, boardX - 25, boardY + 22, 54, new Color(239, 68, 68));
                drawMascot(g2, boardX + 180, boardY + 12, 54, new Color(59, 130, 246));

                // 6. Slogan dưới đáy
                int bannerH = 46;
                g2.setColor(new Color(2, 132, 199));
                g2.fillRect(0, h - bannerH, w, bannerH);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
                String slogan = "Chiến thuật  +  Tư duy  +  Chiếm lĩnh!";
                FontMetrics fmSlogan = g2.getFontMetrics();
                g2.drawString(slogan, (w - fmSlogan.stringWidth(slogan)) / 2, h - 17);

                g2.dispose();
            }

            private void drawMascot(Graphics2D g, int x, int y, int size, Color c) {
                // Shadow
                g.setColor(new Color(0, 0, 0, 35));
                g.fillOval(x + 2, y + 4, size, size);
                // Body
                GradientPaint gp = new GradientPaint(x, y, c.brighter(), x + size, y + size, c.darker());
                g.setPaint(gp);
                g.fillOval(x, y, size, size);
                // Eyes
                g.setColor(Color.WHITE);
                g.fillOval(x + size / 4, y + size / 3, 11, 14);
                g.fillOval(x + size * 5 / 8 - 4, y + size / 3, 11, 14);
                g.setColor(Color.BLACK);
                g.fillOval(x + size / 4 + 3, y + size / 3 + 4, 6, 6);
                g.fillOval(x + size * 5 / 8 - 1, y + size / 3 + 4, 6, 6);
                // Smile
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawArc(x + size / 3, y + size / 2, size / 3, size / 4, 190, 160);
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CỘT PHẢI: FORM ĐĂNG NHẬP / ĐĂNG KÝ (Dùng GridBagLayout CÂN XỨNG 100%)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createRightFormPanel() {
        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setBackground(Color.WHITE);

        rightCardsPanel.setOpaque(false);
        rightCardsPanel.add(createLoginCard(), "LOGIN");
        rightCardsPanel.add(createRegisterCard(), "REGISTER");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rightWrapper.add(rightCardsPanel, gbc);

        return rightWrapper;
    }

    private JPanel createLoginCard() {
        // Cố định bố cục bằng GridBagLayout: TẤT CẢ CÁC COMPONENT ĐƯỢC CĂN THẲNG TRỤC TUYỆT ĐỐI
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 44, 20, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Title
        JLabel title = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        title.setForeground(UITheme.PRIMARY);
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(title, gbc);

        // Subtitle
        JLabel sub = new JLabel("Chào mừng bạn quay trở lại võ đài", SwingConstants.CENTER);
        sub.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(sub, gbc);

        // Label: Tên tài khoản
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(createFieldLabel("Tên tài khoản"), gbc);

        // Ô nhập Tên tài khoản
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        UITheme.styleInput(usernameField);
        usernameField.setPreferredSize(new Dimension(320, 38));
        card.add(usernameField, gbc);

        // Label: Mật khẩu
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(createFieldLabel("Mật khẩu"), gbc);

        // Ô nhập Mật khẩu
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        UITheme.styleInput(passwordField);
        passwordField.setPreferredSize(new Dimension(320, 38));
        card.add(passwordField, gbc);

        // Link Đăng ký tài khoản
        JPanel rowLink = new JPanel(new BorderLayout());
        rowLink.setOpaque(false);
        JLabel regLink = new JLabel("Đăng ký tài khoản mới", SwingConstants.RIGHT);
        regLink.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        regLink.setForeground(UITheme.ACCENT);
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                SoundManager.playClick();
                showRegisterView();
            }
        });
        rowLink.add(regLink, BorderLayout.EAST);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(rowLink, gbc);

        // Thông báo lỗi
        loginErrorLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        loginErrorLabel.setForeground(UITheme.LOSS_COLOR);
        loginErrorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(loginErrorLabel, gbc);

        // Nút Đăng nhập
        JButton btnLogin = UITheme.createGradientButton("Đăng nhập", 320, 42);
        btnLogin.addActionListener(e -> {
            SoundManager.playClick();
            loginErrorLabel.setText(" ");
            loginAction.run();
        });
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(btnLogin, gbc);

        // Khung Host & Port kết nối server (căn giữa hoàn hảo)
        JPanel netCard = new UITheme.RoundedPanel(12, new Color(248, 250, 252), UITheme.BORDER, 1);
        netCard.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 8));

        JLabel hostLbl = new JLabel("Host:");
        hostLbl.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        hostLbl.setForeground(UITheme.TEXT_MUTED);
        serverField.setPreferredSize(new Dimension(110, 28));
        UITheme.styleInput(serverField);

        JLabel portLbl = new JLabel("Cổng:");
        portLbl.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        portLbl.setForeground(UITheme.TEXT_MUTED);
        portField.setPreferredSize(new Dimension(55, 28));
        UITheme.styleInput(portField);

        netCard.add(hostLbl);
        netCard.add(serverField);
        netCard.add(portLbl);
        netCard.add(portField);

        JLabel netTitle = new JLabel("Kết nối máy chủ:", SwingConstants.CENTER);
        netTitle.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 11));
        netTitle.setForeground(UITheme.TEXT_MUTED);

        JPanel netBox = new JPanel(new BorderLayout(0, 4));
        netBox.setOpaque(false);
        netBox.add(netTitle, BorderLayout.NORTH);
        netBox.add(netCard, BorderLayout.CENTER);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(netBox, gbc);

        return card;
    }

    private JPanel createRegisterCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 44, 20, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("TẠO TÀI KHOẢN", SwingConstants.CENTER);
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        title.setForeground(UITheme.PRIMARY);
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(title, gbc);

        JLabel sub = new JLabel("Tham gia cộng đồng Đấu Trí Sắc Màu", SwingConstants.CENTER);
        sub.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(sub, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(createFieldLabel("Tên tài khoản"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        UITheme.styleInput(regUsernameField);
        regUsernameField.setPreferredSize(new Dimension(320, 36));
        card.add(regUsernameField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(createFieldLabel("Mật khẩu"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        UITheme.styleInput(regPasswordField);
        regPasswordField.setPreferredSize(new Dimension(320, 36));
        card.add(regPasswordField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(createFieldLabel("Xác nhận mật khẩu"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        UITheme.styleInput(regConfirmPasswordField);
        regConfirmPasswordField.setPreferredSize(new Dimension(320, 36));
        card.add(regConfirmPasswordField, gbc);

        regErrorLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        regErrorLabel.setForeground(UITheme.LOSS_COLOR);
        regErrorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(regErrorLabel, gbc);

        JButton btnReg = UITheme.createGradientButton("Đăng ký ngay", 320, 42);
        btnReg.addActionListener(e -> {
            SoundManager.playClick();
            regErrorLabel.setText(" ");
            registerAction.run();
        });
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(btnReg, gbc);

        JLabel backLink = new JLabel("Đã có tài khoản? Đăng nhập", SwingConstants.CENTER);
        backLink.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        backLink.setForeground(UITheme.ACCENT);
        backLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                SoundManager.playClick();
                showLoginView();
            }
        });
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(backLink, gbc);

        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private void showRegisterView() {
        currentCard = "REGISTER";
        cardLayout.show(rightCardsPanel, "REGISTER");
        regUsernameField.setText(usernameField.getText().trim());
        regPasswordField.setText("");
        regConfirmPasswordField.setText("");
        regErrorLabel.setText(" ");
        regUsernameField.requestFocusInWindow();
    }

    private void showLoginView() {
        currentCard = "LOGIN";
        cardLayout.show(rightCardsPanel, "LOGIN");
        loginErrorLabel.setText(" ");
        usernameField.requestFocusInWindow();
    }

    private void setupKeyListeners() {
        KeyAdapter lk = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) loginAction.run(); }
        };
        usernameField.addKeyListener(lk);
        passwordField.addKeyListener(lk);

        KeyAdapter rk = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) registerAction.run(); }
        };
        regUsernameField.addKeyListener(rk);
        regPasswordField.addKeyListener(rk);
        regConfirmPasswordField.addKeyListener(rk);
    }

    // ── LoginView Implementation ─────────────────────────────────────────
    @Override public String host() { String h = serverField.getText().trim(); return h.isBlank() ? "127.0.0.1" : h; }
    @Override public int port() { try { return Integer.parseInt(portField.getText().trim()); } catch (Exception e) { return 5000; } }
    @Override public String username() { return "REGISTER".equals(currentCard) ? regUsernameField.getText().trim() : usernameField.getText().trim(); }
    @Override public String password() { return "REGISTER".equals(currentCard) ? new String(regPasswordField.getPassword()) : new String(passwordField.getPassword()); }
    @Override public String confirmPassword() { return new String(regConfirmPasswordField.getPassword()); }
    @Override public void onLogin(Runnable action) { this.loginAction = action; }
    @Override public void onRegister(Runnable action) { this.registerAction = action; }

    @Override
    public void showError(String message) {
        if ("REGISTER".equals(currentCard)) regErrorLabel.setText(message != null ? message : " ");
        else loginErrorLabel.setText(message != null ? message : " ");
    }

    @Override
    public void showMessage(String message, boolean error) {
        if (error) {
            showError(message);
        } else {
            JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if ("REGISTER".equals(currentCard)) {
                usernameField.setText(regUsernameField.getText().trim());
                passwordField.setText(new String(regPasswordField.getPassword()));
                showLoginView();
            }
        }
    }

    @Override
    public void openLobby(ServerConnection connection, String username) {
        SoundManager.playGameStart();
        LobbyFrame lobby = new LobbyFrame(username);
        new LobbyPresenter(lobby, connection, username);
        lobby.setVisible(true);
        dispose();
    }
}
