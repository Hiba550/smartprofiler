package dev.hiba550.smartprofiler.data.optimization;

/**
 * Categories for organizing optimization suggestions
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum SuggestionCategory {
    GRAPHICS("Graphics & Rendering", "Optimizations related to visual rendering"),
    MEMORY("Memory Management", "Memory allocation and garbage collection optimizations"),
    WORLD("World & Gameplay", "World-related performance optimizations"),
    HARDWARE("Hardware & Drivers", "Hardware-specific optimizations"),
    NETWORK("Network & Multiplayer", "Network and server-related optimizations"),
    MODS("Mods & Add-ons", "Mod-related optimizations"),
    GENERAL("General", "General performance tips and miscellaneous optimizations");
    
    private final String displayName;
    private final String description;
    
    SuggestionCategory(String displayName, String description) {
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