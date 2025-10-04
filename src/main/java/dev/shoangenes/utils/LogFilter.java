package dev.shoangenes.utils;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Level;

public class LogFilter implements Filter {

    private final Level minLevel;

    /**
     * Constructs a LogFilter with the minimum log level from ServerProperties.
     */
    public LogFilter() {
        this.minLevel = Level.parse(ServerProperties.getInstance().getLogLevel());
    }

    /**
     * Checks if a LogRecord should be logged based on its level.
     * @param record  a LogRecord
     * @return true if the record should be logged, false otherwise
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        if (record == null || record.getLevel() == null) {
            return false;
        }
        return record.getLevel().intValue() >= minLevel.intValue();
    }
}