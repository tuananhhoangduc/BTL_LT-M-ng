package client;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

final class ClientDialog {
    private ClientDialog() {}

    static boolean ask(Window owner, String title, String message, String acceptText, String rejectText) {
        JDialog dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 310);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);

        // Sound thông báo thách đấu
        SoundManager.playChallenge();

        // Background chính
        UITheme.RoundedPanel root = new UITheme.RoundedPanel(20, Color.WHITE, UITheme.BORDER, 1);
        root.enableShadow(true, 8);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // Trích xuất tên người gửi
        String challenger = "Người chơi";
        if (message != null && message.contains(" ")) {
            String first = message.split("\\s+")[0].trim();
            if (!first.isBlank()) challenger = first;
        }

        // Avatar Squircle gradient tím - xanh
        JPanel avatar = UITheme.createSquircleAvatar(challenger, 64, new Color(124, 58, 237), new Color(59, 130, 246));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tiêu đề & Lời mời
        JLabel titleLabel = new JLabel("Lời mời thách đấu", SwingConstants.CENTER);
        titleLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 17));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("<html><div style='text-align: center; color: #64748b; font-size: 13px; font-family: Segoe UI;'>"
                + "<b style='color: #4f46e5; font-size: 14px;'>" + challenger + "</b> muốn thách đấu với bạn!<br>"
                + "Bạn có sẵn sàng tham chiến ngay bây giờ?</div></html>", SwingConstants.CENTER);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Animated bouncing dots (hiệu ứng chờ sinh động)
        UITheme.BouncingDots dots = new UITheme.BouncingDots(UITheme.ACCENT);
        dots.setAlignmentX(Component.CENTER_ALIGNMENT);

        boolean[] accepted = {false};
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nút Đồng ý (Emerald Green)
        UITheme.SmoothButton btnAccept = new UITheme.SmoothButton(
                "Đồng ý",
                new Color(16, 185, 129),
                new Color(5, 150, 105),
                new Color(4, 120, 87),
                12
        );
        btnAccept.setPreferredSize(new Dimension(130, 40));

        // Nút Từ chối (Rose Red)
        UITheme.SmoothButton btnReject = new UITheme.SmoothButton(
                "Từ chối",
                new Color(239, 68, 68),
                new Color(220, 38, 38),
                new Color(185, 28, 28),
                12
        );
        btnReject.setPreferredSize(new Dimension(130, 40));

        btnAccept.addActionListener(e -> {
            dots.stopAnimation();
            SoundManager.playClick();
            accepted[0] = true;
            dialog.dispose();
        });

        btnReject.addActionListener(e -> {
            dots.stopAnimation();
            SoundManager.playClick();
            dialog.dispose();
        });

        buttons.add(btnAccept);
        buttons.add(btnReject);

        root.add(avatar);
        root.add(Box.createVerticalStrut(10));
        root.add(titleLabel);
        root.add(Box.createVerticalStrut(4));
        root.add(msgLabel);
        root.add(Box.createVerticalStrut(6));
        root.add(dots);
        root.add(Box.createVerticalStrut(14));
        root.add(buttons);

        dialog.setContentPane(root);
        dialog.setVisible(true);
        return accepted[0];
    }
}
