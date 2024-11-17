package joseta.util;

import java.awt.image.*;
import java.net.*;
import java.nio.file.*;

import javax.imageio.*;

public class CachedData {
    private static BufferedImage welcomeImage;

    
    public static void cacheImages() throws Exception {
        Files.createDirectories(Paths.get("images"));
        Path cachedImagePath = Paths.get("images", "welcomeImageBase.png");
        
        if (Files.exists(cachedImagePath))  {
            welcomeImage = ImageIO.read(cachedImagePath.toFile());
        } else {
            welcomeImage = ImageIO.read(new URL("https://probot.media/tZFvtyOXuC.png"));
            // Cache the image
            ImageIO.write(welcomeImage, "png", cachedImagePath.toFile());
        }
    }

    public static BufferedImage getWelcomeImage() {
        return welcomeImage;
    }
}
