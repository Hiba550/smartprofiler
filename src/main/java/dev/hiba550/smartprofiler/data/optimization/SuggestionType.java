package dev.hiba550.smartprofiler.data.optimization;

/**
 * Comprehensive list of optimization suggestion types for Minecraft 1.21.5
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum SuggestionType {
    // Graphics Optimizations
    REDUCE_RENDER_DISTANCE("Reduce render distance for better FPS"),
    OPTIMIZE_GRAPHICS_SETTINGS("Optimize graphics settings for performance"),
    DISABLE_SHADERS("Disable shaders to improve performance"),
    REDUCE_PARTICLES("Reduce particle effects"),
    DISABLE_SMOOTH_LIGHTING("Disable smooth lighting"),
    OPTIMIZE_TEXTURE_PACK("Use optimized texture pack"),
    OPTIMIZE_ENTITY_RENDERING("Optimize entity rendering distance"),
    
    // Memory Optimizations
    ADJUST_MEMORY_ALLOCATION("Adjust JVM memory allocation"),
    REDUCE_MEMORY_USAGE("Reduce overall memory usage"),
    CLEANUP_WORLD_DATA("Clean up world data and unused chunks"),
    RESTART_GAME("Restart Minecraft to clear memory"),
    CLOSE_UNNECESSARY_APPLICATIONS("Close unnecessary background applications"),
    
    // World Optimizations
    REDUCE_LOADED_CHUNKS("Reduce number of loaded chunks"),
    REDUCE_SIMULATION_DISTANCE("Reduce simulation distance"),
    OPTIMIZE_WORLD_GENERATION("Optimize world generation settings"),
    REDUCE_ENTITY_COUNT("Reduce entity count in world"),
    IMPLEMENT_ENTITY_CULLING("Implement entity culling optimization"),
    ADJUST_SPAWNING_SETTINGS("Adjust mob spawning settings"),
    
    // System Optimizations
    OPTIMIZE_JVM_FLAGS("Optimize JVM launch flags"),
    UPDATE_JAVA_VERSION("Update to newer Java version"),
    UPDATE_DRIVERS("Update graphics drivers"),
    ENABLE_HARDWARE_ACCELERATION("Enable hardware acceleration"),
    OPTIMIZE_CPU_SETTINGS("Optimize CPU-related settings"),
    
    // Mod Optimizations
    DISABLE_PROBLEMATIC_MODS("Disable mods causing performance issues"),
    UPDATE_MODS("Update mods to latest versions"),
    
    // Network Optimizations
    OPTIMIZE_NETWORK_SETTINGS("Optimize network settings"),
    REDUCE_NETWORK_USAGE("Reduce network bandwidth usage"),
    CHECK_INTERNET_CONNECTION("Check internet connection stability"),
    CHANGE_SERVER("Consider changing to a different server"),
    
    // General Suggestions
    REPORT_BUG("Report performance bug to mod developers"),
    GENERAL_PERFORMANCE_TIPS("Apply general performance tips");
    
    private final String description;
    
    SuggestionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}