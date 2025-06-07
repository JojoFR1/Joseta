package joseta;

import java.io.*;
import java.util.*;

public class Vars {
    public static String token = null;
    public static long testGuildId = 0;
    public static String[] ownersId; // Not a long because it requires a loop
    
    public static boolean isDebug, isServer = false;

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
    
    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void setServer(boolean server) {
        isServer = server;
    }
}
