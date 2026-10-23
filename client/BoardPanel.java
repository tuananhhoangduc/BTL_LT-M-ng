package client;

import common.BoardState;
import common.CaptureRay;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

import javax.swing.*;

/** One animation clock for the whole board; authoritative state is never delayed by animation. */
final class BoardPanel extends JComponent {
    private BoardState state;
    private BoardState effectBefore;
    private String ownColor = "RED";
    private final boolean spectator;
    private boolean pending, active = true;
    private int pendingCell = -1;
    private int hover = -1, keyboard = 40;
    private long effectStarted;
    private long hoverStarted;
    private final LongSupplier time;
    private final BooleanSupplier reducedMotion;
    private boolean animateMove;
    private final Timer clock = new Timer(16, e -> advanceAnimation());
    private BiConsumer<Integer, Integer> action = (r, c) -> {};

    void advanceAnimation() {
        if (reducedMotion.getAsBoolean() || elapsed() >= BoardMotion.EFFECT_SECONDS) {
            animateMove = false;
            effectBefore = null;
        }
        if (!animateMove && hoverElapsed() >= BoardMotion.HOVER_SECONDS) clock.stop();
        repaint();
    }

    BoardPanel(boolean spectator) {
        this(spectator, System::nanoTime);
    }

    BoardPanel(boolean spectator, LongSupplier time) {
        this(spectator, time, ClientSettings::reducedMotion);
    }

