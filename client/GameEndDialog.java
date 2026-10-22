package client;

import java.awt.*;
import javax.swing.*;

final class GameEndDialog extends JDialog {
    private boolean rematchAccepted = false;

    static boolean show(Frame owner, String resultText, String ownColor, String username, String opponent,
                        int redScore, int blueScore, double currentTotalPoints) {
        GameEndDialog dialog = new GameEndDialog(owner, resultText, ownColor, username, opponent,
                redScore, blueScore, currentTotalPoints);
        dialog.setVisible(true);
        return dialog.rematchAccepted;
    }

    private GameEndDialog(Frame owner, String resultText, String ownColor, String username, String opponent,
                          int redScore, int blueScore, double currentTotalPoints) {
        super(owner, "Kết quả trận đấu", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(450, 480);
        setResizable(false);
        setLocationRelativeTo(owner);

        String[] parts = resultText != null ? resultText.split("\\|", 2) : new String[]{"DRAW", ""};
        String winner = parts[0];
        String rawReason = parts.length > 1 ? parts[1] : "";

        boolean isWin = ownColor != null && ownColor.equals(winner);
        boolean isDraw = "DRAW".equals(winner);

        // Phát âm thanh tương ứng
        if (isWin) SoundManager.playWin();
        else if (isDraw) SoundManager.playCapture();
        else SoundManager.playLose();

        String titleText = isWin ? "Bạn đã chiến thắng!" : (isDraw ? "Trận đấu Hòa!" : "Bạn đã thua!");
        Color titleColor = isWin ? UITheme.WIN_COLOR : (isDraw ? UITheme.DRAW_COLOR : UITheme.LOSS_COLOR);

        // Root container với bóng đổ
        UITheme.RoundedPanel root = new UITheme.RoundedPanel(24, Color.WHITE, UITheme.BORDER, 1);
        root.enableShadow(true, 10);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Trophy Icon với hào quang tròn
        JPanel iconBadge = new JPanel() {
            {
                setPreferredSize(new Dimension(84, 84));
                setMaximumSize(new Dimension(84, 84));
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);

                Color glow = isWin ? new Color(254, 240, 138) : (isDraw ? new Color(254, 243, 199) : new Color(254, 226, 226));
                g2.setColor(glow);
                g2.fillOval(4, 4, 76, 76);

                Color iconColor = isWin ? new Color(217, 119, 6) : (isDraw ? UITheme.DRAW_COLOR : UITheme.LOSS_COLOR);
                Icon vecIcon = UITheme.createVectorIcon(isWin ? "trophy" : (isDraw ? "star" : "logout"), 42, iconColor);
                vecIcon.paintIcon(this, g2, 21, 21);
                g2.dispose();
            }
        };
        iconBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER);
        titleLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 22));
        titleLabel.setForeground(titleColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String reasonText = switch (rawReason) {
            case "TURNS_FINISHED" -> "Hết 50 lượt thi đấu";
            case "BOARD_FULL" -> "Bàn cờ đã đầy";
            case "SURRENDER" -> isWin ? "Đối thủ đã xin thua" : "Bạn đã xin thua";
            case "LEAVE_GAME" -> isWin ? "Đối thủ đã thoát trận" : "Bạn đã thoát trận";
            default -> rawReason.isBlank() ? "Trận đấu đã kết thúc" : rawReason;
        };
        JLabel reasonLabel = new JLabel(reasonText, SwingConstants.CENTER);
        reasonLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 13));
        reasonLabel.setForeground(UITheme.TEXT_MUTED);
        reasonLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Thẻ so sánh điểm
        UITheme.RoundedPanel scoreCard = new UITheme.RoundedPanel(16, new Color(248, 250, 252), UITheme.BORDER, 1);
        scoreCard.setLayout(new GridLayout(1, 2, 16, 0));
        scoreCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        scoreCard.setMaximumSize(new Dimension(380, 100));

        boolean iAmRed = "RED".equals(ownColor);
        int myScore = iAmRed ? redScore : blueScore;
        int oppScore = iAmRed ? blueScore : redScore;

        scoreCard.add(createScoreBlock("Bạn (1P)", myScore, UITheme.PIECE_RED, isWin));
        scoreCard.add(createScoreBlock("Đối thủ (2P)", oppScore, UITheme.PIECE_BLUE, !isWin && !isDraw));

        // Rank points badge
        JLabel rankBadge = new JLabel((isWin ? "+1.0" : (isDraw ? "+0.5" : "+0.0")) + " điểm xếp hạng",
                UITheme.createVectorIcon("star", 16, UITheme.DRAW_COLOR), SwingConstants.CENTER);
        rankBadge.setIconTextGap(6);
        rankBadge.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 15));
        rankBadge.setForeground(UITheme.DRAW_COLOR);
        rankBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons row
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        UITheme.SmoothButton playAgain = new UITheme.SmoothButton(
                "Chơi tiếp", UITheme.PRIMARY, UITheme.PRIMARY_DARK, UITheme.PRIMARY_DARK, 12);
        UITheme.SmoothButton backLobby = new UITheme.SmoothButton(
                "Về sảnh", new Color(226, 232, 240), new Color(203, 213, 225), new Color(148, 163, 184), 12);
        backLobby.setForeground(UITheme.TEXT_PRIMARY);

        playAgain.setPreferredSize(new Dimension(140, 42));
        backLobby.setPreferredSize(new Dimension(110, 42));

        playAgain.addActionListener(e -> {
            SoundManager.playClick();
            rematchAccepted = true;
            dispose();
        });
        backLobby.addActionListener(e -> {
            SoundManager.playClick();
            rematchAccepted = false;
            dispose();
        });

        buttons.add(playAgain);
        buttons.add(backLobby);

        root.add(iconBadge);
        root.add(Box.createVerticalStrut(4));
        root.add(titleLabel);
        root.add(Box.createVerticalStrut(2));
        root.add(reasonLabel);
        root.add(Box.createVerticalStrut(14));
        root.add(scoreCard);
        root.add(Box.createVerticalStrut(12));
        root.add(rankBadge);
        root.add(Box.createVerticalStrut(18));
        root.add(buttons);

        setContentPane(root);
    }

    private JPanel createScoreBlock(String label, int score, Color color, boolean highlight) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel val = new JLabel(String.valueOf(score), SwingConstants.CENTER);
        val.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 26));
        val.setForeground(highlight ? color : UITheme.TEXT_PRIMARY);
        val.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel unit = new JLabel("Quân", SwingConstants.CENTER);
        unit.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        unit.setForeground(UITheme.TEXT_MUTED);
        unit.setAlignmentX(Component.CENTER_ALIGNMENT);

        block.add(lbl);
        block.add(val);
        block.add(unit);
        return block;
    }
}
