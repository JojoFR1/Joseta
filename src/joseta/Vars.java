package joseta;

import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.*;

import net.dv8tion.jda.api.interactions.commands.build.*;

import org.slf4j.*;

public class Vars {
    public static final Logger logger = LoggerFactory.getLogger(JosetaBot.class);
    public static String token, testGuildId = null;
    public static String[] ownersId = null;
    
    public static boolean isDebug, isServer = false;
    public static BufferedImage welcomeImage;

    public static final SlashCommandData[] commands = {
        Commands.slash("ping", "Obtenez le ping du bot.")
    };

    public static void loadSecrets() {
        Properties secret = new Properties();
        try (FileInputStream fi = new FileInputStream("secret.cfg")) {
            secret.load(fi);
        } catch (Exception e) {
            logger.error("Could not open the secret config file.", e);
            System.exit(1);
        }

        token = secret.getProperty(isDebug ? "tokenDebug" : "token");
        testGuildId = secret.getProperty(isDebug ? "testGuildIdDebug" : "testGuildId");
        ownersId = secret.getProperty(isDebug ? "adminsDebug" : "admins").split(" ");
    }
    
    public static void cacheWelcomeImage() throws Exception {
        Path cachedImagePath = Paths.get("resources", "welcomeImageBase.png");
        
        welcomeImage = ImageIO.read(cachedImagePath.toFile());
    }

    public static BufferedImage getWelcomeImage() {
        return welcomeImage;
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void setServer(boolean server) {
        isServer = server;
    }
}
