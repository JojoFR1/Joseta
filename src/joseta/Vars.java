package joseta;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import org.slf4j.*;

import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.*;

public class Vars {
    public static final Logger logger = LoggerFactory.getLogger(JosetaBot.class);
    public static String token = null;
    public static long testGuildId = 0;
    public static String[] ownersId; // Not a long because it requires a loop
    
    public static boolean isDebug, isServer = false;
    public static BufferedImage welcomeImage;

    public static final SlashCommandData[] commands = {
        Commands.slash("ping", "Obtenez le ping du bot."),
        Commands.slash("multi", "Envoie le message d'aide pour le multijoueur."),

        Commands.slash("mute", "Mute un membre.")
            .addOption(OptionType.USER, "user", "Membre a mute", true)
            .addOption(OptionType.STRING, "reason", "La raison du mute")
            .addOption(OptionType.STRING, "time", "La durée du mute (s, m, h, d, w)")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)),
        
        Commands.slash("unmute", "Retire le mute d'un membre")
            .addOption(OptionType.USER, "user", "Membre a unmute", true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)),
        
        Commands.slash("kick", "Exclue un membre.")
            .addOption(OptionType.USER, "user", "Membre a exclure", true)
            .addOption(OptionType.STRING, "reason", "La raison de l'exclusion")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS)),
        
        Commands.slash("ban", "WIP - Ban.")
            .addOption(OptionType.USER, "user", "WIP", true)
            .addOption(OptionType.STRING, "reason", "WIP")
            .addOption(OptionType.STRING, "time", "WIP")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),
        
        Commands.slash("unban", "WIP - Unban.")
            .addOption(OptionType.USER, "user", "WIP", true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
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
        testGuildId = Long.parseLong(secret.getProperty(isDebug ? "testGuildIdDebug" : "testGuildId", "-1"));
        ownersId = secret.getProperty(isDebug ? "adminsDebug" : "admins").split(" ");
    }
    
    public static void cacheWelcomeImage() {
        Path cachedImagePath = Paths.get("resources", "welcomeImageBase.png");
        
        try {
            welcomeImage = ImageIO.read(cachedImagePath.toFile());
        } catch (IOException e) {
            Vars.logger.error("Cache - An error occured while reading the base welcome image.", e);
            System.exit(1);
        }
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
