package client;

import common.BoardState;
import common.RematchStatus;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.*;

import javax.swing.*;

final class GameFrame extends JFrame implements GameView {
    private final String username, opponent;
    private final boolean spectator;
    private final BoardPanel board;
    private final BoardFeedback feedback = new BoardFeedback();
    private final JLabel status = new JLabel("Đang kết nối…");
    private final JLabel timer = new JLabel("15s");
    private final JProgressBar timeBar = new JProgressBar(0, 15);
    private final JLabel identity = new JLabel(" ");
    private final JLabel territoryDelta = new JLabel(" ", SwingConstants.CENTER);
    private final Timer deltaTimer = new Timer(1400, e -> territoryDelta.setText(" "));
    private final JLabel turns = new JLabel("Lượt 1 / 50");
    private final JLabel watchers = new JLabel("0 người xem");
    private final JLabel redName = new JLabel(), blueName = new JLabel();
    private final JLabel redScore = new JLabel("0"), blueScore = new JLabel("0");
    private final UITheme.RoundedPanel redCard, blueCard;
    private final JTextArea chat = new JTextArea();
    private final JTextField input = new JTextField();
    private final JButton surrender, leave;
    private Runnable surrenderAction = () -> {}, leaveAction = () -> {};
    private Consumer<String> chatAction = text -> {};
    private int lastRedScore, lastBlueScore;
    private String ownColor = "RED";
    private boolean ended;
    private Runnable loginAfterDisconnect;
    private String matchId = "";
    private RematchStatus rematchState;
    private JPanel resultPanel;
    private long scoreAccentStarted;
    private int scoreAccentColor;
    private final Timer scoreAccent = new Timer(16, e -> updateScoreAccent());
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    GameFrame(String username, String opponent) {
        this(username, opponent, false, username);
    }

