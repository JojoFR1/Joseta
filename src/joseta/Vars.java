package joseta;

import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.*;

public class Vars {
    public static String token = null;
    public static long testGuildId = 0;
    public static String[] ownersId; // Not a long because it requires a loop
    
    public static boolean isDebug, isServer = false;
    public static BufferedImage welcomeImage;
    
    public static void loadSecrets() {
        Properties secret = new Properties();
        try (FileInputStream fi = new FileInputStream("secret.cfg")) {
            secret.load(fi);
        } catch (IOException e) {
            JosetaBot.logger.error("Could not open the secret config file.", e);
            System.exit(1);
        }

        token = secret.getProperty(isDebug ? "tokenDebug" : "token");
        testGuildId = Long.parseLong(secret.getProperty(isDebug ? "testGuildIdDebug" : "testGuildId", "-1"));
        ownersId = secret.getProperty(isDebug ? "adminsDebug" : "admins").split(" ");
    }
    
    public static void cacheWelcomeImage() {
        Path cachedImagePath = Paths.get("resources", "welcomeImageBase.png");
        
        try {
            welcomeImage = ImageIO.read(cachedImagePath.toFile());
        } catch (IOException e) {
            JosetaBot.logger.error("Cache - An error occured while reading the base welcome image.", e);
            System.exit(1);
        }
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void setServer(boolean server) {
        isServer = server;
    }
}
