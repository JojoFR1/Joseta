package joseta;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;

import java.awt.*;
import java.io.*;
import java.time.*;
import java.util.*;

public class Vars {
    public static String token = null;
    public static long testGuildId = 0;
    public static String[] ownersId; // Not a long because it requires a loop
    
    public static boolean isDebug, isServer = false;

    public static TextChannel testChannel, welcomeChannel, logChannel;
    public static Role memberRole, botRole;

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

    // TODO Un-hardcode IDs (config)
    public static void initialize(Guild guild) {
        if (!isDebug) testChannel = guild.getChannelById(TextChannel.class, 1342202526249914499L);
        if (isDebug) testChannel = guild.getChannelById(TextChannel.class, 1348089378186068101L);
        welcomeChannel = guild.getChannelById(TextChannel.class, 1256989659448348673L);
        logChannel = guild.getChannelById(TextChannel.class, 1219276562860609576L);
        memberRole = guild.getRoleById(1259874357384056852L);
        botRole = guild.getRoleById(1234873005629243433L);
    }

    public static EmbedBuilder getDefaultEmbed(Color color, Guild guild, User user) {
        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(color)
                                .setFooter(guild.getName(), guild.getIconUrl())
                                .setTimestamp(Instant.now());
        
        if (user != null) embed.setAuthor(user.getName(), null, user.getAvatarUrl());

        return embed;
    }
    
    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void setServer(boolean server) {
        isServer = server;
    }
}
