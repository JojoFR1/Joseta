package joseta;

import java.io.*;
import java.util.*;

import net.dv8tion.jda.api.*;

public class JosetaBot {
    
    public static void main(String[] args) {
        Properties secret = new Properties();
        try {
            secret.load(new FileInputStream("secret.cfg"));
        } catch (Exception e) {
            // TODO: handle exception
        }

        String token = secret.getProperty("token");
        JDA bot = JDABuilder.createLight(token)
                    .addEventListeners(new CommandRegister())
                    .addEventListeners(new PingCommand())
                    .build();
    }
}
