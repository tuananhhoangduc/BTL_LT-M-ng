package client;

import common.MatchHistory;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

final class HistoryFrame extends JFrame {
    private final String username;
    private final List<MatchHistory> historyList = new ArrayList<>();
    private final JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 14, 0));
    private final JPanel listContainer = new JPanel();

    HistoryFrame(String username, List<MatchHistory> initialHistory) {
        super("Color Match Arena - Lịch sử đấu [" + username + "]");
        this.username = username;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_PAGE);
        setLayout(new BorderLayout());

        add(createTopGradientHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        centerPanel.add(createSidebar(), BorderLayout.WEST);
        centerPanel.add(createMainContentCard(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        setHistory(initialHistory);
    }

    private JPanel createTopGradientHeader() {
        UITheme.GradientPanel header = new UITheme.GradientPanel(
                new Color(67, 56, 202), new Color(14, 165, 233), true);
        header.setPreferredSize(new Dimension(0, 68));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        brand.setOpaque(false);
        JLabel historyIcon = new JLabel(UITheme.createVectorIcon("history", 22, Color.WHITE));
        JLabel logo = new JLabel("LỊCH SỬ THI ĐẤU");
        logo.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 19));
        logo.setForeground(Color.WHITE);
        brand.add(historyIcon);
        brand.add(logo);
        header.add(brand, BorderLayout.WEST);

        JButton backBtn = new JButton("Quay lại sảnh", UITheme.createVectorIcon("logout", 16, Color.WHITE)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                Color bg = getModel().isRollover() ? new Color(255, 255, 255, 50) : new Color(255, 255, 255, 30);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        backBtn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        backBtn.setForeground(Color.WHITE);
        backBtn.setIconTextGap(8);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        backBtn.addActionListener(e -> {
            SoundManager.playClick();
            dispose();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 16));
        right.setOpaque(false);
        right.add(backBtn);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        UITheme.RoundedPanel sidebar = new UITheme.RoundedPanel(18, Color.WHITE, UITheme.BORDER, 1);
        sidebar.enableShadow(true);
        sidebar.setLayout(new BorderLayout(0, 18));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 14, 20, 14));

        // 1. TOP PROFILE SECTION (Căn giữa tuyệt đối)
        JPanel profilePanel = new JPanel();
        profilePanel.setOpaque(false);
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));

        JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrap.setOpaque(false);
        avatarWrap.add(UITheme.createSquircleAvatar(username, 68, UITheme.PRIMARY, UITheme.ACCENT));
        avatarWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(avatarWrap);

        profilePanel.add(Box.createVerticalStrut(10));

        JLabel nameLbl = new JLabel(username);
        nameLbl.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 17));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);
        nameLbl.setHorizontalAlignment(SwingConstants.CENTER);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(nameLbl);

        profilePanel.add(Box.createVerticalStrut(6));

        JPanel statusPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                g2.setColor(new Color(238, 242, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(199, 210, 254));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusPill.setOpaque(false);
        statusPill.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPill.setMaximumSize(new Dimension(170, 26));

        JLabel statusTxt = new JLabel("● Hồ sơ chiến tích", SwingConstants.CENTER);
        statusTxt.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        statusTxt.setForeground(UITheme.PRIMARY);
        statusPill.add(statusTxt);

        profilePanel.add(statusPill);

        sidebar.add(profilePanel, BorderLayout.NORTH);

        // 2. CENTER NAVIGATION ITEMS
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        navPanel.add(createSidebarBtn("Sảnh chờ", "home", false, this::dispose));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(createSidebarBtn("Lịch sử đấu", "history", true, () -> {}));

        sidebar.add(navPanel, BorderLayout.CENTER);

        // 3. BOTTOM BACK
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(createSidebarBtn("Quay lại", "logout", false, this::dispose), BorderLayout.SOUTH);

        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createSidebarBtn(String text, String iconType, boolean active, Runnable action) {
        Color iconColor = active ? UITheme.PRIMARY : UITheme.TEXT_MUTED;
        JButton btn = new JButton(text, UITheme.createVectorIcon(iconType, 18, iconColor)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                if (active) {
                    g2.setColor(new Color(238, 242, 255));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(UITheme.PRIMARY);
                    g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(UITheme.FONT_FAMILY, active ? Font.BOLD : Font.PLAIN, 14));
        btn.setForeground(active ? UITheme.PRIMARY : UITheme.TEXT_MUTED);
        btn.setIconTextGap(12);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(202, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            SoundManager.playClick();
            action.run();
        });
        return btn;
    }

    private JPanel createMainContentCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel(18, Color.WHITE, UITheme.BORDER, 1);
        card.enableShadow(true);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        JLabel title = new JLabel("Chi tiết lịch sử đối đầu");
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 17));
        title.setForeground(UITheme.TEXT_PRIMARY);
        topContainer.add(title, BorderLayout.NORTH);

        summaryPanel.setOpaque(false);
        topContainer.add(summaryPanel, BorderLayout.CENTER);
        card.add(topContainer, BorderLayout.NORTH);

        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    void setHistory(List<MatchHistory> matches) {
        historyList.clear();
        if (matches != null) historyList.addAll(matches);
        buildSummary();
        buildMatchList();
        revalidate();
        repaint();
    }

    private void buildSummary() {
        summaryPanel.removeAll();
        int total = historyList.size();
        int wins = 0, losses = 0, draws = 0;

        for (MatchHistory m : historyList) {
            String res = m.result() != null ? m.result().toUpperCase() : "";
            if (res.contains("WIN") || res.contains("THẮNG")) wins++;
            else if (res.contains("LOSS") || res.contains("THUA")) losses++;
            else draws++;
        }

        summaryPanel.add(createStatCard("Tổng trận", String.valueOf(total), UITheme.TEXT_PRIMARY));
        summaryPanel.add(createStatCard("Thắng", String.valueOf(wins), UITheme.WIN_COLOR));
        summaryPanel.add(createStatCard("Thua", String.valueOf(losses), UITheme.LOSS_COLOR));
        summaryPanel.add(createStatCard("Hòa", String.valueOf(draws), UITheme.DRAW_COLOR));
    }

    private JPanel createStatCard(String label, String value, Color col) {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel(14, new Color(248, 250, 252), UITheme.BORDER, 1);
        card.setLayout(new BorderLayout(0, 2));
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        card.setPreferredSize(new Dimension(140, 68));

        JLabel title = new JLabel(label, SwingConstants.CENTER);
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        title.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 22));
        val.setForeground(col);

        card.add(title, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private void buildMatchList() {
        listContainer.removeAll();
        if (historyList.isEmpty()) {
            JLabel empty = new JLabel("Chưa có dữ liệu trận đấu", SwingConstants.CENTER);
            empty.setFont(new Font(UITheme.FONT_FAMILY, Font.ITALIC, 14));
            empty.setForeground(UITheme.TEXT_MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
            listContainer.add(empty);
            return;
        }

        for (MatchHistory m : historyList) {
            listContainer.add(createMatchRow(m));
            listContainer.add(Box.createVerticalStrut(8));
        }
    }

    private JPanel createMatchRow(MatchHistory match) {
        UITheme.RoundedPanel row = new UITheme.RoundedPanel(12, Color.WHITE, UITheme.BORDER, 1);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setPreferredSize(new Dimension(680, 50));
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        String res = match.result() != null ? match.result().toUpperCase() : "";
        boolean isWin = res.contains("WIN") || res.contains("THẮNG");
        boolean isLoss = res.contains("LOSS") || res.contains("THUA");

        String badgeText = isWin ? "Thắng" : (isLoss ? "Thua" : "Hòa");
        Color badgeBg = isWin ? UITheme.WIN_BG : (isLoss ? UITheme.LOSS_BG : UITheme.DRAW_BG);
        Color badgeFg = isWin ? UITheme.WIN_COLOR : (isLoss ? UITheme.LOSS_COLOR : UITheme.DRAW_COLOR);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        left.setOpaque(false);

        UITheme.RoundedPanel pill = new UITheme.RoundedPanel(14, badgeBg, null, 0);
        pill.setPreferredSize(new Dimension(64, 28));
        pill.setLayout(new BorderLayout());
        JLabel pillLbl = new JLabel(badgeText, SwingConstants.CENTER);
        pillLbl.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        pillLbl.setForeground(badgeFg);
        pill.add(pillLbl, BorderLayout.CENTER);
        left.add(pill);

        JLabel opp = new JLabel("vs " + match.opponent());
        opp.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        opp.setForeground(UITheme.TEXT_PRIMARY);
        left.add(opp);
        row.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 12));
        right.setOpaque(false);

        JLabel score = new JLabel(match.capturedCells() + " – " + match.opponentCells() + " quân");
        score.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 13));
        score.setForeground(UITheme.TEXT_MUTED);
        right.add(score);

        JLabel time = new JLabel(match.time());
        time.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        time.setForeground(UITheme.TEXT_MUTED);
        right.add(time);

        JLabel pts = new JLabel(isWin ? "+1.0" : (isLoss ? "+0.0" : "+0.5"));
        pts.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
        pts.setForeground(badgeFg);
        right.add(pts);

        row.add(right, BorderLayout.EAST);
        return row;
    }
}
