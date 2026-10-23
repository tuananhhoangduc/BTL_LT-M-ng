package client;

import common.RematchStatus;

import java.awt.*;
import java.util.function.Consumer;

import javax.swing.*;

final class GameEndDialog {
    private GameEndDialog() {}

    static boolean show(
            Frame owner,
            String result,
            String ownColor,
            String username,
            String opponent,
            int redScore,
            int blueScore,
            double ignoredTotal) {
        return show(
                owner,
                result,
                ownColor,
                username,
                opponent,
                redScore,
                blueScore,
                ignoredTotal,
                panel -> {});
    }

    static boolean show(
            Frame owner,
            String result,
            String ownColor,
            String username,
            String opponent,
            int redScore,
            int blueScore,
            double ignoredTotal,
            Consumer<JPanel> opened) {
        JDialog dialog = new JDialog(owner, "Kết quả — " + AppBrand.NAME, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        boolean[] again = {false};
        JPanel root =
                content(
                        result,
                        ownColor,
                        username,
                        opponent,
                        redScore,
                        blueScore,
                        dialog::dispose,
                        () -> {
                            again[0] = true;
                            dialog.dispose();
                        });
        if (!"SPECTATOR".equals(ownColor)) {
            if (result != null && result.startsWith(ownColor + "|")) SoundManager.playWin();
            else if (result != null && result.startsWith("DRAW|")) SoundManager.playCapture();
            else SoundManager.playLose();
        }
        dialog.setContentPane(root);
        dialog.getRootPane()
                .registerKeyboardAction(
                        e -> dialog.dispose(),
                        KeyStroke.getKeyStroke("ESCAPE"),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(440, dialog.getHeight()));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        opened.accept(root);
        try {
            dialog.setVisible(true);
        } finally {
            opened.accept(null);
        }
        return again[0];
    }

    static JPanel content(
            String result,
            String ownColor,
            String username,
            String opponent,
            int redScore,
            int blueScore,
            Runnable back,
            Runnable rematch) {
        String[] parts = (result == null ? "DRAW|" : result).split("\\|", 2);
        String winner = parts[0], reason = parts.length > 1 ? parts[1] : "";
        boolean spectator = "SPECTATOR".equals(ownColor),
                draw = "DRAW".equals(winner),
                win = winner.equals(ownColor);
        String title =
                spectator
                        ? (draw
                                ? "Ván đấu hòa"
                                : ("RED".equals(winner) ? "Đỏ chiến thắng" : "Xanh chiến thắng"))
                        : draw ? "Ván đấu hòa" : win ? "Bạn chiến thắng" : "Ván đấu kết thúc";
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        String emblemId =
                draw ? "emblem.draw" : spectator || win ? "emblem.victory" : "emblem.defeat";
        JComponent emblem =
                new JComponent() {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        UITheme.applyQualityHints(g);
                        SpriteCatalog.draw(g, emblemId, 4, 4, 88, 88);
                        g.dispose();
                    }
                };
        emblem.setPreferredSize(new Dimension(96, 96));
        emblem.setMaximumSize(new Dimension(96, 96));
        emblem.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(emblem);
        root.add(Box.createVerticalStrut(12));
        JLabel caption = new JLabel("KẾT QUẢ");
        caption.setFont(UITheme.FONT_SMALL_B);
        caption.setForeground(UITheme.TEXT_MUTED);
        JLabel heading = new JLabel(title);
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(win || draw ? UITheme.DRAW_COLOR : UITheme.TEXT_PRIMARY);
        JLabel explanation = new JLabel(reason.isBlank() ? "Trận đấu đã kết thúc" : reason);
        explanation.setFont(UITheme.FONT_SMALL);
        explanation.setForeground(UITheme.TEXT_MUTED);
        JPanel scores = new JPanel(new GridLayout(1, 2, 24, 0));
        scores.setOpaque(false);
        boolean red = "RED".equals(ownColor);
        scores.add(
                score(
                        spectator ? username : red ? username + " (bạn)" : opponent,
                        redScore,
                        UITheme.PIECE_RED));
        scores.add(
                score(
                        spectator ? opponent : red ? opponent : username + " (bạn)",
                        blueScore,
                        UITheme.PIECE_BLUE));
        for (JComponent c : new JComponent[] {caption, heading, explanation}) {
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
            root.add(c);
            root.add(Box.createVerticalStrut(10));
        }
        root.add(Box.createVerticalStrut(8));
        root.add(scores);
        root.add(Box.createVerticalStrut(24));
        JLabel info =
                new JLabel(
                        spectator
                                ? "Bạn đang xem trận đấu."
                                : draw
                                        ? "+0.5 điểm xếp hạng"
                                        : win ? "+1.0 điểm xếp hạng" : "+0.0 điểm xếp hạng");
        info.setFont(UITheme.FONT_SMALL);
        info.setForeground(UITheme.TEXT_MUTED);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(info);
        JLabel readiness = new JLabel("Hai người chơi chưa chọn đấu lại.");
        readiness.setFont(UITheme.FONT_SMALL);
        readiness.setForeground(UITheme.TEXT_MUTED);
        readiness.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.putClientProperty("rematchStatus", readiness);
        if (!spectator) {
            root.add(Box.createVerticalStrut(10));
            root.add(readiness);
        }
        root.add(Box.createVerticalStrut(20));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);
        actions.add(UITheme.secondaryButton("Về sảnh", back));
        if (!spectator) {
            JButton rematchButton = UITheme.createGradientButton("Đấu lại", 140, 40);
            rematchButton.addActionListener(
                    e -> {
                        rematch.run();
                        SoundManager.playClick();
                    });
            actions.add(rematchButton);
        }
        root.add(actions);
        return root;
    }

    static String readiness(String ownColor, RematchStatus state) {
        if (state.closed()) return state.reason();
        boolean own = "RED".equals(ownColor) ? state.redAccepted() : state.blueAccepted();
        boolean peer = "RED".equals(ownColor) ? state.blueAccepted() : state.redAccepted();
        return "Bạn: "
                + (own ? "sẵn sàng" : "chưa chọn")
                + " · Đối thủ: "
                + (peer ? "sẵn sàng" : "chưa chọn");
    }

    static void updateReadiness(JPanel root, String ownColor, RematchStatus state) {
        if (root != null && root.getClientProperty("rematchStatus") instanceof JLabel label)
            label.setText(readiness(ownColor, state));
    }

    private static JPanel score(String name, int value, Color tint) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(name);
        label.setFont(UITheme.FONT_BODY_BOLD);
        JLabel number = new JLabel(Integer.toString(value));
        number.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 40));
        number.setForeground(tint);
        JLabel unit = new JLabel("ô lãnh thổ");
        unit.setForeground(UITheme.TEXT_MUTED);
        unit.setFont(UITheme.FONT_SMALL);
        for (JComponent c : new JComponent[] {label, number, unit}) {
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(c);
        }
        return p;
    }
}
