package dev.hiba550.smartprofiler.storage;

/**
 * Represents a database operation to be processed asynchronously
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class DatabaseOperation {
    private final DatabaseOperationType type;
    private final Object data;
    private final long timestamp;
    private final int priority;
    
    public DatabaseOperation(DatabaseOperationType type, Object data, long timestamp) {
        this(type, data, timestamp, 0);
    }
    
    public DatabaseOperation(DatabaseOperationType type, Object data, long timestamp, int priority) {
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
        this.priority = priority;
    }
    
    public DatabaseOperationType getType() {
        return type;
    }
    
    public Object getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public int getPriority() {
        return priority;
    }
    
    /**
     * Gets the age of this operation in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Checks if this operation has expired (older than 30 seconds)
     */
    public boolean isExpired() {
        return getAge() > 30000;
    }
}