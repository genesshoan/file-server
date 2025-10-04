package dev.shoangenes.utils;

import java.util.logging.*;

public class LoggerUtil {

    private static FileHandler fileHandler;
    private static boolean consoleEnabled;
    private static Level logLevel;

    /**
     * Initializes the logger with settings from ServerProperties.
     */
    static {
        try {
            ServerProperties props = ServerProperties.getInstance();
            logLevel = Level.parse(props.getLogLevel().toUpperCase());
            consoleEnabled = Boolean.parseBoolean(props.isLogConsoleEnabled());

            fileHandler = new FileHandler(props.getLogFilePath(), true);
            fileHandler.setFormatter(new SimpleFormatter());



        } catch (Exception e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    /**
     * Returns a configured logger for the specified class.
     *
     * @param clazz The class for which the logger is requested.
     * @return A configured Logger instance.
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setLevel(logLevel);
        logger.setFilter(new LogFilter());
        if (logger.getHandlers().length == 0) {
            logger.addHandler(fileHandler);
            if (consoleEnabled) {
                logger.addHandler(new ConsoleHandler());
            }
        }

        return logger;
    }
}
