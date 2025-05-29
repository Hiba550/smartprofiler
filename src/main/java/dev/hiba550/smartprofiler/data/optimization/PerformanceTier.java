package dev.hiba550.smartprofiler.data.optimization;

/**
 * Performance tiers for hardware classification
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum PerformanceTier {
    HIGH_END("High-End", "Latest hardware, can handle max settings"),
    MID_RANGE("Mid-Range", "Good performance with optimized settings"),
    LOW_END("Low-End", "Basic performance, requires optimization"),
    POTATO("Potato", "Minimal hardware, needs aggressive optimization");
    
    private final String displayName;
    private final String description;
    
    PerformanceTier(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}