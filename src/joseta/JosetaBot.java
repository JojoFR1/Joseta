package joseta;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.*;

import org.slf4j.*;

import java.util.concurrent.*;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.Logger;

import joseta.commands.*;
import joseta.events.*;

public class JosetaBot {
    private static JDA bot;
        
    public static void main(String[] args) {
        registerShutdown();
        preLoad(args);
        
        bot = JDABuilder.createLight(Vars.token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, 
                               GatewayIntent.GUILD_MEMBERS, 
                               GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new PingCommand(),
                                   new GameCommand(),
                                   new AutoResponse(),
                                   new WelcomeMessage())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.watching("🇫🇷 Mindustry France."))
                .build();
        
        try {
            bot.awaitReady();
        } catch (InterruptedException e) {
            Vars.logger.error("Could not await for the bot to connect.", e);
        }

        // Add global commands - Around 1 hour
        bot.updateCommands().addCommands(Vars.commands).queue();
        // Add commands on a test guild - Instantly
        if (Vars.debug) bot.getGuildById(Vars.testGuildId).updateCommands().addCommands(Vars.commands).queue();
    }

    private static void preLoad(String args[]) {
        if (args.length > 0) {
            Vars.setDebug(args[0].equals("--debug"));
            Vars.setServer(args[0].equals("--server"));
        }
        Vars.loadSecrets();

        if (Vars.debug || Vars.server) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
            
            // The appender has to be referenced in logback.xml to 'exist', 
            // so instead of adding it if it's a server, it's removed if otherwise
            if (!Vars.server) rootLogger.detachAppender("FILE");
        }

        try {
            Vars.cacheWelcomeImage();
        } catch (Exception e) {
            Vars.logger.error("Couldn't load the caches.", e);
            System.exit(1);
        }
    }

    private static void registerShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Vars.logger.info("Shutting down...");

            if (bot != null) {
                bot.shutdown();

                try {
                    if (!bot.awaitShutdown(10, TimeUnit.SECONDS))
                        bot.shutdownNow();
                } catch (Exception e) {
                    bot.shutdownNow();
                }
            }
        }));
    } 
}
