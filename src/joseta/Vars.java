package joseta;

import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.*;

import org.slf4j.*;

public class Vars {
    public static final Logger logger = LoggerFactory.getLogger(JosetaBot.class);
    public static String token, testGuildId = null;
    public static String[] ownersId = null;
    
    public static boolean debug, server = false;
    public static BufferedImage welcomeImage;


    public static void loadSecrets() {
        Properties secret = new Properties();
        try (FileInputStream fi = new FileInputStream("secret.cfg")) {
            secret.load(fi);
        } catch (Exception e) {
            System.err.println("Couldn't open the secret config file.");
            System.exit(1);
        }

        token = secret.getProperty(debug ? "tokenDebug" : "token");
        testGuildId = secret.getProperty(debug ? "testGuildIdDebug" : "testGuildId");
        ownersId = secret.getProperty(debug ? "adminsDebug" : "admins").split(" ");
    }
    
    public static void cacheWelcomeImage() throws Exception {
        Path cachedImagePath = Paths.get("resources", "welcomeImageBase.png");
        
        welcomeImage = ImageIO.read(cachedImagePath.toFile());
    }

    public static BufferedImage getWelcomeImage() {
        return welcomeImage;
    }

    public static void setDebug(boolean isDebug) {
        debug = isDebug;
    }

    public static void setServer(boolean isServer) {
        server = isServer;
    }
}
