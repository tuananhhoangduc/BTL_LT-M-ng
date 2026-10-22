package client;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

final class UITheme {
    // ═══ Modern Palette ════════════════════════════════════════════════════
    static final Color PRIMARY       = new Color(79, 70, 229);   // Indigo 600
    static final Color PRIMARY_DARK  = new Color(67, 56, 202);   // Indigo 700
    static final Color PRIMARY_LIGHT = new Color(99, 102, 241);  // Indigo 500
    static final Color ACCENT        = new Color(14, 165, 233);  // Sky 500

    static final Color GRAD_START    = new Color(79, 70, 229);   // Indigo 600
    static final Color GRAD_END      = new Color(14, 165, 233);  // Sky 500
    static final Color GRAD_PURPLE   = new Color(91, 33, 182);   // Violet 800

    static final Color BG_PAGE       = new Color(241, 245, 249); // Slate 100
    static final Color BG_CARD       = Color.WHITE;

    static final Color TEXT_PRIMARY  = new Color(15, 23, 42);    // Slate 900
    static final Color TEXT_MUTED    = new Color(100, 116, 139); // Slate 500
    static final Color TEXT_LIGHT    = new Color(203, 213, 225); // Slate 300

    static final Color WIN_COLOR     = new Color(16, 185, 129);  // Emerald 500
    static final Color WIN_BG        = new Color(209, 250, 229);  // Emerald 100
    static final Color LOSS_COLOR    = new Color(239, 68, 68);   // Rose 500
    static final Color LOSS_BG       = new Color(254, 226, 226);  // Rose 100
    static final Color DRAW_COLOR    = new Color(245, 158, 11);  // Amber 500
    static final Color DRAW_BG       = new Color(254, 243, 199);  // Amber 100

    static final Color PIECE_RED     = new Color(244, 63, 94);   // Rose 500
    static final Color PIECE_BLUE    = new Color(14, 165, 233);  // Sky 500

    static final Color BORDER        = new Color(226, 232, 240); // Slate 200

    // Typography: Segoe UI
    static final String FONT_FAMILY  = "Segoe UI";
    static final Font FONT_TITLE     = new Font(FONT_FAMILY, Font.BOLD, 22);
    static final Font FONT_HEADER    = new Font(FONT_FAMILY, Font.BOLD, 17);
    static final Font FONT_SUBTITLE  = new Font(FONT_FAMILY, Font.BOLD, 15);
    static final Font FONT_BODY      = new Font(FONT_FAMILY, Font.PLAIN, 14);
    static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    static final Font FONT_SMALL     = new Font(FONT_FAMILY, Font.PLAIN, 12);
    static final Font FONT_SMALL_B   = new Font(FONT_FAMILY, Font.BOLD, 12);

    private UITheme() {}

