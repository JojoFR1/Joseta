package joseta.utils;

import ch.qos.logback.classic.Logger;
import org.slf4j.*;
import org.slf4j.event.*;

public class Log {
    private static final Logger logger = (Logger) LoggerFactory.getLogger("Joseta");

    public static void info(String message) { Log.log(Level.INFO, message, null, null); }
    public static void info(String message, Object... args) { Log.log(Level.INFO, message, args, null); }

    public static void warn(String message) { Log.log(Level.WARN, message, null, null); }
    public static void warn(String message, Object... args) { Log.log(Level.WARN, message, args, null); }

    public static void err(String message) { Log.log(Level.ERROR, message, null, null); }
    public static void err(String message, Object... args) { Log.log(Level.ERROR, message, args, null); }
    public static void err(String message, Throwable t) { Log.log(Level.ERROR, message, null, t); }
    public static void err(String message, Throwable t, Object... args) { Log.log(Level.ERROR, message, args, t); }

    public static void debug(String message) { Log.log(Level.DEBUG, message, null, null); }
    public static void debug(String message, Object... args) { Log.log(Level.DEBUG, message, args, null); }

    public static void trace(String message) { Log.log(Level.TRACE, message, null, null); }
    public static void trace(String message, Object... args) { Log.log(Level.TRACE, message, args, null); }

    private static void log(Level logLevel, String message, Object[] args, Throwable t) {
        logger.log(
            null,
            logger.getName(),
            logLevel.toInt(),
            message,
            args,
            t
        );
    }

    public static void setLevel(ch.qos.logback.classic.Level level) {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(level);
    }
}
