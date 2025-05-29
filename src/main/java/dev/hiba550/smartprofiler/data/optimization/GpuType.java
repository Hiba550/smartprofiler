package dev.hiba550.smartprofiler.data.optimization;

/**
 * GPU types for hardware detection and optimization
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum GpuType {
    NVIDIA("NVIDIA GPU", true),
    AMD("AMD GPU", true),
    INTEGRATED("Integrated Graphics", false),
    UNKNOWN("Unknown GPU", false),
    NONE("No GPU", false);
    
    private final String displayName;
    private final boolean isDedicated;
    
    GpuType(String displayName, boolean isDedicated) {
        this.displayName = displayName;
        this.isDedicated = isDedicated;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isDedicated() {
        return isDedicated;
    }
}