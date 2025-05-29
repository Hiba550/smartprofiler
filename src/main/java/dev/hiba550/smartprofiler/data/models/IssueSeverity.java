package dev.hiba550.smartprofiler.data.models;

/**
 * Severity levels for performance issues
 * Used for prioritization and user notification
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum IssueSeverity {
    LOW(1, "Low", "Minor performance impact"),
    MEDIUM(2, "Medium", "Noticeable performance impact"),
    HIGH(3, "High", "Significant performance impact"),
    CRITICAL(4, "Critical", "Severe performance impact, immediate attention needed");
    
    private final int weight;
    private final String displayName;
    private final String description;
    
    IssueSeverity(int weight, String displayName, String description) {
        this.weight = weight;
        this.displayName = displayName;
        this.description = description;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}