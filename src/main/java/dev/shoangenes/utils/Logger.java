package dev.shoangenes.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static Logger instance = null;
    private PrintWriter fileWriter = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private LogLevel logLevel = LogLevel.INFO;
    private boolean consoleEnabled = true;

    /**
     * Private constructor to prevent instantiation
     */
    private Logger() {
        initializeLogger();
    }

    /**
     * Singleton getInstance method
     */
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    /**
     * Initialize logger settings from ServerProperties
     */
    private void initializeLogger() {
        try {
            ServerProperties props = ServerProperties.getInstance();

            logLevel = LogLevel.valueOf(props.getLogLevel().toUpperCase());

            consoleEnabled = Boolean.parseBoolean(props.isLogConsoleEnabled());

            String logFile = props.getLogFilePath();
            setUpFileWriter(logFile);
        } catch (Exception e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    /**
     * Set up the file writer for logging to a file
     */
    private void setUpFileWriter(String logPath) {
        try {
            File logFile = new File(logPath);
            File logDir = logFile.getParentFile();
            if (logDir != null && !logDir.exists()) {
                logDir.mkdirs();
            }

            fileWriter = new PrintWriter(new FileWriter(logFile));
        } catch (IOException e) {
            System.err.println("Failed to set up log file writer: " + e.getMessage());
            fileWriter = null;
        }
    }

    /**
     * Log a message with the specified log level
     */
    private void log(LogLevel level, String message) {
        if (!shouldLog(level)) {
            return;
        }

        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] %s: %s", timestamp, level, message);

        if (consoleEnabled) {
            if (isAnErrorLevel(level)) {
                System.err.println(logMessage);
            } else {
                System.out.println(logMessage);
            }
        }

        if (fileWriter != null) {
            fileWriter.println(logMessage);
            fileWriter.flush();
        }
    }

    /**
     * Determine if a message should be logged based on the current log level
     * @param level The log level of the message
     * @return true if the message should be logged, false otherwise
     */
    private  boolean shouldLog(LogLevel level) {
        return level.ordinal() >= logLevel.ordinal();
    }

    /**
     * Check if the log level is ERROR or WARN
     * @param level The log level to check
     * @return true if the level is ERROR or WARN, false otherwise
     */
    private boolean isAnErrorLevel(LogLevel level) {
        return level == LogLevel.ERROR || level == LogLevel.WARN;
    }

    /**
     * Log a debug message
     * @param message The message to log
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    /**
     * Log an info message
     * @param message The message to log
     */
    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    /**
     * Log a warning message
     * @param message The message to log
     */
    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    /**
     * Log an error message
     * @param message The message to log
     */
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    /**
     * Log an error message with an exception
     * @param message The message to log
     * @param e The exception to log
     */
    public void error(String message, Exception e) {
        log(LogLevel.ERROR, message + " - Exception: " + e.getMessage());
    }

    /**
     * Log server start event
     * @param host The host the server is running on
     * @param port The port the server is running on
     */
    public void logServerStart(String host, int port) {
        info(String.format("Server started on %s:%d", host, port));
    }

    /**
     * Log server stop event
     */
    public void logServerStop() {
        info("Server stopped.");
    }

    /**
     * Log client connection event
     * @param clientAddress The address of the connected client
     */
    public void logClientConnection(String clientAddress) {
        info("Client connected: " + clientAddress);
    }

    /**
     * Log client disconnection event
     * @param clientAddress The address of the disconnected client
     */
    public void logClientDisconnection(String clientAddress) {
        info("Client disconnected: " + clientAddress);
    }

    /**
     * Log HTTP request event
     * @param clientIP The IP address of the client
     * @param method The HTTP method of the request
     * @param uri The URI of the request
     */
    public void logRequest(String clientIP, String method, String uri) {
        info(String.format("REQUEST - %s | %s %s", clientIP, method, uri));
    }

    /**
     * Log HTTP response event
     * @param clientIP The IP address of the client
     * @param statusCode The HTTP status code of the response
     * @param uri The URI of the request that generated the response
     */
    public void logResponse(String clientIP, int statusCode, String uri) {
        info(String.format("RESPONSE - %s | %d %s", clientIP, statusCode, uri));
    }

    /**
     * Log file operation event
     * @param operation The file operation performed (e.g., UPLOAD, DOWNLOAD, DELETE)
     * @param filename The name of the file involved in the operation
     * @param success Whether the operation was successful
     */
    public void logFileOperation(String operation, String filename, boolean success) {
        if (success) {
            info(String.format("FILE %s: %s - SUCCESS", operation, filename));
        } else {
            warn(String.format("FILE %s: %s - FAILED", operation, filename));
        }
    }

    /**
     * Close the logger and release resources
     */
    public void close() {
        if (fileWriter != null) {
            info("Closing logger.");
            fileWriter.close();
        }
    }
}
