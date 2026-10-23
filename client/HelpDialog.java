package client;

import common.RuleExample;

import java.awt.*;

import javax.swing.*;

final class HelpDialog {
    private HelpDialog() {}

    static void show(Window owner) {
        JDialog dialog =
                new JDialog(
                        owner,
                        "Cách chơi — " + AppBrand.NAME,
                        Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(content(dialog::dispose));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.getRootPane()
                .registerKeyboardAction(
                        e -> dialog.dispose(),
                        KeyStroke.getKeyStroke("ESCAPE"),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setVisible(true);
    }

    static JPanel content(Runnable close) {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        root.setPreferredSize(new Dimension(600, 600));
        JLabel title = new JLabel();
        title.setFont(UITheme.FONT_HEADER);
        JLabel description = new JLabel();
        description.setForeground(UITheme.TEXT_MUTED);
        description.setFont(UITheme.FONT_SMALL);
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 8));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(description);
        root.add(heading, BorderLayout.NORTH);
        BoardPanel example = new BoardPanel(true);
        root.add(example, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        int[] step = {0};
        String[] titles = {"1 / 3   Đặt quân", "2 / 3   Chuẩn bị nối ba", "3 / 3   Chiếm lãnh thổ"};
        String[] texts = {
            "Đặt vào ô trống hoặc vùng của mình. Ví dụ: quân đỏ tại C5.",
            "C5 và D5 đã có quân đỏ. Đặt thêm ở E5 để nối ba.",
            "Tia chiếm A5–F5, dừng trước quân xanh G5. Đỏ nhận 6 ô."
        };
        JButton next = UITheme.createGradientButton("Tiếp theo", 130, 36);
        JButton previous = UITheme.secondaryButton("Quay lại", () -> {});
        Runnable refresh =
                () -> {
                    title.setText(titles[step[0]]);
                    description.setText(texts[step[0]]);
                    example.update(
                            RuleExample.step(step[0]),
                            "SPECTATOR",
                            new BoardFeedback.Change(true, false, false, false));
                    previous.setEnabled(step[0] > 0);
                    next.setText(step[0] == 2 ? "Đã hiểu" : "Tiếp theo");
                };
        previous.addActionListener(
                e -> {
                    if (step[0] > 0) {
                        step[0]--;
                        refresh.run();
                    }
                });
        next.addActionListener(
                e -> {
                    SoundManager.playClick();
                    if (step[0] == 2) close.run();
                    else {
                        step[0]++;
                        refresh.run();
                    }
                });
        buttons.add(previous);
        buttons.add(next);
        root.add(buttons, BorderLayout.SOUTH);
        refresh.run();
        return root;
    }
}