    BoardPanel(boolean spectator, LongSupplier time, BooleanSupplier reducedMotion) {
        this.spectator = spectator;
        this.time = time;
        this.reducedMotion = reducedMotion;
        setFocusable(true);
        setPreferredSize(new Dimension(560, 560));
        getAccessibleContext()
                .setAccessibleName("Bàn cờ 9 nhân 9. Dùng phím mũi tên, Enter để đặt quân.");
        MouseAdapter mouse =
                new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        setHover(hit(e.getX(), e.getY()));
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        setHover(-1);
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        requestFocusInWindow();
                        int cell = hit(e.getX(), e.getY());
                        if (cell >= 0) {
                            keyboard = cell;
                            submit(cell);
                        }
                    }
                };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        bind("LEFT", () -> navigate(0, -1));
        bind("RIGHT", () -> navigate(0, 1));
        bind("UP", () -> navigate(-1, 0));
        bind("DOWN", () -> navigate(1, 0));
        bind("ENTER", () -> submit(keyboard));
        bind("SPACE", () -> submit(keyboard));
    }

    private void bind(String key, Runnable run) {
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key), key);
        getActionMap()
                .put(
                        key,
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                run.run();
                            }
                        });
    }

    private void navigate(int dr, int dc) {
        int r = Math.max(0, Math.min(8, keyboard / 9 + dr)),
                c = Math.max(0, Math.min(8, keyboard % 9 + dc));
        keyboard = r * 9 + c;
        hover = -1;
        hoverStarted = time.getAsLong();
        if (!reducedMotion.getAsBoolean()) clock.restart();
        getAccessibleContext()
                .setAccessibleDescription("Hàng " + (r + 1) + ", cột " + (char) ('A' + c));
        repaint();
    }

    private void setHover(int cell) {
        if (hover == cell) return;
        hover = cell;
        hoverStarted = time.getAsLong();
        if (!reducedMotion.getAsBoolean()) clock.restart();
    }

    private double hoverElapsed() {
        return (time.getAsLong() - hoverStarted) / 1_000_000_000.0;
    }

    void onMove(BiConsumer<Integer, Integer> action) {
        this.action = action;
    }

    @Override
    public javax.accessibility.AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) accessibleContext = new AccessibleJComponent() {};
        return accessibleContext;
    }

    private boolean legal(int index) {
        if (state == null || !active || pending || spectator || !ownColor.equals(state.turnColor))
            return false;
        int value = state.cells[index / 9][index % 9];
        return value == 0 || value == ("RED".equals(ownColor) ? 3 : 4);
    }

    private void submit(int index) {
        if (!legal(index)) return;
        pending = true;
        pendingCell = index;
        repaint();
        action.accept(index / 9, index % 9);
    }

    void releasePending() {
        pending = false;
        pendingCell = -1;
        repaint();
    }

    void active(boolean value) {
        active = value;
        if (!value) releasePending();
        repaint();
    }

    boolean isPending() {
        return pending;
    }

    void update(BoardState next, String own, BoardFeedback.Change change) {
        BoardState before = state;
        state = next;
        ownColor = own;
        if (change.reset() || change.move() || change.turn()) releasePending();
        if (change.reset()) stopEffect();
        if (change.move() && !reducedMotion.getAsBoolean()) {
            effectBefore = before;
            effectStarted = time.getAsLong();
            animateMove = true;
            clock.restart();
        }
        repaint();
    }

    boolean animationRunning() {
        return clock.isRunning();
    }

    void stopEffect() {
        clock.stop();
        animateMove = false;
        effectBefore = null;
    }

    private boolean sourceCell(int row, int column) {
        if (effectBefore == null || state == null || state.captureRays.isEmpty()) return false;
        boolean stone =
                effectBefore.cells[row][column] == state.moveColor
                        || (row == state.moveRow && column == state.moveColumn);
        if (!stone) return false;
        for (CaptureRay ray : state.captureRays) {
            if (BoardMotion.onRay(ray, row, column)) return true;
        }
        return false;
    }

    @Override
    public void removeNotify() {
        stopEffect();
        super.removeNotify();
    }

    private double elapsed() {
        return (time.getAsLong() - effectStarted) / 1_000_000_000.0;
    }

    private Rectangle square() {
        int n = Math.max(0, Math.min(getWidth(), getHeight()) - 34);
        return new Rectangle((getWidth() - n) / 2, (getHeight() - n) / 2, n, n);
    }

    private int hit(int x, int y) {
        Rectangle b = square();
        if (!b.contains(x, y) || b.width == 0) return -1;
        return ((y - b.y) * 9 / b.height) * 9 + (x - b.x) * 9 / b.width;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        UITheme.applyQualityHints(g);
        Rectangle b = square();
        double tile = b.width / 9.0;
        g.setColor(UITheme.TEXT_MUTED);
        g.setFont(UITheme.FONT_SMALL);
        for (int i = 0; i < 9; i++) {
            g.drawString(
                    String.valueOf((char) ('A' + i)), (float) (b.x + (i + .5) * tile - 4), b.y - 5);
            g.drawString(String.valueOf(i + 1), b.x - 13, (float) (b.y + (i + .5) * tile + 4));
        }
        int selected = hover >= 0 ? hover : isFocusOwner() ? keyboard : -1;
        double age = animateMove ? elapsed() : 1;
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++) {
                double x = b.x + c * tile + 2, y = b.y + r * tile + 2, n = tile - 4;
                int value = state == null ? 0 : state.cells[r][c],
                        area = state == null ? 0 : state.areas[r][c];
                Color fill =
                        area == 3
                                ? UITheme.RED_AREA
                                : area == 4 ? UITheme.BLUE_AREA : UITheme.BG_CARD;
                if (selected == r * 9 + c && legal(selected))
                    fill = UITheme.blendColors(fill, UITheme.BG_ELEVATED, .8f);
                RoundRectangle2D cell = new RoundRectangle2D.Double(x, y, n, n, 6, 6);
                g.setColor(fill);
                g.fill(cell);
                SpriteCatalog.draw(
                        g,
                        area == 3 ? "tile.red" : area == 4 ? "tile.blue" : "tile.neutral",
                        x,
                        y,
                        n,
                        n);
                if (animateMove && effectBefore != null && effectBefore.areas[r][c] != area) {
                    Composite saved = g.getComposite();
                    g.setComposite(
                            AlphaComposite.SrcOver.derive(
                                    (float) (1 - BoardMotion.areaBlend(age))));
                    int oldArea = effectBefore.areas[r][c];
                    g.setColor(
                            oldArea == 3
                                    ? UITheme.RED_AREA
                                    : oldArea == 4 ? UITheme.BLUE_AREA : UITheme.BG_CARD);
                    g.fill(cell);
                    SpriteCatalog.draw(
                            g,
                            oldArea == 3 ? "tile.red" : oldArea == 4 ? "tile.blue" : "tile.neutral",
                            x,
                            y,
                            n,
                            n);
                    g.setComposite(saved);
                }
                g.setColor(
                        area == 3
                                ? UITheme.PIECE_RED.darker()
                                : area == 4 ? UITheme.PIECE_BLUE.darker() : UITheme.BORDER);
                g.setStroke(new BasicStroke(1));
                g.draw(cell);
                if ((area == 3 || area == 4)
                        && !SpriteCatalog.available(area == 3 ? "tile.red" : "tile.blue")) {
                    g.setColor(
                            area == 3 ? new Color(255, 99, 120, 55) : new Color(87, 200, 255, 55));
                    if (area == 3) {
                        g.draw(new Line2D.Double(x + 6, y + n - 6, x + n - 6, y + 6));
                    } else {
                        g.draw(new Line2D.Double(x + 6, y + 6, x + n - 6, y + n - 6));
                    }
                }
                boolean latest = state != null && r == state.moveRow && c == state.moveColumn;
                if (latest) {
                    g.setColor(state.moveColor == 1 ? UITheme.PIECE_RED : UITheme.PIECE_BLUE);
                    g.setStroke(new BasicStroke(1.5f));
                    g.draw(new RoundRectangle2D.Double(x + 3, y + 3, n - 6, n - 6, 5, 5));
                }
                if (value == 1 || value == 2) {
                    double progress = Math.min(1, age / .18);
                    double scale =
                            latest && age < .18 ? .62 + .38 * (1 - Math.pow(1 - progress, 3)) : 1;
                    ArenaArt.crystal(g, x + n / 2, y + n / 2, n * .86 * scale, value);
                } else if ((selected == r * 9 + c && legal(selected))
                        || (pending && pendingCell == r * 9 + c)) {
                    Composite old = g.getComposite();
                    float hoverAlpha =
                            reducedMotion.getAsBoolean()
                                    ? 1
                                    : (float)
                                            BoardMotion.ease(
                                                    hoverElapsed() / BoardMotion.HOVER_SECONDS);
                    g.setComposite(
                            AlphaComposite.SrcOver.derive(pending ? .35f : .25f * hoverAlpha));
                    ArenaArt.crystal(
                            g, x + n / 2, y + n / 2, n * .6, "RED".equals(ownColor) ? 1 : 2);
                    g.setComposite(old);
                }
                if (animateMove && age < .12 && sourceCell(r, c)) {
                    Composite composite = g.getComposite();
                    g.setComposite(
                            AlphaComposite.SrcOver.derive((float) BoardMotion.sourceAlpha(age)));
                    ArenaArt.crystal(g, x + n / 2, y + n / 2, n * .86, state.moveColor);
                    g.setComposite(composite);
                }
                if (selected == r * 9 + c) {
                    g.setColor(
                            legal(selected)
                                    ? ("RED".equals(ownColor)
                                            ? UITheme.PIECE_RED
                                            : UITheme.PIECE_BLUE)
                                    : UITheme.TEXT_MUTED);
                    g.setStroke(new BasicStroke(1.5f));
                    g.draw(cell);
                }
                if (pending && pendingCell == r * 9 + c) {
                    g.setColor("RED".equals(ownColor) ? UITheme.PIECE_RED : UITheme.PIECE_BLUE);
                    g.setStroke(
                            new BasicStroke(
                                    1.8f,
                                    BasicStroke.CAP_BUTT,
                                    BasicStroke.JOIN_ROUND,
                                    1f,
                                    new float[] {4, 3},
                                    0));
                    g.draw(cell);
                }
            }
        if (animateMove && !reducedMotion.getAsBoolean() && state != null && age < .43) {
            Color tint = state.moveColor == 1 ? UITheme.PIECE_RED : UITheme.PIECE_BLUE;
            float alpha = (float) BoardMotion.beamAlpha(age);
            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            for (CaptureRay ray : state.captureRays) {
                double x1 = b.x + (ray.startColumn() + .5) * tile,
                        y1 = b.y + (ray.startRow() + .5) * tile;
                double x2 = b.x + (ray.endColumn() + .5) * tile,
                        y2 = b.y + (ray.endRow() + .5) * tile;
                double ox = b.x + (ray.originColumn() + .5) * tile;
                double oy = b.y + (ray.originRow() + .5) * tile;
                Line2D line =
                        new Line2D.Double(
                                BoardMotion.front(ox, x1, age),
                                BoardMotion.front(oy, y1, age),
                                BoardMotion.front(ox, x2, age),
                                BoardMotion.front(oy, y2, age));
                g.setColor(tint);
                g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(line);
                g.setColor(UITheme.TEXT_PRIMARY);
                g.setStroke(new BasicStroke(1.4f));
                g.draw(line);
            }
            if (state.moveRow >= 0) {
                double radius = tile * (.25 + age * .8),
                        cx = b.x + (state.moveColumn + .5) * tile,
                        cy = b.y + (state.moveRow + .5) * tile;
                g.setColor(tint);
                g.setStroke(new BasicStroke(1.5f));
                g.draw(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
            }
        }
        g.dispose();
    }
}
