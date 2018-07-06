package uk.ac.port.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Simple logger for this application.
 *
 * @author Clement Gouin
 */
public final class Logger {

    private static java.util.logging.Logger appLogger = java.util.logging.Logger.getLogger("VSquare");

    private Logger() {
    }

    /**
     * Change the log level of this logger.
     *
     * @param newLevel the level of log to show
     */
    public static void setLevel(Level newLevel) {
        appLogger.setLevel(newLevel);
    }

    /**
     * @return the current logger level;
     */
    public static Level getLevel() {
        return appLogger.getLevel();
    }

    /**
     * Load a config file for the logger and set locale to english.
     *
     * @param relativePath the path in resources of the config file
     */
    public static void init(String relativePath) {
        init(relativePath, Level.INFO);
    }

    /**
     * Load a config file for the logger and set locale to english.
     *
     * @param relativePath the path in resources of the config file
     * @param level        the level to set the logger to
     */
    public static void init(String relativePath, Level level) {
        Locale.setDefault(Locale.ENGLISH);
        loadConfigFromFile(relativePath);
        Logger.setLevel(level);
    }

    /**
     * Load a config file for the logger.
     *
     * @param relativePath the path in resources of the config file
     */
    private static void loadConfigFromFile(String relativePath) {
        try {
            InputStream is = Logger.class.getClassLoader().getResourceAsStream(relativePath);
            if (is == null) {
                Logger.log(Level.SEVERE, "Logger config file not found at path {0}", relativePath);
                return;
            }
            LogManager.getLogManager().readConfiguration(is);
            appLogger = java.util.logging.Logger.getLogger("VSquare");
        } catch (IOException e) {
            Logger.log(Level.SEVERE, e.toString(), e);
        }
    }

    /**
     * Log a message.
     *
     * @param lvl     the level of logging
     * @param message the message
     * @param objects the object for the message formatting
     */
    public static void log(Level lvl, String message, Object... objects) {
        Logger.log(Utils.getCallingClassName(3), lvl, message, objects);
    }

    public static void log(Exception e){
        Logger.log(Level.SEVERE, e.toString(), e);
    }

    public static void log(Level lvl, Exception e){
        Logger.log(lvl, e.toString(), e);
    }

    /**
     * Log a message.
     *
     * @param source  the source class name
     * @param lvl     the level of logging
     * @param message the message
     * @param objects the object for the message formatting
     */
    public static void log(String source, Level lvl, String message, Object... objects) {
        message = String.format("[VSquare-%s] %s", source, message);
        appLogger.log(lvl, message, objects);
        if (lvl == Level.SEVERE && objects.length > 0 && objects[0] instanceof Exception) {
            Exception e = (Exception) objects[0];
            StringBuilder stackTrace = new StringBuilder(message);
            for (StackTraceElement ste : e.getStackTrace()) {
                stackTrace.append('\n');
                stackTrace.append(ste);
                Logger.log(source, Level.SEVERE, "\t {0}", ste);
            }
        }
    }
}
