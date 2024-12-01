package joseta;

import joseta.commands.*;
import joseta.events.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.*;

import org.slf4j.*;

import java.util.concurrent.*;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.Logger;

public class JosetaBot {
    private static JDA bot;
        
    public static void main(String[] args) {
        registerShutdown();

        if (args.length > 0) {
            Vars.setDebug(args[0].equals("--debug"));
            Vars.setServer(args[0].equals("--server"));
        }
        preLoad();
        
        bot = JDABuilder.createLight(Vars.token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, 
                               GatewayIntent.GUILD_MEMBERS, 
                               GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new PingCommand(),
                                   new ModCommands(),
                                   new AutoResponse(),
                                   new WelcomeMessage())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.watching("🇫🇷 Mindustry France."))
                .build();
            
        try {
            bot.awaitReady();
        } catch (InterruptedException e) {
            Vars.logger.error("An error occured while waiting for the bot to connect.", e);
            System.exit(1);
        }
    
        // Add commands on a test guild - Instantly
        if (Vars.isDebug && Vars.testGuildId != -1)
            bot.getGuildById(Vars.testGuildId).updateCommands().addCommands(Vars.commands).queue();
        // Add global commands - Takes time
        else {
            bot.getGuilds().forEach(g -> g.updateCommands().addCommands().queue()); // Reset for the guilds command to avoid duplicates.
            bot.updateCommands().addCommands(Vars.commands).queue();
        }
    }

    private static void preLoad() {
        Vars.loadSecrets();

        if (Vars.isDebug || Vars.isServer) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
            
            // The appender has to be referenced in logback.xml to 'exist', 
            // so instead of adding it if it's a server, it's removed if otherwise
            if (!Vars.isServer) rootLogger.detachAppender("FILE");
        }

        Vars.cacheWelcomeImage();
    }

    private static void registerShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Vars.logger.info("Shutting down...");
            
            if (bot != null) {
                bot.setAutoReconnect(false);
                bot.shutdown();
                
                try {
                    if (!bot.awaitShutdown(10, TimeUnit.SECONDS)) {
                        Vars.logger.warn("The shutdown 10 second limit was exceeded. Force shutting down...");    
                        bot.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Vars.logger.error("An error occured while waitin for the bot to shutdown. Force shutting down...", e);
                    bot.shutdownNow();
                }
            }
        }, "ShutdownThread"));
    } 
}
