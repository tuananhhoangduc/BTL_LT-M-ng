package client;

import common.MatchHistory;
import common.PlayerInfo;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class RankingFrame extends JFrame {
    private final List<PlayerInfo> originalList;
    private final String currentUsername;
    private final Runnable historyAction;

    private boolean sortByPoints = true;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"#", "Tên người chơi", "Điểm", "Trận thắng", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable rankingTable = new JTable(tableModel);

    private final JButton tabPoints = new JButton("Theo điểm");
    private final JButton tabWins   = new JButton("Theo số trận thắng");

    RankingFrame(List<PlayerInfo> ranking, Runnable historyAction) {
        this(ranking, null, historyAction);
    }

    RankingFrame(List<PlayerInfo> ranking, String currentUsername, Runnable historyAction) {
        super("Color Match Arena - Bảng xếp hạng");
        this.originalList = ranking != null ? new ArrayList<>(ranking) : new ArrayList<>();
        this.currentUsername = currentUsername;
        this.historyAction = historyAction;

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
        centerPanel.add(createTableCard(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createTopGradientHeader() {
        UITheme.GradientPanel header = new UITheme.GradientPanel(
                new Color(67, 56, 202), new Color(14, 165, 233), true);
        header.setPreferredSize(new Dimension(0, 68));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        brand.setOpaque(false);
        JLabel trophyIcon = new JLabel(UITheme.createVectorIcon("trophy", 24, Color.WHITE));
        JLabel logo = new JLabel("BẢNG XẾP HẠNG ANH HÙNG");
        logo.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 19));
        logo.setForeground(Color.WHITE);
        brand.add(trophyIcon);
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

        String name = currentUsername != null ? currentUsername : "Player";

        // 1. TOP PROFILE SECTION (Căn giữa tuyệt đối)
        JPanel profilePanel = new JPanel();
        profilePanel.setOpaque(false);
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));

        JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrap.setOpaque(false);
        avatarWrap.add(UITheme.createSquircleAvatar(name, 68, UITheme.PRIMARY, UITheme.ACCENT));
        avatarWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(avatarWrap);

        profilePanel.add(Box.createVerticalStrut(10));

        JLabel nameLbl = new JLabel(name);
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
                g2.setColor(UITheme.WIN_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(167, 243, 208));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusPill.setOpaque(false);
        statusPill.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPill.setMaximumSize(new Dimension(170, 26));

        JLabel statusTxt = new JLabel("● Đang trực tuyến", SwingConstants.CENTER);
        statusTxt.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        statusTxt.setForeground(new Color(5, 150, 105));
        statusPill.add(statusTxt);

        profilePanel.add(statusPill);

        sidebar.add(profilePanel, BorderLayout.NORTH);

        // 2. CENTER NAVIGATION ITEMS
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        navPanel.add(createSidebarBtn("Sảnh chờ", "home", false, this::dispose));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(createSidebarBtn("Bảng xếp hạng", "trophy", true, () -> {}));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(createSidebarBtn("Lịch sử đấu", "history", false, () -> {
            dispose();
            if (historyAction != null) historyAction.run();
        }));

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

    private JPanel createTableCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel(18, Color.WHITE, UITheme.BORDER, 1);
        card.enableShadow(true);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 36));

        JLabel title = new JLabel("Top cao thủ Color Match");
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 17));
        title.setForeground(UITheme.TEXT_PRIMARY);
        topBar.add(title, BorderLayout.WEST);

        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tabRow.setOpaque(false);

        styleTab(tabPoints, true);
        styleTab(tabWins, false);

        tabPoints.addActionListener(e -> {
            SoundManager.playClick();
            if (!sortByPoints) {
                sortByPoints = true;
                styleTab(tabPoints, true);
                styleTab(tabWins, false);
                refreshData();
            }
        });
        tabWins.addActionListener(e -> {
            SoundManager.playClick();
            if (sortByPoints) {
                sortByPoints = false;
                styleTab(tabPoints, false);
                styleTab(tabWins, true);
                refreshData();
            }
        });

        tabRow.add(tabPoints);
        tabRow.add(tabWins);
        topBar.add(tabRow, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        setupTable();
        JScrollPane scroll = new JScrollPane(rankingTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void styleTab(JButton btn, boolean active) {
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(active ? UITheme.PRIMARY : Color.WHITE);
        btn.setForeground(active ? Color.WHITE : UITheme.TEXT_MUTED);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? UITheme.PRIMARY : UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
    }

    private void setupTable() {
        rankingTable.setRowHeight(48);
        rankingTable.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        rankingTable.getTableHeader().setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        rankingTable.getTableHeader().setBackground(new Color(248, 250, 252));
        rankingTable.getTableHeader().setForeground(UITheme.TEXT_MUTED);
        rankingTable.getTableHeader().setPreferredSize(new Dimension(100, 38));
        rankingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rankingTable.setShowVerticalLines(false);
        rankingTable.setGridColor(new Color(241, 245, 249));
        rankingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Căn chỉnh tiêu đề bảng đồng bộ 100% với nội dung bên dưới (khung chữ thẳng tắp)
        rankingTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setBackground(new Color(248, 250, 252));
                l.setForeground(UITheme.TEXT_MUTED);
                l.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                        BorderFactory.createEmptyBorder(0, col == 1 ? 16 : 0, 0, 0)
                ));
                l.setHorizontalAlignment(col == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                return l;
            }
        });

        rankingTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        rankingTable.getColumnModel().getColumn(0).setMaxWidth(80);
        rankingTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        rankingTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        rankingTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        rankingTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        rankingTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, col);
                String u = (String) table.getValueAt(row, 1);
                boolean isMe = currentUsername != null && u != null && u.contains("(Bạn)");

                if (selected) c.setBackground(new Color(224, 231, 255));
                else if (isMe) c.setBackground(new Color(245, 243, 255));
                else c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                c.setForeground(UITheme.TEXT_PRIMARY);

                if (col == 0) {
                    setHorizontalAlignment(CENTER);
                    setBorder(BorderFactory.createEmptyBorder());
                    setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
                    if (row == 0) setForeground(new Color(234, 179, 8));
                    else if (row == 1) setForeground(new Color(148, 163, 184));
                    else if (row == 2) setForeground(new Color(205, 127, 50));
                    else setForeground(UITheme.TEXT_MUTED);
                } else if (col == 1) {
                    setHorizontalAlignment(LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8)); // Khớp với lề của header
                    setFont(new Font(UITheme.FONT_FAMILY, isMe ? Font.BOLD : Font.PLAIN, 14));
                    if (isMe) setForeground(UITheme.PRIMARY);
                } else if (col == 2 || col == 3) {
                    setHorizontalAlignment(CENTER);
                    setBorder(BorderFactory.createEmptyBorder());
                    setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
                } else if (col == 4) {
                    setHorizontalAlignment(CENTER);
                    setBorder(BorderFactory.createEmptyBorder());
                    setForeground(UITheme.WIN_COLOR);
                    setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
                }
                return c;
            }
        });
    }

    private void refreshData() {
        List<PlayerInfo> sorted = new ArrayList<>(originalList);
        if (sortByPoints) {
            sorted.sort(Comparator.comparingDouble(PlayerInfo::points).reversed()
                    .thenComparing(Comparator.comparingInt(PlayerInfo::wins).reversed()));
        } else {
            sorted.sort(Comparator.comparingInt(PlayerInfo::wins).reversed()
                    .thenComparing(Comparator.comparingDouble(PlayerInfo::points).reversed()));
        }

        tableModel.setRowCount(0);
        int rank = 1;
        for (PlayerInfo p : sorted) {
            boolean isMe = currentUsername != null && currentUsername.equals(p.username());
            String medal = rank == 1 ? "1" : (rank == 2 ? "2" : (rank == 3 ? "3" : String.valueOf(rank)));
            tableModel.addRow(new Object[]{
                    medal,
                    p.username() + (isMe ? " (Bạn)" : ""),
                    String.format("%.1f đ", p.points()),
                    p.wins() + " trận",
                    "● Online"
            });
            rank++;
        }
    }

    void showHistory(List<MatchHistory> values) {}
}