    GameFrame(String username, String opponent, boolean spectator, String viewer) {
        super(
                AppBrand.NAME
                        + " — "
                        + (spectator ? "Đang xem: " : "")
                        + username
                        + " / "
                        + opponent);
        this.username = username;
        this.opponent = opponent;
        this.spectator = spectator;
        board = new BoardPanel(spectator);
        redCard = playerCard(redName, redScore, 1);
        blueCard = playerCard(blueName, blueScore, 2);
        surrender =
                UITheme.secondaryButton(
                        "Đầu hàng",
                        () -> {
                            if (!ended
                                    && ClientDialog.ask(
                                            this,
                                            "Đầu hàng",
                                            "Bạn muốn kết thúc ván đấu và nhận thua?",
                                            "Đầu hàng",
                                            "Tiếp tục chơi")) surrenderAction.run();
                        });
        leave =
                UITheme.secondaryButton(
                        spectator ? "Rời phòng xem" : "Rời trận",
                        () -> {
                            if (loginAfterDisconnect != null) {
                                loginAfterDisconnect.run();
                                return;
                            }
                            if (spectator
                                    || ended
                                    || ClientDialog.ask(
                                            this,
                                            "Rời trận",
                                            "Rời trận đang chơi sẽ được tính là thua.",
                                            "Rời trận",
                                            "Ở lại")) leaveAction.run();
                        });
        JPanel root = SpriteCatalog.arenaPanel(new BorderLayout(20, 16));
        root.setBackground(UITheme.BG_PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        JLabel brand = new JLabel(AppBrand.WORDMARK);
        brand.setFont(UITheme.FONT_HEADER);
        header.add(brand, BorderLayout.WEST);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        tools.setOpaque(false);
        watchers.setForeground(UITheme.TEXT_MUTED);
        tools.add(watchers);
        tools.add(UITheme.secondaryButton("Cách chơi", () -> HelpDialog.show(this)));
        tools.add(UITheme.secondaryButton("Cài đặt", () -> SettingsDialog.show(this)));
        header.add(tools, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);
        JPanel main = new JPanel(new BorderLayout(16, 0));
        main.setOpaque(false);
        JPanel players = new JPanel();
        players.setOpaque(false);
        players.setLayout(new BoxLayout(players, BoxLayout.Y_AXIS));
        players.setPreferredSize(new Dimension(182, 0));
        players.add(redCard);
        players.add(Box.createVerticalStrut(12));
        players.add(blueCard);
        players.add(Box.createVerticalGlue());
        identity.setText(spectator ? "Chế độ khán giả" : " ");
        identity.setForeground(UITheme.TEXT_MUTED);
        identity.setOpaque(true);
        identity.setBackground(UITheme.BG_CARD);
        identity.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        players.add(identity);
        players.add(Box.createVerticalStrut(12));
        surrender.setAlignmentX(Component.LEFT_ALIGNMENT);
        leave.setAlignmentX(Component.LEFT_ALIGNMENT);
        surrender.setMaximumSize(new Dimension(182, 38));
        leave.setMaximumSize(new Dimension(182, 38));
        if (!spectator) {
            players.add(surrender);
            players.add(Box.createVerticalStrut(8));
        }
        players.add(leave);
        main.add(players, BorderLayout.WEST);
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        JPanel matchBar = new JPanel(new BorderLayout(12, 0));
        matchBar.setOpaque(false);
        status.setFont(UITheme.FONT_BODY_BOLD);
        timer.setFont(new Font("Consolas", Font.BOLD, 26));
        turns.setForeground(UITheme.TEXT_MUTED);
        matchBar.add(status, BorderLayout.WEST);
        matchBar.add(turns, BorderLayout.CENTER);
        matchBar.add(timer, BorderLayout.EAST);
        turns.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel turnHeader = new JPanel(new BorderLayout(0, 8));
        turnHeader.setOpaque(false);
        turnHeader.add(matchBar, BorderLayout.NORTH);
        timeBar.setValue(15);
        timeBar.setPreferredSize(new Dimension(100, 4));
        timeBar.setBorderPainted(false);
        timeBar.setBackground(UITheme.BG_ELEVATED);
        turnHeader.add(timeBar, BorderLayout.SOUTH);
        center.add(turnHeader, BorderLayout.NORTH);
        center.add(board, BorderLayout.CENTER);
        JLabel hint = new JLabel("Nối từ 3 quân để mở tia chiếm lãnh thổ.", SwingConstants.CENTER);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setFont(UITheme.FONT_SMALL);
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 2));
        footer.setOpaque(false);
        territoryDelta.setFont(UITheme.FONT_SMALL_B);
        footer.add(territoryDelta);
        footer.add(hint);
        center.add(footer, BorderLayout.SOUTH);
        main.add(center, BorderLayout.CENTER);
        main.add(sidebar(), BorderLayout.EAST);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        leave.doClick();
                    }
                });
        setSize(1180, 740);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        SoundManager.scene("game");
        deltaTimer.setRepeats(false);
    }

    private UITheme.RoundedPanel playerCard(JLabel name, JLabel score, int color) {
        Color tint = color == 1 ? UITheme.PIECE_RED : UITheme.PIECE_BLUE;
        UITheme.RoundedPanel card =
                new UITheme.RoundedPanel(10, UITheme.BG_CARD, UITheme.BORDER, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        card.setMaximumSize(new Dimension(240, 194));
        card.setPreferredSize(new Dimension(182, 194));
        JComponent emblem =
                new JComponent() {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        UITheme.applyQualityHints(g);
                        ArenaArt.crystal(g, 28, 28, 44, color);
                        g.dispose();
                    }
                };
        emblem.setPreferredSize(new Dimension(56, 56));
        emblem.setMaximumSize(new Dimension(56, 56));
        JLabel role = new JLabel(color == 1 ? "ĐỎ / TINH THỂ" : "XANH / TINH THỂ");
        role.setForeground(tint);
        role.setFont(UITheme.FONT_SMALL_B);
        name.setFont(UITheme.FONT_BODY_BOLD);
        score.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 32));
        score.setForeground(tint);
        JLabel unit = new JLabel("ô lãnh thổ");
        unit.setForeground(UITheme.TEXT_MUTED);
        unit.setFont(UITheme.FONT_SMALL);
        for (JComponent c : new JComponent[] {emblem, role, name, score, unit}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(c);
        }
        return card;
    }

    private JPanel sidebar() {
        JPanel side = new JPanel(new BorderLayout(0, 14));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(236, 0));
        UITheme.RoundedPanel rules =
                new UITheme.RoundedPanel(10, UITheme.BG_CARD, UITheme.BORDER, 1);
        rules.setLayout(new GridLayout(0, 1, 0, 8));
        rules.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        String[] lines = {
            "LUẬT CHƠI",
            "Đặt vào ô trống hoặc vùng của bạn.",
            "Nối ≥3 quân ngang, dọc hoặc chéo.",
            "Tia dừng trước quân đối thủ.",
            "15 giây / lượt · 50 lượt / ván.",
            "Nhiều ô lãnh thổ hơn sẽ thắng."
        };
        for (int i = 0; i < lines.length; i++) {
            JLabel l = new JLabel(lines[i]);
            l.setFont(i == 0 ? UITheme.FONT_SMALL_B : UITheme.FONT_SMALL);
            l.setForeground(i == 0 ? UITheme.TEXT_PRIMARY : UITheme.TEXT_MUTED);
            rules.add(l);
        }
        side.add(rules, BorderLayout.NORTH);
        UITheme.RoundedPanel conversation =
                new UITheme.RoundedPanel(10, UITheme.BG_CARD, UITheme.BORDER, 1);
        conversation.setLayout(new BorderLayout(0, 12));
        conversation.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel title = new JLabel("TRÒ CHUYỆN");
        title.setFont(UITheme.FONT_SMALL_B);
        conversation.add(title, BorderLayout.NORTH);
        chat.setEditable(false);
        chat.setLineWrap(true);
        chat.setWrapStyleWord(true);
        chat.setFont(UITheme.FONT_SMALL);
        chat.setBackground(UITheme.BG_CARD);
        chat.setForeground(UITheme.TEXT_MUTED);
        JScrollPane scroll = new JScrollPane(chat);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        conversation.add(scroll, BorderLayout.CENTER);
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        UITheme.styleInput(input);
        input.getAccessibleContext().setAccessibleName("Tin nhắn trong trận");
        JButton send = UITheme.secondaryButton("Gửi", this::sendChat);
        send.setMargin(new Insets(4, 8, 4, 8));
        input.addActionListener(e -> sendChat());
        row.add(input, BorderLayout.CENTER);
        row.add(send, BorderLayout.EAST);
        conversation.add(row, BorderLayout.SOUTH);
        side.add(conversation, BorderLayout.CENTER);
        return side;
    }

    private void sendChat() {
        String text = input.getText().trim();
        if (!text.isBlank()) {
            chatAction.accept(text);
            input.setText("");
        }
    }

    @Override
    public void onMove(BiConsumer<Integer, Integer> action) {
        board.onMove(
                (row, column) -> {
                    status.setText("Đang gửi nước đi…");
                    action.accept(row, column);
                });
    }

    @Override
    public void onSurrender(Runnable action) {
        surrenderAction = action;
    }

    @Override
    public void onLeave(Runnable action) {
        leaveAction = action;
    }

    @Override
    public void onChat(Consumer<String> action) {
        chatAction = action;
    }

    @Override
    public void showBoard(BoardState state, String own) {
        matchId = state.matchId;
        ownColor = own;
        int redDelta = state.redScore - lastRedScore;
        int blueDelta = state.blueScore - lastBlueScore;
        lastRedScore = state.redScore;
        lastBlueScore = state.blueScore;
        BoardFeedback.Change change = feedback.accept(state, own);
        if (change.reset()) {
            rematchState = null;
            scoreAccent.stop();
            redScore.setForeground(UITheme.PIECE_RED);
            blueScore.setForeground(UITheme.PIECE_BLUE);
            deltaTimer.stop();
            territoryDelta.setText(" ");
            ended = false;
            board.active(true);
            status.setText("Trận đấu mới");
        }
        board.update(state, own, change);
        if (change.move()) {
            if (state.captureRays.isEmpty()) {
                if (change.turn() && !spectator && own.equals(state.turnColor))
                    SoundManager.playTurn();
                else SoundManager.playMove();
            } else SoundManager.playCapture();
            if (!state.captureRays.isEmpty()) {
                int delta = state.moveColor == 1 ? redDelta : blueDelta;
                territoryDelta.setText(
                        (state.moveColor == 1 ? "Đỏ" : "Xanh")
                                + " chiếm lãnh thổ · "
                                + (delta >= 0 ? "+" : "")
                                + delta
                                + " ô");
                territoryDelta.setForeground(
                        state.moveColor == 1 ? UITheme.PIECE_RED : UITheme.PIECE_BLUE);
                deltaTimer.setInitialDelay(1400);
                deltaTimer.restart();
                scoreAccentColor = state.moveColor;
                scoreAccentStarted = System.nanoTime();
                if (!ClientSettings.reducedMotion()) scoreAccent.restart();
            }
        } else if (change.turn() && own.equals(state.turnColor)) SoundManager.playTurn();
        if (change.tick()) SoundManager.playTick();
        boolean redTurn = "RED".equals(state.turnColor), iAmRed = "RED".equals(own);
        identity.setText(spectator ? "Chế độ khán giả" : "Bạn: " + (iAmRed ? "Đỏ" : "Xanh"));
        identity.setForeground(
                spectator ? UITheme.TEXT_MUTED : iAmRed ? UITheme.PIECE_RED : UITheme.PIECE_BLUE);
        redName.setText(spectator ? username : iAmRed ? username + " (bạn)" : opponent);
        blueName.setText(spectator ? opponent : iAmRed ? opponent : username + " (bạn)");
        redName.setToolTipText(redName.getText());
        blueName.setToolTipText(blueName.getText());
        redScore.setText(Integer.toString(state.redScore));
        blueScore.setText(Integer.toString(state.blueScore));
        redCard.setBorderColor(redTurn ? UITheme.PIECE_RED : UITheme.BORDER);
        blueCard.setBorderColor(redTurn ? UITheme.BORDER : UITheme.PIECE_BLUE);
        turns.setText("Lượt " + Math.min(50, 51 - state.turnsRemaining) + " / 50");
        timer.setText(state.secondsRemaining + "s");
        timeBar.setValue(state.secondsRemaining);
        timeBar.setForeground(state.secondsRemaining <= 5 ? UITheme.PIECE_RED : UITheme.ACCENT);
        timer.setForeground(state.secondsRemaining <= 5 ? UITheme.PIECE_RED : UITheme.TEXT_PRIMARY);
        status.setText(
                spectator
                        ? (redTurn ? "Lượt Đỏ" : "Lượt Xanh")
                        : board.isPending()
                                ? "Đang gửi nước đi…"
                                : own.equals(state.turnColor) ? "Lượt của bạn" : "Đợi đối thủ");
        status.setForeground(redTurn ? UITheme.PIECE_RED : UITheme.PIECE_BLUE);
    }

    @Override
    public boolean askRematch(String result) {
        ended = true;
        board.active(false);
        return GameEndDialog.show(
                this,
                result,
                ownColor,
                username,
                opponent,
                lastRedScore,
                lastBlueScore,
                0,
                panel -> {
                    resultPanel = panel;
                    if (rematchState != null)
                        GameEndDialog.updateReadiness(panel, ownColor, rematchState);
                });
    }

    private void updateScoreAccent() {
        double age = (System.nanoTime() - scoreAccentStarted) / 1_000_000_000.0;
        float emphasis = age < .25 ? 0 : (float) (1 - BoardMotion.clamp((age - .25) / .65));
        JLabel label = scoreAccentColor == 1 ? redScore : blueScore;
        Color base = scoreAccentColor == 1 ? UITheme.PIECE_RED : UITheme.PIECE_BLUE;
        label.setForeground(UITheme.blendColors(base, UITheme.TEXT_PRIMARY, emphasis * .65f));
        if (age >= .9 || ClientSettings.reducedMotion()) {
            label.setForeground(base);
            scoreAccent.stop();
        }
    }

    @Override
    public void moveRejected(String reason) {
        board.releasePending();
        status.setText("Chọn lại ô hợp lệ");
        territoryDelta.setForeground(UITheme.PIECE_RED);
        territoryDelta.setText(reason == null ? "Không thể đặt quân vào ô này." : reason);
        deltaTimer.setInitialDelay(3000);
        deltaTimer.restart();
    }

    @Override
    public void rematchStatus(RematchStatus state) {
        if (!matchId.equals(state.matchId())) return;
        rematchState = state;
        status.setText(GameEndDialog.readiness(ownColor, state));
        GameEndDialog.updateReadiness(resultPanel, ownColor, state);
    }

    @Override
    public void showMessage(String text) {
        boolean wasPending = board.isPending();
        board.releasePending();
        if (!ended && wasPending) status.setText("Chọn lại ô hợp lệ");
        JOptionPane.showMessageDialog(this, text, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void addChatMessage(String sender, String text) {
        if (!username.equals(sender)) SoundManager.playChat();
        chat.append(LocalTime.now().format(TIME) + "  " + sender + "\n" + text + "\n\n");
        if (chat.getDocument().getLength() > 24000) chat.replaceRange("", 0, 8000);
        chat.setCaretPosition(chat.getDocument().getLength());
    }

    @Override
    public void setActionsEnabled(boolean value) {
        ended = !value;
        surrender.setEnabled(value);
        board.active(value);
        if (!value) {
            status.setText("Ván đấu đã kết thúc");
            timer.setText("—");
        }
    }

    @Override
    public void waitingForRematch() {
        status.setText(
                rematchState == null
                        ? "Đang chờ máy chủ xác nhận đấu lại…"
                        : GameEndDialog.readiness(ownColor, rematchState));
    }

    @Override
    public void connectionLost(Runnable login) {
        loginAfterDisconnect = login;
        status.setText("Mất kết nối máy chủ");
        leave.setText("Về đăng nhập");
        board.active(false);
        if (ClientDialog.ask(
                this,
                "Mất kết nối",
                "Kết nối đã đóng. Trở về đăng nhập để kết nối lại.",
                "Về đăng nhập",
                "Xem lại bàn")) login.run();
    }

    @Override
    public void updateSpectatorCount(int count) {
        watchers.setText(count + " người xem");
    }

    @Override
    public boolean isSpectator() {
        return spectator;
    }

    @Override
    public void close() {
        dispose();
    }

    @Override
    public void dispose() {
        deltaTimer.stop();
        scoreAccent.stop();
        board.stopEffect();
        super.dispose();
    }
}
