package client;

import java.awt.*;

import javax.swing.*;

final class ClientDialog {
    private ClientDialog() {}

    static boolean ask(
            Window owner, String title, String message, String acceptText, String rejectText) {
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel heading = new JLabel(title);
        heading.setFont(UITheme.FONT_HEADER);
        root.add(heading, BorderLayout.NORTH);
        JTextArea body = new JTextArea(message);
        body.setEditable(false);
        body.setFocusable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setRows(3);
        body.setColumns(32);
        body.setFont(UITheme.FONT_BODY);
        body.setBackground(UITheme.BG_CARD);
        body.setForeground(UITheme.TEXT_MUTED);
        root.add(body, BorderLayout.CENTER);
        boolean[] accepted = {false};
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton cancel = UITheme.secondaryButton(rejectText, dialog::dispose);
        JButton accept = UITheme.createGradientButton(acceptText, 140, 38);
        accept.addActionListener(
                e -> {
                    SoundManager.playClick();
                    accepted[0] = true;
                    dialog.dispose();
                });
        actions.add(cancel);
        actions.add(accept);
        root.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(cancel);
        dialog.getRootPane()
                .registerKeyboardAction(
                        e -> dialog.dispose(),
                        KeyStroke.getKeyStroke("ESCAPE"),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        if (title.contains("thách")) SoundManager.playChallenge();
        dialog.setVisible(true);
        return accepted[0];
    }
}
