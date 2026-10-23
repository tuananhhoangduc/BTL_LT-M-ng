package client;

import java.awt.*;

import javax.swing.*;

final class SettingsDialog {
    private SettingsDialog() {}

    static void show(Window owner) {
        JDialog dialog =
                new JDialog(
                        owner, "Cài đặt — " + AppBrand.NAME, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = content(dialog, dialog::dispose);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton((JButton) root.getClientProperty("defaultButton"));
        dialog.getRootPane()
                .registerKeyboardAction(
                        e -> dialog.dispose(),
                        KeyStroke.getKeyStroke("ESCAPE"),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    static JPanel content(Window owner, Runnable close) {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 16, 0);
        JLabel title = new JLabel("Âm thanh & hiển thị");
        title.setFont(UITheme.FONT_TITLE);
        root.add(title, c);
        String[] names = {"Âm lượng tổng", "Nhạc nền", "Hiệu ứng"};
        String[] keys = {"master", "music", "sfx"};
        int[] values = {ClientSettings.master(), ClientSettings.music(), ClientSettings.sfx()};
        for (int i = 0; i < names.length; i++) {
            c.gridy++;
            c.gridwidth = 1;
            c.gridx = 0;
            c.weightx = 0;
            root.add(new JLabel(names[i]), c);
            JSlider slider = new JSlider(0, 100, values[i]);
            slider.setPreferredSize(new Dimension(220, 32));
            slider.getAccessibleContext().setAccessibleName(names[i]);
            String key = keys[i];
            slider.addChangeListener(
                    e -> {
                        ClientSettings.volume(key, slider.getValue());
                        slider.setToolTipText(slider.getValue() + "%");
                        SoundManager.refresh();
                    });
            c.gridx = 1;
            c.weightx = 1;
            JPanel control = new JPanel(new BorderLayout(8, 0));
            control.setOpaque(false);
            JLabel percentage = new JLabel(values[i] + "%");
            percentage.setPreferredSize(new Dimension(38, 28));
            slider.addChangeListener(e -> percentage.setText(slider.getValue() + "%"));
            control.add(slider, BorderLayout.CENTER);
            control.add(percentage, BorderLayout.EAST);
            root.add(control, c);
        }
        JCheckBox mute = new JCheckBox("Tắt toàn bộ âm thanh", ClientSettings.muted());
        mute.addActionListener(
                e -> {
                    ClientSettings.flag("muted", mute.isSelected());
                    SoundManager.refresh();
                });
        JCheckBox motion = new JCheckBox("Giảm chuyển động", ClientSettings.reducedMotion());
        motion.addActionListener(e -> ClientSettings.flag("reducedMotion", motion.isSelected()));
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        root.add(mute, c);
        c.gridy++;
        root.add(motion, c);
        c.gridy++;
        JLabel track = new JLabel("BGM: " + AppBrand.MUSIC);
        track.setFont(UITheme.FONT_SMALL);
        track.setForeground(UITheme.TEXT_MUTED);
        root.add(track, c);
        c.gridy++;
        JPanel links = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        links.setOpaque(false);
        links.add(UITheme.secondaryButton("Cách chơi", () -> HelpDialog.show(owner)));
        links.add(UITheme.secondaryButton("Giới thiệu / Credits", () -> AboutDialog.show(owner)));
        root.add(links, c);
        c.gridy++;
        c.insets = new Insets(8, 0, 0, 0);
        JButton done = UITheme.secondaryButton("Xong", close);
        root.add(done, c);
        root.putClientProperty("defaultButton", done);
        return root;
    }
}
