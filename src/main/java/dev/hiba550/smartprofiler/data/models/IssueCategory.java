package dev.hiba550.smartprofiler.data.models;

/**
 * Categories of performance issues for organization and analysis
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum IssueCategory {
    RENDERING(4, "Rendering", "Graphics and visual rendering"),
    MEMORY(3, "Memory", "Memory usage and garbage collection"),
    WORLD(2, "World", "World loading and game logic"),
    NETWORK(1, "Network", "Network communication"),
    SYSTEM(2, "System", "System resources and OS interaction");
    
    private final int weight;
    private final String displayName;
    private final String description;
    
    IssueCategory(int weight, String displayName, String description) {
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