package client;

import java.awt.Image;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.ImageIO;

final class AssetLoader {
    private static final Map<String, Image> CACHE = new HashMap<>();
    private static final Set<String> MISSING = new HashSet<>();

    private AssetLoader() {}

    static InputStream open(String name) throws IOException {
        Path file = Path.of("assets").resolve(name);
        if (Files.isRegularFile(file)) return Files.newInputStream(file);
        return AssetLoader.class.getResourceAsStream("/assets/" + name);
    }

    static synchronized Image getImage(String name) {
        if (CACHE.containsKey(name)) return CACHE.get(name);
        if (MISSING.contains(name)) return null;
        try (InputStream input = open(name)) {
            Image image = input == null ? null : ImageIO.read(input);
            if (image != null) {
                CACHE.put(name, image);
                return image;
            }
        } catch (IOException ignored) {
        }
        MISSING.add(name);
        return null;
    }
}
