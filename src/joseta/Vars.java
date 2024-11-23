package joseta;

import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.*;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import org.slf4j.*;

public class Vars {
    public static final Logger logger = LoggerFactory.getLogger(JosetaBot.class);
    public static String token, testGuildId = null;
    public static String[] ownersId = null;
    
    public static boolean debug, server = false;
    public static BufferedImage welcomeImage;

    public static SlashCommandData[] commands = {
        Commands.slash("ping", "Obtenez le ping du bot."),
        Commands.slash("play", "Jouez a des jeux.")
            .addOptions(new OptionData(OptionType.STRING, "game", "Le jeu à jouer.", true)
                .addChoice("Guess the Block", "gtb"))
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
    };

    public static void loadSecrets() {
        Properties secret = new Properties();
        try (FileInputStream fi = new FileInputStream("secret.cfg")) {
            secret.load(fi);
        } catch (Exception e) {
            logger.error("Could not open the secret config file.", e);
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

    public static void setDebug(boolean isDebug) {
        debug = isDebug;
    }

    public static void setServer(boolean isServer) {
        server = isServer;
    }
}
