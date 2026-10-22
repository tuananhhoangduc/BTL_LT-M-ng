package client;

import common.BoardState;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.*;

final class GameFrame extends JFrame implements GameView {
    // ─── Board & Cells ────────────────────────────────────────────────────────
    private final BoardCell[][] cells = new BoardCell[9][9];

    // ─── Status & Timer ───────────────────────────────────────────────────────
    private final JLabel turnCounterLabel = new JLabel("Lượt: 0 / 50");
    private final JLabel timerLabel       = new JLabel("00:15");
    private final JLabel statusLabel      = new JLabel("Đang kết nối...");

    // Player 1 (You)
    private final JLabel p1Name  = new JLabel();
    private final JLabel p1Score = new JLabel("0 quân");
    private final UITheme.RoundedPanel p1Card;

    // Player 2 (Opponent)
    private final JLabel p2Name  = new JLabel();
    private final JLabel p2Score = new JLabel("0 quân");
    private final UITheme.RoundedPanel p2Card;

    // ─── Buttons ─────────────────────────────────────────────────────────────
    private final UITheme.SmoothButton surrenderButton;
    private final UITheme.SmoothButton leaveButton;

    // ─── Chat ────────────────────────────────────────────────────────────────
    private final JTextArea chatArea    = new JTextArea();
    private final JTextField chatInput  = new JTextField();
    private final UITheme.SmoothButton sendBtn;

    // ─── State ───────────────────────────────────────────────────────────────
    private final String username;
    private final String opponent;
    private final boolean isSpectator;
    private final String viewerUsername;
    private final JLabel spectatorBadge = new JLabel("0 người xem");
    private int lastRedScore  = 0;
    private int lastBlueScore = 0;
    private String ownColor = "RED";

    // ─── Callbacks ───────────────────────────────────────────────────────────
    private BiConsumer<Integer, Integer> moveAction      = (r, c) -> {};
    private Runnable                     surrenderAction = () -> {};
    private Runnable                     leaveAction     = () -> {};
    private Consumer<String>             chatAction      = t  -> {};

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    GameFrame(String username, String opponent) {
        this(username, opponent, false, username);
    }

