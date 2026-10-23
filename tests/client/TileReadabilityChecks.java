package client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.ImageIO;

/** Project-specific colour sanity checks plus previews at actual gameplay sizes. */
public final class TileReadabilityChecks {
    private static void check(boolean value, String text) {
        if (!value) throw new AssertionError(text);
    }

    public static void main(String[] args) throws Exception {
        Properties paths = new Properties();
        try (InputStream input = AssetLoader.open("images/catalog.properties")) {
            paths.load(input);
        }
        double[][] means = new double[3][];
        String[] ids = {"tile.neutral", "tile.red", "tile.blue"};
        for (int i = 0; i < ids.length; i++) {
            BufferedImage tile;
            try (InputStream input = AssetLoader.open("images/" + paths.getProperty(ids[i]))) {
                tile = ImageIO.read(input);
            }
            double[] sum = new double[3];
            int count = 0;
            for (int y = tile.getHeight() / 4; y < tile.getHeight() * 3 / 4; y++)
                for (int x = tile.getWidth() / 4; x < tile.getWidth() * 3 / 4; x++) {
                    int pixel = tile.getRGB(x, y);
                    if ((pixel >>> 24) < 128) continue;
                    sum[0] += (pixel >>> 16) & 255;
                    sum[1] += (pixel >>> 8) & 255;
                    sum[2] += pixel & 255;
                    count++;
                }
            check(count > 0, "Tile surface visible");
            for (int c = 0; c < 3; c++) sum[c] /= count;
            means[i] = sum;
            System.out.printf("TILE_RGB %s %.1f %.1f %.1f%n", ids[i], sum[0], sum[1], sum[2]);
        }
        check(
                means[1][0] - means[1][2] > 35 && means[1][0] - means[1][1] > 35,
                "Red must read coral, not grey");
        check(
                means[2][2] - means[2][0] > 40 && means[2][2] - means[2][1] > 15,
                "Blue must read blue, not grey");
        check(
                Math.max(means[0][0], Math.max(means[0][1], means[0][2]))
                                - Math.min(means[0][0], Math.min(means[0][1], means[0][2]))
                        < 35,
                "Neutral must stay low saturation");
        check(
                (means[0][0] + means[0][1] + means[0][2]) / 3 < 85,
                "Neutral should not compete with owned cells");
        SpriteCatalog.preload();
        BufferedImage sheet = new BufferedImage(720, 430, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        UITheme.applyQualityHints(g);
        g.setColor(UITheme.BG_PAGE);
        g.fillRect(0, 0, 720, 430);
        g.setFont(UITheme.FONT_BODY);
        for (int col = 0; col < 3; col++) {
            g.setColor(Color.WHITE);
            g.drawString(ids[col], 50 + col * 220, 30);
        }
        int[] sizes = {32, 48, 64};
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++) {
                int size = sizes[row], x = 50 + col * 220, y = 55 + row * 110;
                SpriteCatalog.draw(g, ids[col], x, y, size, size);
                SpriteCatalog.draw(g, ids[col], x + size + 12, y, size, size);
                if (col > 0)
                    ArenaArt.crystal(
                            g, x + size + 12 + size / 2.0, y + size / 2.0, size * .82, col);
                g.setColor(UITheme.TEXT_MUTED);
                g.drawString(size + " px", x, y + size + 22);
            }
        g.dispose();
        Path output = Path.of(".work/review/tile-readability.png");
        Files.createDirectories(output.getParent());
        ImageIO.write(sheet, "png", output.toFile());
        System.out.println("PASS: territory colour separation and 32/48/64 px comparison sheet");
    }
}
