package dev.hiba550.smartprofiler.client.overlay;

/**
 * Defines different overlay display modes for the performance profiler
 * 
 * Each mode provides different levels of detail and visual complexity:
 * - MINIMAL: Just essential metrics (FPS, Memory)
 * - COMPACT: Main performance indicators
 * - DETAILED: Comprehensive metrics with graphs
 * - GRAPH_ONLY: Visual graphs only for minimal HUD impact
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum OverlayMode {
    /**
     * Minimal overlay showing only FPS and memory usage
     * Best for competitive gameplay where HUD space is critical
     */
    MINIMAL("Minimal", "Shows only FPS and memory usage"),
    
    /**
     * Compact overlay with main performance metrics
     * Good balance between information and screen space
     */
    COMPACT("Compact", "Main performance metrics in compact layout"),
    
    /**
     * Detailed overlay with comprehensive performance data
     * Includes graphs, warnings, and detailed statistics
     */
    DETAILED("Detailed", "Comprehensive performance analysis with graphs"),
    
    /**
     * Graph-only overlay for visual performance tracking
     * Minimal text, focuses on visual trend representation
     */
    GRAPH_ONLY("Graphs Only", "Visual performance graphs without text metrics");
    
    private final String displayName;
    private final String description;
    
    OverlayMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the next mode in the cycle
     */
    public OverlayMode getNext() {
        OverlayMode[] modes = values();
        int currentIndex = this.ordinal();
        return modes[(currentIndex + 1) % modes.length];
    }
    
    /**
     * Gets the previous mode in the cycle
     */
    public OverlayMode getPrevious() {
        OverlayMode[] modes = values();
        int currentIndex = this.ordinal();
        return modes[(currentIndex - 1 + modes.length) % modes.length];
    }
}