package client;

import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

final class AssetLoader {
    private static final Map<String, Image> cache = new HashMap<>();
    private static final String ASSET_DIR = "assets" + File.separator;

    private AssetLoader() {}

    /**
     * Tải ảnh từ thư mục assets/ (nếu người dùng thêm ảnh vào).
     * Trả về null nếu chưa có file để giao diện tự động vẽ fallback Graphics2D vector.
     */
    static Image getImage(String fileName) {
        if (cache.containsKey(fileName)) {
            return cache.get(fileName);
        }
        try {
            File f = new File(ASSET_DIR + fileName);
            if (f.exists() && f.isFile()) {
                Image img = ImageIO.read(f);
                if (img != null) {
                    cache.put(fileName, img);
                    return img;
                }
            }
        } catch (Exception ignored) {}
        cache.put(fileName, null);
        return null;
    }
}