    GameFrame(String username, String opponent, boolean isSpectator, String viewerUsername) {
        super(isSpectator
                ? "Color Match Arena - [Đang xem] " + username + " vs " + opponent
                : "Color Match Arena - Trận đấu [" + username + " vs " + opponent + "]");
        this.username = username;
        this.opponent = opponent;
        this.isSpectator = isSpectator;
        this.viewerUsername = viewerUsername;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1060, 680));
        setLocationRelativeTo(null);

        surrenderButton = new UITheme.SmoothButton("Xin thua", new Color(239, 68, 68), new Color(220, 38, 38), new Color(185, 28, 28), 12);
        leaveButton     = new UITheme.SmoothButton(isSpectator ? "Rời phòng xem" : "Thoát", new Color(100, 116, 139), new Color(71, 85, 105), new Color(51, 65, 85), 12);
        sendBtn         = new UITheme.SmoothButton("Gửi", UITheme.PRIMARY, UITheme.PRIMARY_DARK, UITheme.PRIMARY_DARK, 10);

        p1Card = new UITheme.RoundedPanel(18, Color.WHITE, UITheme.PIECE_RED, 2);
        p2Card = new UITheme.RoundedPanel(18, Color.WHITE, UITheme.PIECE_BLUE, 2);
        p1Card.enableShadow(true);
        p2Card.enableShadow(true);

        // Root container
        JPanel root = new JPanel(new BorderLayout(14, 12));
        root.setBackground(UITheme.BG_PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));

        root.add(createTopHeader(), BorderLayout.NORTH);

        JPanel mainLayout = new JPanel(new BorderLayout(16, 0));
        mainLayout.setOpaque(false);
        mainLayout.add(createLeftPanel(), BorderLayout.WEST);
        mainLayout.add(createCenterBoard(), BorderLayout.CENTER);
        mainLayout.add(createRightPanel(), BorderLayout.EAST);

        root.add(mainLayout, BorderLayout.CENTER);
        setContentPane(root);

        // Bind events
        chatInput.addActionListener(e -> sendChatMsg());
        sendBtn.addActionListener(e -> sendChatMsg());
        surrenderButton.addActionListener(e -> {
            SoundManager.playClick();
            surrenderAction.run();
        });
        leaveButton.addActionListener(e -> {
            SoundManager.playClick();
            leaveAction.run();
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  1. TOP BAR CAPSULE
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createTopHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JLabel brand = new JLabel(
                isSpectator ? "ĐANG XEM TRẬN ĐẤU" : "ĐẤU TRÍ SẮC MÀU",
                UITheme.createVectorIcon(isSpectator ? "eye" : "star", 20, isSpectator ? new Color(124, 58, 237) : UITheme.PRIMARY),
                SwingConstants.LEFT
        );
        brand.setIconTextGap(8);
        brand.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 18));
        brand.setForeground(isSpectator ? new Color(124, 58, 237) : UITheme.PRIMARY);
        bar.add(brand, BorderLayout.WEST);

        UITheme.RoundedPanel capsule = new UITheme.RoundedPanel(20, Color.WHITE, UITheme.BORDER, 1);
        capsule.enableShadow(true);
        capsule.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 8));

        statusLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        statusLabel.setForeground(UITheme.WIN_COLOR);

        turnCounterLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        turnCounterLabel.setForeground(UITheme.TEXT_PRIMARY);

        timerLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        timerLabel.setForeground(new Color(234, 88, 12));

        spectatorBadge.setIcon(UITheme.createVectorIcon("eye", 16, new Color(124, 58, 237)));
        spectatorBadge.setIconTextGap(6);
        spectatorBadge.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        spectatorBadge.setForeground(new Color(109, 40, 217));

        capsule.add(statusLabel);
        capsule.add(new JLabel("•"));
        capsule.add(turnCounterLabel);
        capsule.add(new JLabel("•"));
        capsule.add(timerLabel);
        capsule.add(new JLabel("•"));
        capsule.add(spectatorBadge);

        bar.add(capsule, BorderLayout.CENTER);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  2. LEFT PANEL (Player Cards CÂN XỨNG HOÀN TOÀN)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(230, 0));

        JPanel topBox = new JPanel(new GridLayout(3, 1, 0, 10));
        topBox.setOpaque(false);

        // Player 1
        p1Name.setText(username);
        setupPlayerCard(p1Card, p1Name, p1Score, isSpectator ? "Người chơi Đỏ (1P)" : "Bạn (1P)", UITheme.PIECE_RED, !isSpectator);
        topBox.add(p1Card);

        // VS Badge
        JPanel vsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        vsPanel.setOpaque(false);
        JLabel vs = new JLabel("VS", SwingConstants.CENTER);
        vs.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 18));
        vs.setForeground(new Color(148, 163, 184));
        vsPanel.add(vs);
        topBox.add(vsPanel);

        // Player 2
        p2Name.setText(opponent);
        setupPlayerCard(p2Card, p2Name, p2Score, isSpectator ? "Người chơi Xanh (2P)" : "Đối thủ (2P)", UITheme.PIECE_BLUE, false);
        topBox.add(p2Card);

        panel.add(topBox, BorderLayout.NORTH);

        // Actions
        if (isSpectator) {
            JPanel actions = new JPanel(new GridLayout(1, 1, 0, 0));
            actions.setOpaque(false);
            actions.setPreferredSize(new Dimension(230, 44));
            leaveButton.setPreferredSize(new Dimension(230, 44));
            actions.add(leaveButton);
            panel.add(actions, BorderLayout.SOUTH);
        } else {
            JPanel actions = new JPanel(new GridLayout(2, 1, 0, 10));
            actions.setOpaque(false);
            actions.setPreferredSize(new Dimension(230, 92));

            surrenderButton.setPreferredSize(new Dimension(100, 40));
            leaveButton.setPreferredSize(new Dimension(100, 40));

            actions.add(surrenderButton);
            actions.add(leaveButton);
            panel.add(actions, BorderLayout.SOUTH);
        }

        return panel;
    }

    private void setupPlayerCard(UITheme.RoundedPanel card, JLabel nameLbl, JLabel scoreLbl, String role, Color accent, boolean isYou) {
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel avatar = UITheme.createSquircleAvatar(isYou ? username : opponent, 52, accent, accent.darker());

        JPanel info = new JPanel(new GridLayout(3, 1, 0, 2));
        info.setOpaque(false);

        JLabel rLabel = new JLabel(role);
        rLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        rLabel.setForeground(UITheme.TEXT_MUTED);

        nameLbl.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 16));
        nameLbl.setForeground(accent);

        scoreLbl.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 16));
        scoreLbl.setForeground(UITheme.TEXT_PRIMARY);

        info.add(rLabel);
        info.add(nameLbl);
        info.add(scoreLbl);

        card.add(avatar, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  3. CENTER BOARD (Vuông chuẩn 540x540 ở chính giữa)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createCenterBoard() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        UITheme.RoundedPanel boardContainer = new UITheme.RoundedPanel(20, new Color(224, 231, 255), new Color(199, 210, 254), 2);
        boardContainer.enableShadow(true);
        boardContainer.setLayout(new GridLayout(9, 9, 3, 3));
        boardContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boardContainer.setPreferredSize(new Dimension(540, 540));
        boardContainer.setMinimumSize(new Dimension(540, 540));
        boardContainer.setMaximumSize(new Dimension(540, 540));

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                final int row = r;
                final int col = c;
                BoardCell cell = new BoardCell();
                cell.addActionListener(e -> {
                    SoundManager.playMove();
                    moveAction.accept(row, col);
                });
                cells[r][c] = cell;
                boardContainer.add(cell);
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(boardContainer, gbc);

        return wrapper;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  4. RIGHT PANEL (Quân cờ, Luật chơi & Chat)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 0));

        JPanel topBox = new JPanel(new GridLayout(2, 1, 0, 10));
        topBox.setOpaque(false);
        topBox.add(createLegendCard());
        topBox.add(createRulesCard());

        panel.add(topBox, BorderLayout.NORTH);
        panel.add(createChatCard(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLegendCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.BORDER, 1);
        card.enableShadow(true);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("Quân cờ & Lãnh thổ");
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 8, 4));
        grid.setOpaque(false);
        grid.add(legendItem("● Quân Đỏ", UITheme.PIECE_RED));
        grid.add(legendItem("▲ Quân Xanh", UITheme.PIECE_BLUE));
        grid.add(legendItem("■ Vùng Đỏ", new Color(254, 202, 202)));
        grid.add(legendItem("■ Vùng Xanh", new Color(186, 230, 253)));
        card.add(grid, BorderLayout.CENTER);

        return card;
    }

    private JLabel legendItem(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        l.setForeground(c.darker());
        return l;
    }

    private JPanel createRulesCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.BORDER, 1);
        card.enableShadow(true);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("Luật chơi nhanh");
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel rulesBox = new JPanel(new GridLayout(4, 1, 0, 2));
        rulesBox.setOpaque(false);
        String[] rules = {
                "• Mỗi lượt có 15s để đặt quân",
                "• Chiếm các ô liền kề quân của bạn",
                "• Đi vào ô màu nhận thêm điểm cộng",
                "• Hết 50 lượt, ai nhiều quân hơn thắng!"
        };
        for (String r : rules) {
            JLabel rl = new JLabel(r);
            rl.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
            rl.setForeground(UITheme.TEXT_MUTED);
            rulesBox.add(rl);
        }
        card.add(rulesBox, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChatCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel(16, Color.WHITE, UITheme.BORDER, 1);
        card.enableShadow(true);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Chat trong trận");
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        chatArea.setEditable(false);
        chatArea.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        chatArea.setForeground(UITheme.TEXT_PRIMARY);
        chatArea.setBackground(new Color(248, 250, 252));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        card.add(scroll, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setOpaque(false);

        UITheme.styleInput(chatInput);
        sendBtn.setPreferredSize(new Dimension(65, 34));

        inputRow.add(chatInput, BorderLayout.CENTER);
        inputRow.add(sendBtn, BorderLayout.EAST);
        card.add(inputRow, BorderLayout.SOUTH);

        return card;
    }

    private void sendChatMsg() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            SoundManager.playChat();
            chatAction.accept(msg);
            chatInput.setText("");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GameView Implementation
    // ═══════════════════════════════════════════════════════════════════════
    @Override public void onMove(BiConsumer<Integer, Integer> action) { this.moveAction = action; }
    @Override public void onSurrender(Runnable action) { this.surrenderAction = action; }
    @Override public void onLeave(Runnable action) { this.leaveAction = action; }
    @Override public void onChat(Consumer<String> action) { this.chatAction = action; }

    @Override
    public void showBoard(BoardState state, String ownColor) {
        this.ownColor = ownColor;
        this.lastRedScore = state.redScore;
        this.lastBlueScore = state.blueScore;

        turnCounterLabel.setText("Lượt: " + (50 - state.turnsRemaining + 1) + " / 50");
        timerLabel.setText(String.format("00:%02d", state.secondsRemaining));

        if (isSpectator) {
            boolean redTurn = "RED".equals(state.turnColor);
            statusLabel.setText(redTurn ? "● Lượt: " + username + " (Đỏ)" : "● Lượt: " + opponent + " (Xanh)");
            statusLabel.setForeground(redTurn ? UITheme.PIECE_RED : UITheme.PIECE_BLUE);

            p1Score.setText(state.redScore + " quân");
            p2Score.setText(state.blueScore + " quân");

            p1Card.setBorderColor(redTurn ? UITheme.PIECE_RED : UITheme.BORDER);
            p2Card.setBorderColor(!redTurn ? UITheme.PIECE_BLUE : UITheme.BORDER);
            p1Card.repaint();
            p2Card.repaint();

            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    int val = state.cells[r][c];
                    cells[r][c].setValue(val, state.areas[r][c]);
                    cells[r][c].setEnabled(false); // Khán giả chỉ xem, không bấm cờ
                }
            }
        } else {
            boolean myTurn = state.turnColor.equals(ownColor);
            statusLabel.setText(myTurn ? "● Đến lượt BẠN!" : "● Đợi đối thủ...");
            statusLabel.setForeground(myTurn ? UITheme.WIN_COLOR : UITheme.TEXT_MUTED);

            boolean iAmRed = "RED".equals(ownColor);
            p1Score.setText((iAmRed ? state.redScore : state.blueScore) + " quân");
            p2Score.setText((iAmRed ? state.blueScore : state.redScore) + " quân");

            p1Card.setBorderColor(myTurn ? UITheme.ACCENT : (iAmRed ? UITheme.PIECE_RED : UITheme.PIECE_BLUE));
            p1Card.repaint();

            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    int val = state.cells[r][c];
                    cells[r][c].setValue(val, state.areas[r][c]);
                    cells[r][c].setEnabled(myTurn && (val == 0 || val == ownArea()));
                }
            }
        }
    }

    private int ownArea() {
        return "RED".equals(ownColor) ? 3 : 4;
    }

    @Override
    public boolean askRematch(String resultText) {
        if (isSpectator) {
            GameEndDialog.show(this, resultText, "SPECTATOR", username, opponent,
                    lastRedScore, lastBlueScore, 0.0);
            return false;
        }
        return GameEndDialog.show(this, resultText, ownColor, username, opponent,
                lastRedScore, lastBlueScore, 0.0);
    }

    @Override
    public void updateSpectatorCount(int count) {
        SwingUtilities.invokeLater(() -> {
            spectatorBadge.setText(count + " người xem");
        });
    }

    @Override
    public boolean isSpectator() {
        return isSpectator;
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void addChatMessage(String sender, String text) {
        SwingUtilities.invokeLater(() -> {
            SoundManager.playChat();
            String time = LocalTime.now().format(TIME_FMT);
            chatArea.append("[" + time + "] " + sender + ": " + text + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    @Override
    public void setActionsEnabled(boolean enabled) {
        surrenderButton.setEnabled(enabled);
        leaveButton.setEnabled(enabled);
    }

    @Override
    public void close() { dispose(); }

    // ═══════════════════════════════════════════════════════════════════════
    //  Board Cell Component với POP-IN ANIMATION & ASSET LOADER
    // ═══════════════════════════════════════════════════════════════════════
    private static final class BoardCell extends JButton {
        private int value;
        private int area;
        private boolean isHovered = false;

        // Pop-in Scale Animation
        private float scaleProgress = 1.0f;
        private Timer animTimer;

        BoardCell() {
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) { isHovered = true; repaint(); } }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        void setValue(int value, int area) {
            boolean isNewPiece = (this.value == 0 && value > 0);
            this.value = value;
            this.area = area;

            if (isNewPiece) {
                startPopInAnimation();
            } else {
                scaleProgress = 1.0f;
                repaint();
            }
        }

        private void startPopInAnimation() {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            scaleProgress = 0.35f;
            animTimer = new Timer(15, e -> {
                scaleProgress += 0.12f;
                if (scaleProgress >= 1.0f) {
                    scaleProgress = 1.0f;
                    animTimer.stop();
                }
                repaint();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            UITheme.applyQualityHints(g);
            int w = getWidth();
            int h = getHeight();

            // Background
            Color bg = Color.WHITE;
            if (area == 3) bg = new Color(254, 226, 226);
            else if (area == 4) bg = new Color(224, 242, 254);

            if (isHovered && isEnabled() && value == 0) {
                bg = new Color(238, 242, 255);
            }

            g.setColor(bg);
            g.fillRoundRect(1, 1, w - 2, h - 2, 8, 8);

            // Border
            g.setColor(isHovered && isEnabled() ? UITheme.PRIMARY : new Color(203, 213, 225));
            g.setStroke(new BasicStroke(isHovered && isEnabled() ? 1.8f : 1.2f));
            g.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);

            // Vẽ quân cờ với Pop-In Scale Animation
            if (value == 1) {
                Image img = AssetLoader.getImage("piece_red.png");
                if (img != null) {
                    drawAssetPiece(g, img, w, h, scaleProgress);
                } else {
                    drawGlassMarble(g, w, h, new Color(244, 63, 94), new Color(190, 18, 60), scaleProgress);
                }
            } else if (value == 2) {
                Image img = AssetLoader.getImage("piece_blue.png");
                if (img != null) {
                    drawAssetPiece(g, img, w, h, scaleProgress);
                } else {
                    drawGlassTriangle(g, w, h, new Color(14, 165, 233), new Color(3, 105, 161), scaleProgress);
                }
            }

            g.dispose();
        }

        private void drawAssetPiece(Graphics2D g, Image img, int w, int h, float scale) {
            int baseSize = Math.min(w, h) * 72 / 100;
            int size = (int)(baseSize * scale);
            int x = (w - size) / 2;
            int y = (h - size) / 2;
            g.drawImage(img, x, y, size, size, null);
        }

        private void drawGlassMarble(Graphics2D g, int w, int h, Color light, Color dark, float scale) {
            int baseSize = Math.min(w, h) * 68 / 100;
            int size = Math.max(4, (int)(baseSize * scale));
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            // Shadow
            g.setColor(new Color(0, 0, 0, 40));
            g.fillOval(x + 2, y + 4, size - 4, size - 4);

            // 3D Sphere
            GradientPaint gp = new GradientPaint(x + size * 0.3f, y + size * 0.2f, light, x + size, y + size, dark);
            g.setPaint(gp);
            g.fillOval(x, y, size, size);

            // Specular reflection
            int specSize = size * 38 / 100;
            g.setColor(new Color(255, 255, 255, 160));
            g.fillOval(x + size * 22 / 100, y + size * 18 / 100, specSize, specSize / 2);
        }

        private void drawGlassTriangle(Graphics2D g, int w, int h, Color light, Color dark, float scale) {
            int baseHalf = Math.min(w, h) / 3;
            int half = Math.max(2, (int)(baseHalf * scale));

            Polygon tri = new Polygon();
            tri.addPoint(w / 2, h / 2 - half);
            tri.addPoint(w / 2 - half, h / 2 + half);
            tri.addPoint(w / 2 + half, h / 2 + half);

            Polygon shadow = new Polygon();
            shadow.addPoint(w / 2, h / 2 - half + 3);
            shadow.addPoint(w / 2 - half + 2, h / 2 + half + 3);
            shadow.addPoint(w / 2 + half + 2, h / 2 + half + 3);
            g.setColor(new Color(0, 0, 0, 40));
            g.fillPolygon(shadow);

            GradientPaint gp = new GradientPaint(w / 2f, h / 2f - half, light, w / 2f, h / 2f + half, dark);
            g.setPaint(gp);
            g.fillPolygon(tri);

            g.setColor(new Color(255, 255, 255, 140));
            g.setStroke(new BasicStroke(1.5f));
            g.drawPolygon(tri);
        }
    }
}
