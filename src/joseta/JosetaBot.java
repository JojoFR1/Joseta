package joseta;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.requests.*;

import org.slf4j.*;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.Logger;

import joseta.commands.*;
import joseta.events.*;
import joseta.util.*;

public class JosetaBot {
    
    @SuppressWarnings("unused")
    public static void main(String[] args) {
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
            CachedData.cacheImages();
        } catch (Exception e) {
            Vars.logger.error("Couldn't load the caches.", e);
            System.exit(1);
        }
        
        JDA bot = JDABuilder.createLight(Vars.token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new CommandRegister())
                    .addEventListeners(new PingCommand())
                    .addEventListeners(new WelcomeMessage())
                    .build();
    }
}
