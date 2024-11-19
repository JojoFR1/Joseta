package joseta;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.requests.*;

import joseta.commands.*;
import joseta.events.*;
import joseta.util.*;

public class JosetaBot {
    
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        if (args.length > 0) Vars.setDebug(args[0].equals("debug") ? true : false);
        else Vars.setDebug(false);
        Vars.loadSecrets();

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
