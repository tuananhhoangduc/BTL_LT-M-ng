package client;

import java.awt.*;
import java.awt.geom.*;

/** Resolution-independent art shared by login, board and player cards. */
final class ArenaArt {
    private ArenaArt() {}

    static void crystal(Graphics2D g, double x, double y, double size, int color) {
        if (SpriteCatalog.draw(
                g, color == 1 ? "piece.red" : "piece.blue", x - size / 2, y - size / 2, size, size))
            return;
        Color tint = color == 1 ? UITheme.PIECE_RED : UITheme.PIECE_BLUE;
        Path2D shape = new Path2D.Double();
        int sides = color == 1 ? 8 : 4;
        for (int i = 0; i < sides; i++) {
            double a = -Math.PI / 2 + i * Math.PI * 2 / sides;
            double px = x + Math.cos(a) * size / 2, py = y + Math.sin(a) * size / 2;
            if (i == 0) shape.moveTo(px, py);
            else shape.lineTo(px, py);
        }
        shape.closePath();
        g.setPaint(
                new GradientPaint(
                        (float) x,
                        (float) (y - size / 2),
                        tint,
                        (float) x,
                        (float) (y + size / 2),
                        tint.darker().darker()));
        g.fill(shape);
        g.setColor(UITheme.blendColors(tint, Color.WHITE, .3f));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(shape);
        Shape clip = g.getClip();
        g.clip(shape);
        Path2D facet = new Path2D.Double();
        facet.moveTo(x - size / 2, y);
        facet.lineTo(x, y - size / 2);
        facet.lineTo(x, y + size * .12);
        facet.closePath();
        g.setColor(new Color(255, 255, 255, 65));
        g.fill(facet);
        g.setColor(new Color(255, 255, 255, 45));
        g.draw(new Line2D.Double(x, y - size / 2, x, y + size / 2));
        g.draw(new Line2D.Double(x - size / 2, y, x + size / 2, y));
        g.setClip(clip);
    }

    static void backdrop(Graphics2D g, int width, int height) {
        g.setColor(UITheme.BG_PAGE);
        g.fillRect(0, 0, width, height);
        if (SpriteCatalog.cover(g, "background.login", width, height, .65f)) return;
        g.setColor(UITheme.BG_CARD);
        Path2D p = new Path2D.Double();
        p.moveTo(0, height);
        p.lineTo(width * .3, height * .65);
        p.lineTo(width * .65, height);
        p.closePath();
        g.fill(p);
        g.setColor(UITheme.BORDER);
        g.setStroke(new BasicStroke(1));
        g.draw(new Line2D.Double(0, height, width * .3, height * .65));
        g.draw(new Line2D.Double(width * .3, height * .65, width * .65, height));
    }
}
