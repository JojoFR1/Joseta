package joseta;

import arc.util.*;

import java.io.*;
import java.util.*;

public class Vars {
    public static String token = null;
    public static String[] ownersId; // Not a long because it requires a loop
    public static String sqlFilePath, sqlUrl, sqlUsername, sqlPassword;
    
    public static boolean isDebug, isServer = false;

    public static void loadSecrets() {
        Properties secret = new Properties();
        try (FileInputStream fi = new FileInputStream("secret.cfg")) {
            secret.load(fi);
        } catch (IOException e) {
            Log.err("Could not open the secret config file.", e);
            System.exit(1);
        }
        
        String suffix = isDebug ? "-dev" : "";
        
        token = secret.getProperty("token" + suffix);
        ownersId = secret.getProperty("admins" + suffix).split(" ");
        sqlFilePath = secret.getProperty("sqlFilePath" + suffix );
        sqlUrl = secret.getProperty("sqlDriver" + suffix) + sqlFilePath;
        sqlUsername = secret.getProperty("sqlUsername" + suffix);
        sqlPassword = secret.getProperty("sqlPassword" + suffix);
    }
    
    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void setServer(boolean server) {
        isServer = server;
    }
}
