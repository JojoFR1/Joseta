package joseta;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.*;

import org.slf4j.*;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.Logger;

import joseta.commands.*;
import joseta.events.*;

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
            rootLogger.setLevel(Level.TRACE);
            
            // The appender has to be referenced in logback.xml to 'exist', 
            // so instead of adding it if it's a server, it's removed if otherwise
            if (!Vars.server) {
                rootLogger.setLevel(Level.DEBUG);
                rootLogger.detachAppender("FILE");
            }
        }

        try {
            Vars.cacheWelcomeImage();
        } catch (Exception e) {
            Vars.logger.error("Couldn't load the caches.", e);
            System.exit(1);
        }
        
        JDABuilder builder = JDABuilder.createLight(Vars.token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new CommandRegister())
                    .addEventListeners(new PingCommand())
                    .addEventListeners(new WelcomeMessage());
        
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        builder.setActivity(Activity.playing("Hola"));
                    
        builder.build();
    }
}
