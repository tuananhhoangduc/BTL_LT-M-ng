package client;

import common.MatchHistory;
import common.PlayerInfo;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class LobbyFrame extends JFrame implements LobbyView {
    private final String username;
    private final List<PlayerInfo> allPlayers = new ArrayList<>();
    private String currentFilter = "ALL";
    private String searchKeyword = "";
    private final java.util.Map<String, JButton> filterButtons = new java.util.HashMap<>();
    private JLabel emptyState;
    private final JLabel requestStatus = new JLabel("Chọn đối thủ trong danh sách để thách đấu.");

    private final JLabel usernameLabel = new JLabel();
    private final JLabel pointsLabel = new JLabel("0.0 đ");
    private final JLabel onlineCountBadge = new JLabel("● 0 đang online");

    private final JTextField searchField = new JTextField();
    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new Object[] {"#", "Tên người chơi", "Điểm", "Trạng thái", "Thao tác"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 4;
                }
            };
    private final JTable playerTable = new JTable(tableModel);

    private Runnable challengeAction = () -> {};
    private java.util.function.Consumer<String> spectateAction = t -> {};
    private Runnable rankingAction = () -> {};
    private Runnable historyAction = () -> {};
    private Runnable logoutAction = () -> {};

    private RankingFrame rankingFrame;
    private HistoryFrame historyFrame;

    LobbyFrame(String username) {
        super(AppBrand.NAME + " - Sảnh chờ online");
        this.username = username;
        SoundManager.scene("menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(
                new Dimension(1000, 660)); // Khóa kích thước tối thiểu đảm bảo không bị méo lệch
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
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.BG_PAGE);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 24, 14, 24));
        requestStatus.setFont(UITheme.FONT_SMALL);
        requestStatus.setForeground(UITheme.TEXT_MUTED);
        footer.add(requestStatus, BorderLayout.CENTER);
        footer.add(
                UITheme.secondaryButton("Cách chơi", () -> HelpDialog.show(this)),
                BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  1. TOP GRADIENT HEADER (Không dùng emoji lỗi font, dùng Vector Icons)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createTopGradientHeader() {
        UITheme.GradientPanel header =
                new UITheme.GradientPanel(UITheme.BG_CARD, UITheme.BG_CARD, true);
        header.setPreferredSize(new Dimension(0, 68));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // Logo
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 16));
        brand.setOpaque(false);

        JLabel logoIcon = new JLabel(UITheme.createVectorIcon("crystal", 24, UITheme.ACCENT));
        logoIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 26));
        logoIcon.setForeground(UITheme.TEXT_PRIMARY);

        JLabel logoText = new JLabel(AppBrand.WORDMARK);
        logoText.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 20));
        logoText.setForeground(UITheme.TEXT_PRIMARY);

        brand.add(logoIcon);
        brand.add(logoText);
        header.add(brand, BorderLayout.WEST);

        // Right side quick stats & actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 15));
        rightPanel.setOpaque(false);

        // Points badge với Star Vector Icon
        JPanel pointsCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        pointsCard.setOpaque(false);
        JLabel starIcon = new JLabel(UITheme.createVectorIcon("star", 16, UITheme.DRAW_COLOR));
        pointsLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 15));
        pointsLabel.setForeground(UITheme.TEXT_PRIMARY);
        pointsCard.add(starIcon);
        pointsCard.add(pointsLabel);

        // Nút Header với Vector Icons
        JButton btnQuickHistory =
                createHeaderPillButton("Lịch sử đấu", "history", () -> historyAction.run());
        JButton btnQuickRanking =
                createHeaderPillButton("Bảng xếp hạng", "trophy", () -> rankingAction.run());

        rightPanel.add(UITheme.secondaryButton("Cài đặt", () -> SettingsDialog.show(this)));
        rightPanel.add(pointsCard);
        rightPanel.add(btnQuickHistory);
        rightPanel.add(btnQuickRanking);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createHeaderPillButton(String text, String iconType, Runnable action) {
        JButton btn =
                new JButton(text, UITheme.createVectorIcon(iconType, 16, UITheme.TEXT_PRIMARY)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        UITheme.applyQualityHints(g2);
                        Color bg =
                                getModel().isRollover()
                                        ? new Color(255, 255, 255, 50)
                                        : new Color(255, 255, 255, 30);
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setIconTextGap(8);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btn.addActionListener(
                e -> {
                    SoundManager.playClick();
                    action.run();
                });
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  2. LEFT SIDEBAR (Căn giữa chuẩn tuyệt đối, không bị lệch trục)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createSidebar() {
        UITheme.RoundedPanel sidebar =
                new UITheme.RoundedPanel(18, UITheme.BG_CARD, UITheme.BORDER, 1);
        sidebar.enableShadow(true);
        sidebar.setLayout(new BorderLayout(0, 18));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 14, 20, 14));

        // 1. TOP PROFILE SECTION (Avatar + Username + Status căn giữa tuyệt đối)
        JPanel profilePanel = new JPanel();
        profilePanel.setOpaque(false);
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));

        JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrap.setOpaque(false);
        avatarWrap.add(UITheme.createSquircleAvatar(username, 68, UITheme.PRIMARY, UITheme.ACCENT));
        avatarWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(avatarWrap);

        profilePanel.add(Box.createVerticalStrut(10));

        usernameLabel.setText(username);
        usernameLabel.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 17));
        usernameLabel.setForeground(UITheme.TEXT_PRIMARY);
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(usernameLabel);

        profilePanel.add(Box.createVerticalStrut(6));

        // Status pill badge bo tròn viền xanh lục
        JPanel statusPill =
                new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        UITheme.applyQualityHints(g2);
                        g2.setColor(UITheme.WIN_BG);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g2.setColor(UITheme.WIN_BG);
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
        statusPill.setOpaque(false);
        statusPill.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPill.setMaximumSize(new Dimension(170, 26));

        JLabel statusTxt = new JLabel("● Sẵn sàng thi đấu", SwingConstants.CENTER);
        statusTxt.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        statusTxt.setForeground(UITheme.WIN_COLOR);
        statusPill.add(statusTxt);

        profilePanel.add(statusPill);

        sidebar.add(profilePanel, BorderLayout.NORTH);

        // 2. CENTER NAVIGATION ITEMS
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        navPanel.add(createSidebarBtn("Sảnh chờ", "home", true, () -> {}));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(createSidebarBtn("Bảng xếp hạng", "trophy", false, () -> rankingAction.run()));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(createSidebarBtn("Lịch sử đấu", "history", false, () -> historyAction.run()));

        sidebar.add(navPanel, BorderLayout.CENTER);

        // 3. BOTTOM LOGOUT
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(
                createSidebarBtn("Đăng xuất", "logout", false, () -> logoutAction.run()),
                BorderLayout.SOUTH);

        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createSidebarBtn(
            String text, String iconType, boolean active, Runnable action) {
        Color iconColor = active ? UITheme.ACCENT : UITheme.TEXT_MUTED;
        JButton btn =
                new JButton(text, UITheme.createVectorIcon(iconType, 18, iconColor)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        UITheme.applyQualityHints(g2);
                        if (active) {
                            g2.setColor(UITheme.BG_ELEVATED);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                            g2.setColor(UITheme.PRIMARY);
                            g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
                        } else if (getModel().isRollover()) {
                            g2.setColor(UITheme.BG_PAGE);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        }
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
        btn.setFont(new Font(UITheme.FONT_FAMILY, active ? Font.BOLD : Font.PLAIN, 14));
        btn.setForeground(active ? UITheme.ACCENT : UITheme.TEXT_MUTED);
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
        btn.addActionListener(
                e -> {
                    SoundManager.playClick();
                    action.run();
                });
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  3. MAIN TABLE CARD (Layout cố định, không đè lấn khi Zoom)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel createTableCard() {
        UITheme.RoundedPanel card =
                new UITheme.RoundedPanel(18, UITheme.BG_CARD, UITheme.BORDER, 1);
        card.enableShadow(true);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Subheader Layout cố định tách biệt 2 bên bằng BorderLayout
        JPanel subHeader = new JPanel(new BorderLayout(14, 0));
        subHeader.setOpaque(false);
        subHeader.setPreferredSize(new Dimension(0, 36));

        // Bên trái: Tiêu đề + Badge online
        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftTitle.setOpaque(false);

        JLabel tableTitle = new JLabel("Danh sách người chơi");
        tableTitle.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 17));
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);

        onlineCountBadge.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
        onlineCountBadge.setForeground(UITheme.WIN_COLOR);
        onlineCountBadge.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.WIN_BG, 1),
                        BorderFactory.createEmptyBorder(3, 10, 3, 10)));

        leftTitle.add(tableTitle);
        leftTitle.add(onlineCountBadge);
        subHeader.add(leftTitle, BorderLayout.WEST);

        // Bên phải: Search Box + Filter Chips
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        searchField.getAccessibleContext().setAccessibleName("Tìm người chơi");
        searchField.setPreferredSize(new Dimension(140, 32));
        searchField.setToolTipText("Tìm người chơi...");
        UITheme.styleInput(searchField);
        searchField.addCaretListener(
                e -> {
                    searchKeyword = searchField.getText().trim().toLowerCase();
                    updateFilteredTable();
                });

        JButton btnAll = createFilterChip("Tất cả", "ALL");
        JButton btnFree = createFilterChip("Đang rỗi", "FREE");
        JButton btnBusy = createFilterChip("Đang bận", "BUSY");

        rightControls.add(searchField);
        rightControls.add(btnAll);
        rightControls.add(btnFree);
        rightControls.add(btnBusy);
        subHeader.add(rightControls, BorderLayout.EAST);

        card.add(subHeader, BorderLayout.NORTH);

        // Table
        setupPlayerTable();
        JScrollPane scroll = new JScrollPane(playerTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);
        emptyState =
                new JLabel(
                        "Chưa có người chơi phù hợp. Kiểm tra bộ lọc hoặc mời bạn cùng vào"
                            + " server.");
        emptyState.setForeground(UITheme.TEXT_MUTED);
        emptyState.setFont(UITheme.FONT_SMALL);
        emptyState.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        card.add(emptyState, BorderLayout.SOUTH);

        return card;
    }

    private JButton createFilterChip(String text, String filter) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        filterButtons.put(filter, btn);
        boolean sel = currentFilter.equals(filter);
        btn.setBackground(sel ? UITheme.PRIMARY : UITheme.BG_CARD);
        btn.setForeground(sel ? UITheme.TEXT_PRIMARY : UITheme.TEXT_MUTED);
        btn.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(sel ? UITheme.PRIMARY : UITheme.BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btn.addActionListener(
                e -> {
                    SoundManager.playClick();
                    currentFilter = filter;
                    filterButtons.forEach(
                            (key, button) -> {
                                boolean selected = currentFilter.equals(key);
                                button.setBackground(selected ? UITheme.PRIMARY : UITheme.BG_CARD);
                                button.setForeground(
                                        selected ? UITheme.TEXT_PRIMARY : UITheme.TEXT_MUTED);
                                button.setBorder(
                                        BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                        selected
                                                                ? UITheme.PRIMARY
                                                                : UITheme.BORDER),
                                                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                            });
                    updateFilteredTable();
                });
        return btn;
    }

    private void setupPlayerTable() {
        playerTable.setRowHeight(48);
        playerTable.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 14));
        playerTable.getTableHeader().setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        playerTable.getTableHeader().setBackground(UITheme.BG_CARD);
        playerTable.getTableHeader().setForeground(UITheme.TEXT_MUTED);
        playerTable.getTableHeader().setPreferredSize(new Dimension(100, 38));
        playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerTable.setShowVerticalLines(false);
        playerTable.setGridColor(UITheme.BG_PAGE);
        playerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Căn chỉnh tiêu đề bảng đồng bộ 100% với nội dung bên dưới (khung chữ thẳng tắp)
        playerTable
                .getTableHeader()
                .setDefaultRenderer(
                        new DefaultTableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(
                                    JTable table,
                                    Object value,
                                    boolean isSelected,
                                    boolean hasFocus,
                                    int row,
                                    int col) {
                                JLabel l =
                                        (JLabel)
                                                super.getTableCellRendererComponent(
                                                        table,
                                                        value,
                                                        isSelected,
                                                        hasFocus,
                                                        row,
                                                        col);
                                l.setBackground(UITheme.BG_CARD);
                                l.setForeground(UITheme.TEXT_MUTED);
                                l.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
                                l.setBorder(
                                        BorderFactory.createCompoundBorder(
                                                BorderFactory.createMatteBorder(
                                                        0, 0, 1, 0, UITheme.BORDER),
                                                BorderFactory.createEmptyBorder(
                                                        0, col == 1 ? 16 : 0, 0, 0)));
                                l.setHorizontalAlignment(
                                        col == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                                return l;
                            }
                        });

        playerTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        playerTable.getColumnModel().getColumn(0).setMaxWidth(80);
        playerTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        playerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        playerTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        playerTable.getColumnModel().getColumn(4).setPreferredWidth(140);

        playerTable.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int col) {
                        Component c =
                                super.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, col);
                        String userInRow = (String) table.getValueAt(row, 1);
                        boolean isMe = userInRow != null && userInRow.contains("(Bạn)");

                        if (isSelected) {
                            c.setBackground(UITheme.BG_ELEVATED);
                        } else if (isMe) {
                            c.setBackground(UITheme.BG_CARD);
                        } else {
                            c.setBackground(row % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_CARD);
                        }
                        c.setForeground(UITheme.TEXT_PRIMARY);

                        if (col == 0) {
                            setHorizontalAlignment(CENTER);
                            setBorder(BorderFactory.createEmptyBorder());
                            setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 13));
                            setForeground(UITheme.TEXT_MUTED);
                        } else if (col == 1) {
                            setHorizontalAlignment(LEFT);
                            setBorder(
                                    BorderFactory.createEmptyBorder(
                                            0, 16, 0, 8)); // Khớp với lề của header
                            setFont(
                                    new Font(
                                            UITheme.FONT_FAMILY,
                                            isMe ? Font.BOLD : Font.PLAIN,
                                            14));
                            if (isMe) setForeground(UITheme.ACCENT);
                        } else if (col == 2) {
                            setHorizontalAlignment(CENTER);
                            setBorder(BorderFactory.createEmptyBorder());
                            setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
                        } else if (col == 3) {
                            setHorizontalAlignment(CENTER);
                            setBorder(BorderFactory.createEmptyBorder());
                            boolean free = "Đang rỗi".equals(value);
                            setText(free ? "● Đang rỗi" : "● Đang thi đấu");
                            setForeground(free ? UITheme.WIN_COLOR : UITheme.DRAW_COLOR);
                            setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
                        }
                        return c;
                    }
                });

        playerTable
                .getColumnModel()
                .getColumn(4)
                .setCellRenderer(
                        (table, value, isSelected, hasFocus, row, col) -> {
                            String target = (String) table.getValueAt(row, 1);
                            boolean isMe = target != null && target.contains("(Bạn)");
                            boolean isBusy =
                                    "Đang thi đấu".equals(table.getValueAt(row, 3))
                                            || "Xem trận".equals(value);

                            UITheme.SmoothButton btn;
                            if (isBusy) {
                                btn =
                                        new UITheme.SmoothButton(
                                                "Xem trận",
                                                UITheme.PRIMARY,
                                                UITheme.ACCENT,
                                                UITheme.PRIMARY_DARK,
                                                10);
                                btn.setIcon(
                                        UITheme.createVectorIcon("eye", 14, UITheme.TEXT_PRIMARY));
                                btn.setIconTextGap(6);
                            } else {
                                btn =
                                        new UITheme.SmoothButton(
                                                "Thách đấu",
                                                UITheme.PRIMARY,
                                                UITheme.BG_CARD,
                                                UITheme.PRIMARY_DARK,
                                                10);
                            }
                            btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
                            btn.setEnabled(!isMe);
                            btn.setPreferredSize(new Dimension(112, 32));

                            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                            p.setOpaque(false);
                            p.add(btn);
                            return p;
                        });

        playerTable
                .getColumnModel()
                .getColumn(4)
                .setCellEditor(
                        new DefaultCellEditor(new JCheckBox()) {
                            private final UITheme.SmoothButton btnChallenge =
                                    new UITheme.SmoothButton(
                                            "Thách đấu",
                                            UITheme.PRIMARY,
                                            UITheme.BG_CARD,
                                            UITheme.PRIMARY_DARK,
                                            10);
                            private final UITheme.SmoothButton btnWatch =
                                    new UITheme.SmoothButton(
                                            "Xem trận",
                                            UITheme.PRIMARY,
                                            UITheme.ACCENT,
                                            UITheme.PRIMARY_DARK,
                                            10);
                            private int currentRow;
                            private boolean currentBusy;

                            {
                                btnChallenge.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
                                btnWatch.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 12));
                                btnWatch.setIcon(
                                        UITheme.createVectorIcon("eye", 14, UITheme.TEXT_PRIMARY));
                                btnWatch.setIconTextGap(6);

                                btnChallenge.addActionListener(
                                        e -> {
                                            fireEditingStopped();
                                            playerTable.setRowSelectionInterval(
                                                    currentRow, currentRow);
                                            challengeAction.run();
                                        });
                                btnWatch.addActionListener(
                                        e -> {
                                            fireEditingStopped();
                                            playerTable.setRowSelectionInterval(
                                                    currentRow, currentRow);
                                            String target =
                                                    (String) tableModel.getValueAt(currentRow, 1);
                                            if (target != null) {
                                                if (target.endsWith(" (Bạn)"))
                                                    target =
                                                            target.substring(
                                                                    0, target.length() - 6);
                                                spectateAction.accept(target);
                                            }
                                        });
                            }

                            @Override
                            public Component getTableCellEditorComponent(
                                    JTable table,
                                    Object value,
                                    boolean isSelected,
                                    int row,
                                    int col) {
                                this.currentRow = row;
                                String target = (String) table.getValueAt(row, 1);
                                boolean isMe = target != null && target.contains("(Bạn)");
                                this.currentBusy =
                                        "Đang thi đấu".equals(table.getValueAt(row, 3))
                                                || "Xem trận".equals(value);

                                UITheme.SmoothButton btn = currentBusy ? btnWatch : btnChallenge;
                                btn.setEnabled(!isMe);
                                btn.setPreferredSize(new Dimension(112, 32));

                                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                                p.setOpaque(false);
                                p.add(btn);
                                return p;
                            }

                            @Override
                            public Object getCellEditorValue() {
                                return currentBusy ? "Xem trận" : "Thách đấu";
                            }
                        });
    }

    private void updateFilteredTable() {
        if (playerTable.isEditing()) playerTable.getCellEditor().cancelCellEditing();
        tableModel.setRowCount(0);
        int index = 1;

        for (PlayerInfo p : allPlayers) {
            String name = p.username();
            if (!searchKeyword.isEmpty() && !name.toLowerCase().contains(searchKeyword)) {
                continue;
            }
            boolean busy = p.busy();
            if ("FREE".equals(currentFilter) && busy) continue;
            if ("BUSY".equals(currentFilter) && !busy) continue;

            boolean isMe = username.equals(name);
            tableModel.addRow(
                    new Object[] {
                        index++,
                        name + (isMe ? " (Bạn)" : ""),
                        String.format("%.1f đ", p.points()),
                        busy ? "Đang thi đấu" : "Đang rỗi",
                        busy ? "Xem trận" : "Thách đấu"
                    });
        }
        if (emptyState != null) emptyState.setVisible(tableModel.getRowCount() == 0);
        onlineCountBadge.setText("● " + allPlayers.size() + " đang online");
    }

    @Override
    public PlayerInfo selectedPlayer() {
        int row = playerTable.getSelectedRow();
        if (row < 0) return null;
        String name = (String) tableModel.getValueAt(row, 1);
        if (name != null && name.endsWith(" (Bạn)")) {
            name = name.substring(0, name.length() - 6);
        }
        for (PlayerInfo p : allPlayers) {
            if (p.username().equals(name)) return p;
        }
        return null;
    }

    @Override
    public void challengeStatus(String text, boolean waiting) {
        requestStatus.setText(text == null ? " " : text);
        requestStatus.setToolTipText(text);
        requestStatus.setForeground(waiting ? UITheme.ACCENT : UITheme.TEXT_MUTED);
        playerTable.setEnabled(!waiting);
    }

    @Override
    public void connectionLost(Runnable login) {
        if (rankingFrame != null) rankingFrame.dispose();
        if (historyFrame != null) historyFrame.dispose();
        playerTable.setEnabled(false);
        challengeStatus("Mất kết nối máy chủ. Đăng nhập lại để tiếp tục.", true);
        if (ClientDialog.ask(
                this,
                "Mất kết nối",
                "Kết nối máy chủ đã đóng. Trở về đăng nhập để kết nối lại.",
                "Về đăng nhập",
                "Đóng thông báo")) login.run();
    }

    @Override
    public void setPlayers(List<PlayerInfo> values) {
        SwingUtilities.invokeLater(
                () -> {
                    allPlayers.clear();
                    if (values != null) allPlayers.addAll(values);
                    for (PlayerInfo p : allPlayers) {
                        if (username.equals(p.username())) {
                            pointsLabel.setText(String.format("%.1f đ", p.points()));
                        }
                    }
                    updateFilteredTable();
                });
    }

    @Override
    public void onChallenge(Runnable action) {
        this.challengeAction = action;
    }

    @Override
    public void onSpectate(java.util.function.Consumer<String> action) {
        this.spectateAction = action;
    }

    @Override
    public void onRanking(Runnable action) {
        this.rankingAction = action;
    }

    @Override
    public void onHistory(Runnable action) {
        this.historyAction = action;
    }

    @Override
    public void onLogout(Runnable action) {
        this.logoutAction = action;
    }

    @Override
    public boolean confirmChallenge(String challenger) {
        return ClientDialog.ask(
                this, "Lời thách đấu", challenger + " thách đấu bạn!", "Đồng ý", "Từ chối");
    }

    @Override
    public void showRanking(List<PlayerInfo> ranking, Runnable historyAction) {
        if (rankingFrame != null && rankingFrame.isDisplayable()) rankingFrame.dispose();
        setVisible(false);
        rankingFrame = new RankingFrame(ranking, username, historyAction);
        rankingFrame.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        LobbyFrame.this.setVisible(true);
                    }
                });
        rankingFrame.setVisible(true);
    }

    @Override
    public void showHistory(List<MatchHistory> history) {
        if (historyFrame != null && historyFrame.isDisplayable()) historyFrame.dispose();
        setVisible(false);
        historyFrame = new HistoryFrame(username, history);
        historyFrame.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        LobbyFrame.this.setVisible(true);
                    }
                });
        historyFrame.setVisible(true);
    }

    @Override
    public void showMessage(String message, boolean error) {
        JOptionPane.showMessageDialog(
                this,
                message,
                error ? "Lỗi" : "Thông báo",
                error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void close() {
        if (rankingFrame != null) rankingFrame.dispose();
        if (historyFrame != null) historyFrame.dispose();
        dispose();
    }
}
