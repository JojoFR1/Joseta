package joseta;

import net.dv8tion.jda.api.*;

import joseta.commands.*;
import joseta.events.*;

public class JosetaBot {
    
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        if (args.length > 0) Vars.setDebug(args[0].equals("debug") ? true : false);
        else Vars.setDebug(false);
        Vars.loadSecrets();
        
        JDA bot = JDABuilder.createLight(Vars.token)
                    .addEventListeners(new CommandRegister())
                    .addEventListeners(new PingCommand())
                    .addEventListeners(new WelcomeMessage())
                    .build();
    }
}
