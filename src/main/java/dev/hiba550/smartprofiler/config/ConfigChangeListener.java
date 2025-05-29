package dev.hiba550.smartprofiler.config;

/**
 * Interface for components that need to respond to configuration changes
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
@FunctionalInterface
public interface ConfigChangeListener {
    
    /**
     * Called when configuration changes are detected
     * 
     * @param changeType The type of change that occurred
     * @param oldValue The previous value (may be null)
     * @param newValue The new value (may be null)
     */
    void onConfigChanged(ConfigChangeType changeType, Object oldValue, Object newValue);
}