    static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  VECTOR ICONS (Đảm bảo 100% không bao giờ bị ô vuông lỗi font [])
    // ═══════════════════════════════════════════════════════════════════════
    static Icon createVectorIcon(String type, int size, Color color) {
        return new Icon() {
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);
                g2.translate(x, y);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                float s = size;
                switch (type) {
                    case "home" -> {
                        Path2D p = new Path2D.Float();
                        p.moveTo(s * 0.5f, s * 0.15f);
                        p.lineTo(s * 0.88f, s * 0.48f);
                        p.lineTo(s * 0.78f, s * 0.48f);
                        p.lineTo(s * 0.78f, s * 0.88f);
                        p.lineTo(s * 0.58f, s * 0.88f);
                        p.lineTo(s * 0.58f, s * 0.65f);
                        p.lineTo(s * 0.42f, s * 0.65f);
                        p.lineTo(s * 0.42f, s * 0.88f);
                        p.lineTo(s * 0.22f, s * 0.88f);
                        p.lineTo(s * 0.22f, s * 0.48f);
                        p.lineTo(s * 0.12f, s * 0.48f);
                        p.closePath();
                        g2.fill(p);
                    }
                    case "trophy" -> {
                        // Cup
                        Path2D cup = new Path2D.Float();
                        cup.moveTo(s * 0.25f, s * 0.2f);
                        cup.lineTo(s * 0.75f, s * 0.2f);
                        cup.lineTo(s * 0.68f, s * 0.55f);
                        cup.quadTo(s * 0.5f, s * 0.7f, s * 0.32f, s * 0.55f);
                        cup.closePath();
                        g2.fill(cup);
                        // Handles
                        g2.drawArc((int)(s * 0.12f), (int)(s * 0.22f), (int)(s * 0.22f), (int)(s * 0.3f), 90, 180);
                        g2.drawArc((int)(s * 0.66f), (int)(s * 0.22f), (int)(s * 0.22f), (int)(s * 0.3f), 270, 180);
                        // Base
                        g2.drawLine((int)(s * 0.5f), (int)(s * 0.68f), (int)(s * 0.5f), (int)(s * 0.85f));
                        g2.drawLine((int)(s * 0.32f), (int)(s * 0.85f), (int)(s * 0.68f), (int)(s * 0.85f));
                    }
                    case "history" -> {
                        // Clock / Scroll icon
                        g2.drawOval((int)(s * 0.15f), (int)(s * 0.15f), (int)(s * 0.7f), (int)(s * 0.7f));
                        g2.drawLine((int)(s * 0.5f), (int)(s * 0.5f), (int)(s * 0.5f), (int)(s * 0.32f));
                        g2.drawLine((int)(s * 0.5f), (int)(s * 0.5f), (int)(s * 0.68f), (int)(s * 0.5f));
                    }
                    case "logout" -> {
                        // Exit arrow
                        g2.drawRoundRect((int)(s * 0.15f), (int)(s * 0.15f), (int)(s * 0.42f), (int)(s * 0.7f), 4, 4);
                        g2.drawLine((int)(s * 0.35f), (int)(s * 0.5f), (int)(s * 0.85f), (int)(s * 0.5f));
                        g2.drawLine((int)(s * 0.68f), (int)(s * 0.32f), (int)(s * 0.85f), (int)(s * 0.5f));
                        g2.drawLine((int)(s * 0.68f), (int)(s * 0.68f), (int)(s * 0.85f), (int)(s * 0.5f));
                    }
                    case "star" -> {
                        Path2D star = new Path2D.Float();
                        double cx = s * 0.5, cy = s * 0.5;
                        double rOuter = s * 0.45, rInner = s * 0.2;
                        for (int i = 0; i < 10; i++) {
                            double angle = Math.PI / 2 + i * Math.PI / 5;
                            double r = (i % 2 == 0) ? rOuter : rInner;
                            double px = cx + r * Math.cos(angle);
                            double py = cy - r * Math.sin(angle);
                            if (i == 0) star.moveTo(px, py);
                            else star.lineTo(px, py);
                        }
                        star.closePath();
                        g2.fill(star);
                    }
                    case "eye" -> {
                        Path2D eye = new Path2D.Float();
                        eye.moveTo(s * 0.12f, s * 0.5f);
                        eye.quadTo(s * 0.5f, s * 0.15f, s * 0.88f, s * 0.5f);
                        eye.quadTo(s * 0.5f, s * 0.85f, s * 0.12f, s * 0.5f);
                        eye.closePath();
                        g2.draw(eye);
                        g2.fillOval((int)(s * 0.37f), (int)(s * 0.37f), (int)(s * 0.26f), (int)(s * 0.26f));
                    }
                    default -> g2.fillOval((int)(s * 0.3f), (int)(s * 0.3f), (int)(s * 0.4f), (int)(s * 0.4f));
                }
                g2.dispose();
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GRADIENT PANEL
    // ═══════════════════════════════════════════════════════════════════════
    static class GradientPanel extends JPanel {
        private final Color from, to;
        private final boolean horizontal;

        GradientPanel(Color from, Color to, boolean horizontal) {
            this.from = from;
            this.to = to;
            this.horizontal = horizontal;
            setOpaque(false);
        }

        GradientPanel() { this(GRAD_START, GRAD_END, true); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);
            GradientPaint gp = horizontal
                    ? new GradientPaint(0, 0, from, getWidth(), 0, to)
                    : new GradientPaint(0, 0, from, 0, getHeight(), to);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ROUNDED PANEL WITH MULTI-LAYER SHADOW
    // ═══════════════════════════════════════════════════════════════════════
    static class RoundedPanel extends JPanel {
        protected int radius;
        protected Color bgColor;
        protected Color borderColor;
        protected int borderThick;
        private boolean drawShadow = false;
        private int shadowLayers = 5;

        RoundedPanel(int radius, Color bg, Color border, int borderThick) {
            this.radius = radius;
            this.bgColor = bg;
            this.borderColor = border;
            this.borderThick = borderThick;
            setOpaque(false);
        }

        RoundedPanel(int radius, Color bg) { this(radius, bg, null, 0); }

        void enableShadow(boolean on) { this.drawShadow = on; repaint(); }
        void enableShadow(boolean on, int layers) { this.drawShadow = on; this.shadowLayers = layers; repaint(); }
        void setBgColor(Color c) { this.bgColor = c; repaint(); }
        void setBorderColor(Color c) { this.borderColor = c; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);
            int w = getWidth(), h = getHeight();

            if (drawShadow) {
                for (int i = shadowLayers; i >= 1; i--) {
                    float opacity = (float) (Math.pow((float) (shadowLayers - i + 1) / shadowLayers, 2.0) * 0.035);
                    g2.setColor(new Color(15, 23, 42, Math.min(255, (int)(opacity * 255))));
                    int offset = i;
                    g2.fill(new RoundRectangle2D.Double(
                            offset, offset + 1, w - offset * 2, h - offset * 2, radius + i, radius + i));
                }
            }

            int inset = drawShadow ? shadowLayers : 0;
            int boxW = w - inset * 2;
            int boxH = h - inset * 2;

            if (bgColor != null) {
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(inset, inset, boxW, boxH, radius, radius));
            }

            if (borderColor != null && borderThick > 0) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderThick));
                float off = borderThick / 2.0f;
                g2.draw(new RoundRectangle2D.Double(
                        inset + off, inset + off, boxW - borderThick, boxH - borderThick, radius, radius));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SMOOTH HOVER BUTTON WITH ANIMATION
    // ═══════════════════════════════════════════════════════════════════════
    static class SmoothButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final int cornerRadius;
        private float hoverProgress = 0.0f;
        private Timer animTimer;

