package dev.hiba550.smartprofiler.data.analysis;

/**
 * Context information for performance analysis
 * Helps adjust thresholds and analysis based on current game state
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum AnalysisContext {
    UNKNOWN("Unknown", 1.0f),
    NORMAL_GAMEPLAY("Normal Gameplay", 1.0f),
    HEAVY_WORLD("Heavy World Loading", 1.5f),
    ENTITY_HEAVY("Entity Heavy Environment", 1.3f),
    RENDER_INTENSIVE("Render Intensive Scene", 1.4f),
    SERVER_PROCESSING("Server Processing", 1.2f),
    MULTIPLAYER("Multiplayer Environment", 1.1f);
    
    private final String displayName;
    private final float thresholdMultiplier;
    
    AnalysisContext(String displayName, float thresholdMultiplier) {
        this.displayName = displayName;
        this.thresholdMultiplier = thresholdMultiplier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the threshold multiplier for this context
     * Values > 1.0 make the analysis more lenient (higher thresholds)
     */
    public float getThresholdMultiplier() {
        return thresholdMultiplier;
    }
}