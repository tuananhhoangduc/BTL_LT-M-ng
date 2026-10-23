package client;

import java.awt.*;

import javax.swing.*;

final class AboutDialog {
    private AboutDialog() {}

    static void show(Window owner) {
        JTextArea text =
                new JTextArea(
                        AppBrand.NAME
                                + " · "
                                + AppBrand.VERSION
                                + "\n\n"
                                + AppBrand.TAGLINE
                                + "\n\n"
                                + "BGM: Puzzle Game — Bright & Casual\n"
                                + "File MP3 do người dùng cung cấp.\n"
                                + "cyberwave-orchestra-puzzle-game-bright-casual-video-game-music-249202.mp3\n\n"
                                + "SFX: Kenney — Interface Sounds / Music Jingles (CC0).\n\n"
                                + "Mỹ thuật PNG: tạo bằng image_gen theo concept Color Match Arena.\n"
                                + "Chi tiết nguồn, prompt và phiên bản trong"
                                + " assets/ASSET_CREDITS.md.");
        text.setFont(UITheme.FONT_BODY);
        text.setBackground(UITheme.BG_CARD);
        text.setForeground(UITheme.TEXT_MUTED);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setColumns(44);
        text.setRows(14);
        text.setCaretPosition(0);
        JOptionPane.showMessageDialog(
                owner,
                new JScrollPane(text),
                "Giới thiệu — " + AppBrand.NAME,
                JOptionPane.PLAIN_MESSAGE);
    }
}