        SmoothButton(String text, Color normal, Color hover, Color pressed, int radius) {
            super(text);
            this.normalBg = normal;
            this.hoverBg = hover;
            this.pressedBg = pressed;
            this.cornerRadius = radius;

            setFont(FONT_BODY_BOLD);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    SoundManager.playClick();
                    startAnimation(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    startAnimation(false);
                }
            });
        }

        private void startAnimation(boolean targetHover) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            animTimer = new Timer(15, e -> {
                if (targetHover) {
                    hoverProgress += 0.18f;
                    if (hoverProgress >= 1.0f) {
                        hoverProgress = 1.0f;
                        animTimer.stop();
                    }
                } else {
                    hoverProgress -= 0.18f;
                    if (hoverProgress <= 0.0f) {
                        hoverProgress = 0.0f;
                        animTimer.stop();
                    }
                }
                repaint();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);

            Color currentBg;
            if (getModel().isPressed()) {
                currentBg = pressedBg;
            } else if (!isEnabled()) {
                currentBg = new Color(203, 213, 225);
            } else {
                currentBg = blendColors(normalBg, hoverBg, hoverProgress);
            }

            g2.setColor(currentBg);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

            if (isEnabled()) {
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() / 2.0, cornerRadius, cornerRadius));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ANIMATED BOUNCING DOTS
    // ═══════════════════════════════════════════════════════════════════════
    static class BouncingDots extends JComponent {
        private int tick = 0;
        private final Timer timer;
        private final Color dotColor;

        BouncingDots(Color color) {
            this.dotColor = color;
            setPreferredSize(new Dimension(60, 24));
            setOpaque(false);
            timer = new Timer(45, e -> {
                tick = (tick + 1) % 60;
                repaint();
            });
            timer.start();
        }

        BouncingDots() { this(ACCENT); }

        public void stopAnimation() {
            if (timer != null) timer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int dotSize = 8;
            int gap = 14;

            for (int i = 0; i < 3; i++) {
                double phase = (tick * 0.16) - (i * 0.6);
                double bounce = Math.sin(phase);
                int yOffset = (int) (bounce * 4);
                float alpha = (float) Math.max(0.2, (bounce + 1) / 2);

                int dx = cx + (i - 1) * gap - dotSize / 2;
                int dy = cy + yOffset - dotSize / 2;

                g2.setColor(new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(), (int) (alpha * 255)));
                g2.fillOval(dx, dy, dotSize, dotSize);
            }
            g2.dispose();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SQUIRCLE AVATAR
    // ═══════════════════════════════════════════════════════════════════════
    static JPanel createSquircleAvatar(String name, int size, Color c1, Color c2) {
        return new JPanel() {
            {
                setPreferredSize(new Dimension(size, size));
                setMaximumSize(new Dimension(size, size));
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);

                int arc = size * 35 / 100;
                GradientPaint gp = new GradientPaint(0, 0, c1, size, size, c2);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, size, size, arc, arc));

                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Double(1, 1, size - 2, size - 2, arc, arc));

                g2.setColor(Color.WHITE);
                int fontSize = (int) (size * 0.42);
                g2.setFont(new Font(FONT_FAMILY, Font.BOLD, fontSize));
                String in = (name != null && name.length() >= 2) ? name.substring(0, 2).toUpperCase() : (name != null ? name.toUpperCase() : "U");
                FontMetrics fm = g2.getFontMetrics();
                int sw = fm.stringWidth(in);
                int sh = fm.getAscent() - fm.getDescent();
                g2.drawString(in, (size - sw) / 2, (size + sh) / 2);

                g2.dispose();
            }
        };
    }

    static JPanel createAvatar(String name, int size, Color color) {
        return createSquircleAvatar(name, size, color, color.darker());
    }

    static void styleInput(JTextField field) {
        field.setFont(FONT_BODY);
        field.setBackground(new Color(248, 250, 252));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    }

    static JButton createGradientButton(String text, int width, int height) {
        SmoothButton btn = new SmoothButton(text, PRIMARY, PRIMARY_DARK, PRIMARY_DARK.darker(), 12) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);
                Color c1 = getModel().isPressed() ? PRIMARY_DARK : (getModel().isRollover() ? PRIMARY_LIGHT : PRIMARY);
                Color c2 = getModel().isPressed() ? GRAD_PURPLE.darker() : (getModel().isRollover() ? ACCENT : GRAD_PURPLE);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() / 2.0, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(width, height));
        return btn;
    }

    static Color blendColors(Color c1, Color c2, float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        float ir = 1f - r;
        int red = (int) (c1.getRed() * ir + c2.getRed() * r);
        int green = (int) (c1.getGreen() * ir + c2.getGreen() * r);
        int blue = (int) (c1.getBlue() * ir + c2.getBlue() * r);
        return new Color(red, green, blue);
    }
}
