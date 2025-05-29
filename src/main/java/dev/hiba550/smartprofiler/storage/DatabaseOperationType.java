package dev.hiba550.smartprofiler.storage;

/**
 * Types of database operations for the performance profiler
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum DatabaseOperationType {
    INSERT_FRAME,
    INSERT_ISSUE,
    UPDATE_SESSION,
    INSERT_SUGGESTION,
    UPDATE_STATISTICS,
    CLEANUP_DATA,
    EXPORT_DATA
}