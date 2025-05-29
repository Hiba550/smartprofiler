package dev.hiba550.smartprofiler.config;

import dev.hiba550.smartprofiler.SmartProfilerMod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages configuration change notifications
 * 
 * Provides a centralized system for components to register for configuration
 * change notifications and automatically handles propagation of changes.
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class ConfigChangeManager {
    
    private static final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * Registers a listener for configuration changes
     */
    public static void registerListener(ConfigChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            SmartProfilerMod.LOGGER.debug("Registered config change listener: {}", 
                listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Unregisters a configuration change listener
     */
    public static void unregisterListener(ConfigChangeListener listener) {
        if (listeners.remove(listener)) {
            SmartProfilerMod.LOGGER.debug("Unregistered config change listener: {}", 
                listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Notifies all listeners of a configuration change
     */
    public static void notifyChange(ConfigChangeType changeType, Object oldValue, Object newValue) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigChanged(changeType, oldValue, newValue);
            } catch (Exception e) {
                SmartProfilerMod.LOGGER.error("Error in config change listener", e);
            }
        }
    }
    
    /**
     * Convenience method for boolean changes
     */
    public static void notifyBooleanChange(ConfigChangeType changeType, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            notifyChange(changeType, oldValue, newValue);
        }
    }
    
    /**
     * Convenience method for integer changes
     */
    public static void notifyIntegerChange(ConfigChangeType changeType, int oldValue, int newValue) {
        if (oldValue != newValue) {
            notifyChange(changeType, oldValue, newValue);
        }
    }
    
    /**
     * Clears all registered listeners
     */
    public static void clearListeners() {
        listeners.clear();
        SmartProfilerMod.LOGGER.debug("Cleared all config change listeners");
    }
    
    /**
     * Gets the number of registered listeners
     */
    public static int getListenerCount() {
        return listeners.size();
    }
}