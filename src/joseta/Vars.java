package joseta;

import java.io.*;
import java.util.*;

import org.slf4j.*;

public class Vars {
    public static final Logger logger = LoggerFactory.getLogger(JosetaBot.class);
    public static String token, testGuildId = null;
    public static String[] ownersId = null;
    
    public static boolean debug;

    public static void setDebug(boolean isDebug) {
        debug = isDebug;
    }

    public static void loadSecrets() {
        Properties secret = new Properties();
        try (FileInputStream fi = new FileInputStream("secret.cfg")) {
            secret.load(fi);
        } catch (Exception e) {
            System.err.println("Couldn't open the secret config file.");
            System.exit(1);
        }

        token = debug ? secret.getProperty("tokenDebug") : secret.getProperty("token");
        testGuildId = debug ? secret.getProperty("testGuildIdDebug") : secret.getProperty("testGuildId");
        ownersId = secret.getProperty("admins").split(" ");
    }
}