package client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.ImageIO;

public final class AssetChecks {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        Properties catalog = new Properties();
        try (InputStream in = AssetLoader.open("images/catalog.properties")) {
            check(in != null, "Catalog exists in JAR");
            catalog.load(in);
        }
        check(catalog.size() == 10, "Ten production PNGs");
        BufferedImage sheet = new BufferedImage(1000, 480, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(11, 18, 32));
        g.fillRect(0, 0, 1000, 480);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        int index = 0;
        for (String key : new TreeSet<>(catalog.stringPropertyNames())) {
            BufferedImage image;
            try (InputStream in = AssetLoader.open("images/" + catalog.getProperty(key))) {
                check(in != null, key + " exists");
                image = ImageIO.read(in);
            }
            check(
                    image != null && image.getWidth() >= 256 && image.getHeight() >= 256,
                    key + " dimensions/decode");
            if (!key.startsWith("background.")) {
                check(image.getColorModel().hasAlpha(), key + " true alpha channel");
                check((image.getRGB(0, 0) >>> 24) == 0, key + " corner must be transparent");
                if (!key.startsWith("emblem."))
                    check(
                            (image.getRGB(image.getWidth() / 2, image.getHeight() / 2) >>> 24)
                                    > 128,
                            key + " visible center");
            }
            int x = index % 5 * 200, y = index / 5 * 240;
            double scale = Math.min(176.0 / image.getWidth(), 190.0 / image.getHeight());
            g.drawImage(
                    image,
                    x + 12,
                    y + 20,
                    (int) (image.getWidth() * scale),
                    (int) (image.getHeight() * scale),
                    null);
            g.setColor(Color.WHITE);
            g.drawString(key, x + 12, y + 226);
            index++;
            System.out.println("PNG_OK " + key + " " + image.getWidth() + "x" + image.getHeight());
        }
        g.dispose();
        if (args.length > 0 && args[0].equals("--sheet")) {
            Path output = Path.of(".work/review/assets.png");
            Files.createDirectories(output.getParent());
            ImageIO.write(sheet, "png", output.toFile());
        }
        System.out.println(
                "PASS: ten standalone production PNGs; sprite alpha and packaged catalog");
    }
}
