package dev.hiba550.smartprofiler.data.optimization;

/**
 * Statistics about the optimization engine's performance and effectiveness
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public record OptimizationStatistics(
    long totalSuggestionsGenerated,
    long totalSuggestionsApplied,
    int pendingSuggestions,
    int trackedSuggestionTypes,
    HardwareProfile hardwareProfile,
    PlaystyleProfile playstyleProfile
) {
    
    /**
     * Gets the suggestion application rate (0.0 to 1.0)
     */
    public double getApplicationRate() {
        if (totalSuggestionsGenerated == 0) {
            return 0.0;
        }
        return (double) totalSuggestionsApplied / totalSuggestionsGenerated;
    }
    
    /**
     * Checks if the optimization engine is performing well
     */
    public boolean isPerformingWell() {
        return getApplicationRate() > 0.3 && pendingSuggestions < 10;
    }
    
    /**
     * Gets a user-friendly summary
     */
    public String getSummary() {
        return String.format(
            "Generated %d suggestions, %d applied (%.1f%% rate), %d pending",
            totalSuggestionsGenerated,
            totalSuggestionsApplied,
            getApplicationRate() * 100,
            pendingSuggestions
        );
    }
}