package dev.shoangenes.utils;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ServerProperties {
    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_STORAGE_DIR = "./storage/";
    private static final String DEFAULT_MAX_FILE_SIZE = "52428800"; // 50MB
    private static final String DEFAULT_MAX_CONNECTIONS = "100";
    private static final String DEFAULT_ALLOWED_TYPES = "txt,jpg,png,gif,pdf,docx";
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final String DEFAULT_LOG_FILE = "./logs/server.log";
    private static final String DEFAULT_LOG_CONSOLE = "true";
    private static final String PROPERTIES_FILE = "config.properties";

    private final static Logger logger = LoggerUtil.getLogger(ServerProperties.class);

    /* Singleton instance */
    private static ServerProperties instance = null;
    /* Properties object to hold the configurations */
    private Properties properties = new Properties();

    /**
     * Private constructor to prevent instantiation
     */
    private ServerProperties() {
        loadProperties();
    }

    /**
     * Singleton getInstance method
     */
    public static ServerProperties getInstance() {
        if (instance == null) {
            instance = new ServerProperties();
        }
        return instance;
    }

    /**
     * Load properties from the config file
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            logger.warning("Could not load properties file, using default values.");
        }
    }

    /**
     * Get the server port
     * @return the server port as a string
     */
    public int getPort() {
        return Integer.parseInt(properties.getProperty("server.port", DEFAULT_PORT));
    }

    /**
     * Get the server host
     * @return the server host as a string
     */
    public String getHost() {
        return properties.getProperty("server.host", DEFAULT_HOST);
    }

    /**
     * Get the storage directory
     * @return the storage directory as a string
     */
    public String getStorageDir() {
        return properties.getProperty("server.storageDir", DEFAULT_STORAGE_DIR);
    }

    /**
     * Get the maximum file size allowed for uploads
     * @return the maximum file size as a string
     */
    public int getMaxFileSize() {
        return Integer.parseInt(properties.getProperty("server.maxFileSize", DEFAULT_MAX_FILE_SIZE));
    }

    /**
     * Get the maximum number of concurrent connections
     * @return the maximum number of connections as a string
     */
    public int getMaxConnections() {
        return Integer.parseInt(properties.getProperty("server.maxConnections", DEFAULT_MAX_CONNECTIONS));
    }

    /**
     * Get the allowed file types for upload
     * @return a comma-separated string of allowed file types
     */
    public String getAllowedTypes() {
        return properties.getProperty("server.allowedTypes", DEFAULT_ALLOWED_TYPES);
    }

    /**
     * Get the logging level
     * @return the logging level as a string
     */
    public String getLogLevel() {
        return properties.getProperty("logging.level", DEFAULT_LOG_LEVEL);
    }

    /**
     * Get the log file path
     * @return the log file path as a string
     */
    public String getLogFilePath() {
        return properties.getProperty("logging.file", DEFAULT_LOG_FILE);
    }

    /**
     * Check if console logging is enabled
     * @return "true" if console logging is enabled, otherwise "false"
     */
    public String isLogConsoleEnabled() {
        return properties.getProperty("logging.console", DEFAULT_LOG_CONSOLE);
    }

    public String getMappingFilePath() {
        return properties.getProperty("server.mapping.file", "./file_mappings.json");
    }
}
