package client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.imageio.ImageIO;

/** File-backed art. Decode/preparation are outside paint; scaled variants have a bounded cache. */
final class SpriteCatalog {
    private static final Map<String, BufferedImage> ORIGINALS = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> SCALED =
            Collections.synchronizedMap(
                    new LinkedHashMap<>(64, .75f, true) {
                        @Override
                        protected boolean removeEldestEntry(
                                Map.Entry<String, BufferedImage> entry) {
                            return size() > 64;
                        }
                    });
    private static final Set<String> IN_FLIGHT = ConcurrentHashMap.newKeySet();
    private static final ThreadPoolExecutor RESIZER =
            new ThreadPoolExecutor(
                    1,
                    1,
                    0,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(32),
                    r -> {
                        Thread t = new Thread(r, "sprite-resize");
                        t.setDaemon(true);
                        return t;
                    });
    private static volatile boolean loaded;

    private SpriteCatalog() {}

    static synchronized void preload() {
        if (loaded) return;
        Properties catalog = new Properties();
        try (InputStream stream = AssetLoader.open("images/catalog.properties")) {
            if (stream == null) throw new IOException("Missing images/catalog.properties");
            catalog.load(stream);
            for (String key : catalog.stringPropertyNames()) {
                try (InputStream input = AssetLoader.open("images/" + catalog.getProperty(key))) {
                    if (input == null) throw new IOException("Missing sprite " + key);
                    BufferedImage image = ImageIO.read(input);
                    if (image == null) throw new IOException("Invalid PNG " + key);
                    if (!key.startsWith("background.")) image = trimPadding(image);
                    if (!key.startsWith("background.")
                            && Math.max(image.getWidth(), image.getHeight()) > 256) {
                        double factor = 256.0 / Math.max(image.getWidth(), image.getHeight());
                        image =
                                resize(
                                        image,
                                        Math.max(1, (int) (image.getWidth() * factor)),
                                        Math.max(1, (int) (image.getHeight() * factor)));
                    }
                    ORIGINALS.put(key, image);
                } catch (IOException error) {
                    System.err.println("[Art] " + error.getMessage());
                }
            }
        } catch (IOException error) {
            System.err.println("[Art] " + error.getMessage());
        }
        loaded = true;
    }

    static boolean available(String id) {
        return ORIGINALS.containsKey(id);
    }

    static int count() {
        return ORIGINALS.size();
    }

    private static BufferedImage trimPadding(BufferedImage image) {
        int left = image.getWidth(), top = image.getHeight(), right = -1, bottom = -1;
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) > 64) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
            }
        return right < left
                ? image
                : image.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    private static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    static boolean draw(Graphics2D g, String id, double x, double y, double width, double height) {
        BufferedImage original = ORIGINALS.get(id);
        if (original == null || width < 1 || height < 1) return false;
        if (id.startsWith("emblem.")) {
            double factor = Math.min(width / original.getWidth(), height / original.getHeight());
            double fittedWidth = original.getWidth() * factor;
            double fittedHeight = original.getHeight() * factor;
            x += (width - fittedWidth) / 2;
            y += (height - fittedHeight) / 2;
            width = fittedWidth;
            height = fittedHeight;
        }
        int w = Math.max(1, (int) Math.round(width)), h = Math.max(1, (int) Math.round(height));
        // Device-pixel cache prevents blurred sprites at Windows 125/150% scaling.
        double scale = Math.abs(g.getTransform().getScaleX());
        int pw = Math.max(1, (int) Math.round(w * scale)),
                ph = Math.max(1, (int) Math.round(h * scale));
        String key = id + ":" + pw + "x" + ph;
        BufferedImage image = SCALED.get(key);
        if (image == null) {
            image = original;
            if ((long) pw * ph <= 512L * 512 && IN_FLIGHT.add(key)) {
                try {
                    RESIZER.execute(
                            () -> {
                                try {
                                    SCALED.put(key, resize(original, pw, ph));
                                } finally {
                                    IN_FLIGHT.remove(key);
                                }
                            });
                } catch (RejectedExecutionException ignored) {
                    IN_FLIGHT.remove(key);
                }
            }
        }
        g.drawImage(image, (int) Math.round(x), (int) Math.round(y), w, h, null);
        return true;
    }

    static boolean cover(Graphics2D g, String id, int width, int height, float opacity) {
        BufferedImage image = ORIGINALS.get(id);
        if (image == null || width <= 0 || height <= 0) return false;
        double scale =
                Math.max(width / (double) image.getWidth(), height / (double) image.getHeight());
        int w = (int) Math.ceil(image.getWidth() * scale),
                h = (int) Math.ceil(image.getHeight() * scale);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(opacity));
        g.drawImage(image, (width - w) / 2, (height - h) / 2, w, h, null);
        g.setComposite(old);
        return true;
    }

    static javax.swing.JPanel arenaPanel(LayoutManager layout) {
        javax.swing.JPanel panel =
                new javax.swing.JPanel(layout) {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        super.paintComponent(graphics);
                        Graphics2D g = (Graphics2D) graphics.create();
                        cover(g, "background.arena", getWidth(), getHeight(), .42f);
                        g.dispose();
                    }
                };
        panel.setBackground(UITheme.BG_PAGE);
        return panel;
    }

    static void shutdown() {
        RESIZER.shutdownNow();
        SCALED.clear();
        ORIGINALS.clear();
    }
}
