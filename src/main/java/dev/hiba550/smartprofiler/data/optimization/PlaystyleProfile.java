package dev.hiba550.smartprofiler.data.optimization;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks user playstyle and preferences to generate personalized suggestions
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class PlaystyleProfile {
    
    // Playstyle tracking
    private final AtomicLong totalPlayTime = new AtomicLong(0);
    private final AtomicInteger sessionCount = new AtomicInteger(0);
    private final Map<PlaystyleMetric, Double> metrics = new EnumMap<>(PlaystyleMetric.class);
    
    // Preference tracking
    private final Map<SuggestionCategory, Integer> categoryPreferences = new EnumMap<>(SuggestionCategory.class);
    private final Map<SuggestionType, Integer> suggestionPreferences = new EnumMap<>(SuggestionType.class);
    
    // Activity tracking
    private volatile boolean preferredVisualQuality = true;
    private volatile boolean preferredPerformance = true;
    private volatile int preferredRenderDistance = 12;
    private volatile GraphicsPreference graphicsPreference = GraphicsPreference.BALANCED;
    
    private LocalDateTime lastUpdated = LocalDateTime.now();
    
    public PlaystyleProfile() {
        initializeDefaults();
    }
    
    /**
     * Initializes default values for all metrics
     */
    private void initializeDefaults() {
        for (PlaystyleMetric metric : PlaystyleMetric.values()) {
            metrics.put(metric, 0.0);
        }
        
        for (SuggestionCategory category : SuggestionCategory.values()) {
            categoryPreferences.put(category, 0);
        }
    }
    
    /**
     * Records a gaming session for profile learning
     */
    public void recordSession(SessionData sessionData) {
        sessionCount.incrementAndGet();
        totalPlayTime.addAndGet(sessionData.durationMinutes());
        
        // Update metrics based on session data
        updateMetrics(sessionData);
        
        // Learn from user behavior during session
        learnFromBehavior(sessionData);
        
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Updates playstyle metrics based on session data
     */
    private void updateMetrics(SessionData sessionData) {
        // Building intensity (based on block place/break count)
        double buildingIntensity = Math.min(1.0, sessionData.blockInteractions() / 1000.0);
        updateMetric(PlaystyleMetric.BUILDING_INTENSITY, buildingIntensity);
        
        // Exploration activity (based on chunks visited)
        double explorationActivity = Math.min(1.0, sessionData.chunksVisited() / 100.0);
        updateMetric(PlaystyleMetric.EXPLORATION_ACTIVITY, explorationActivity);
        
        // Combat activity (based on mob interactions)
        double combatActivity = Math.min(1.0, sessionData.mobInteractions() / 50.0);
        updateMetric(PlaystyleMetric.COMBAT_ACTIVITY, combatActivity);
        
        // Redstone complexity (based on redstone components)
        double redstoneComplexity = Math.min(1.0, sessionData.redstoneComponents() / 20.0);
        updateMetric(PlaystyleMetric.REDSTONE_COMPLEXITY, redstoneComplexity);
        
        // Social activity (multiplayer indicators)
        double socialActivity = sessionData.isMultiplayer() ? 1.0 : 0.0;
        updateMetric(PlaystyleMetric.SOCIAL_ACTIVITY, socialActivity);
    }
    
    /**
     * Updates a metric using exponential moving average
     */
    private void updateMetric(PlaystyleMetric metric, double newValue) {
        double currentValue = metrics.get(metric);
        double alpha = 0.1; // Learning rate
        double updatedValue = alpha * newValue + (1 - alpha) * currentValue;
        metrics.put(metric, updatedValue);
    }
    
    /**
     * Learns from user behavior and preferences
     */
    private void learnFromBehavior(SessionData sessionData) {
        // Learn graphics preferences from settings used
        if (sessionData.averageFps() > 60 && sessionData.graphicsSettings().equals("fancy")) {
            graphicsPreference = GraphicsPreference.QUALITY;
            preferredVisualQuality = true;
        } else if (sessionData.averageFps() < 30) {
            graphicsPreference = GraphicsPreference.PERFORMANCE;
            preferredPerformance = true;
        }
        
        // Learn render distance preferences
        if (sessionData.renderDistance() != preferredRenderDistance) {
            // User manually changed render distance, learn from it
            preferredRenderDistance = sessionData.renderDistance();
        }
    }
    
    /**
     * Records user response to a suggestion
     */
    public void recordSuggestionResponse(SuggestionType suggestionType, SuggestionResponse response) {
        SuggestionCategory category = getSuggestionCategory(suggestionType);
        
        int categoryScore = categoryPreferences.get(category);
        int suggestionScore = suggestionPreferences.getOrDefault(suggestionType, 0);
        
        switch (response) {
            case APPLIED_HELPFUL -> {
                categoryPreferences.put(category, categoryScore + 2);
                suggestionPreferences.put(suggestionType, suggestionScore + 3);
            }
            case APPLIED_NEUTRAL -> {
                categoryPreferences.put(category, categoryScore + 1);
                suggestionPreferences.put(suggestionType, suggestionScore + 1);
            }
            case DISMISSED -> {
                categoryPreferences.put(category, categoryScore - 1);
                suggestionPreferences.put(suggestionType, suggestionScore - 2);
            }
            case MARKED_UNHELPFUL -> {
                categoryPreferences.put(category, categoryScore - 2);
                suggestionPreferences.put(suggestionType, suggestionScore - 3);
            }
        }
        
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Gets user preference score for a suggestion category (higher = more preferred)
     */
    public int getCategoryPreference(SuggestionCategory category) {
        return categoryPreferences.getOrDefault(category, 0);
    }
    
    /**
     * Gets user preference score for a specific suggestion type
     */
    public int getSuggestionPreference(SuggestionType suggestionType) {
        return suggestionPreferences.getOrDefault(suggestionType, 0);
    }
    
    /**
     * Gets the value of a playstyle metric (0.0 to 1.0)
     */
    public double getMetric(PlaystyleMetric metric) {
        return metrics.getOrDefault(metric, 0.0);
    }
    
    /**
     * Determines if user prefers performance over visual quality
     */
    public boolean prefersPerformanceOverQuality() {
        return graphicsPreference == GraphicsPreference.PERFORMANCE || 
               (graphicsPreference == GraphicsPreference.BALANCED && preferredPerformance);
    }
    
    /**
     * Gets recommended suggestion types based on playstyle
     */
    public java.util.List<SuggestionType> getRecommendedSuggestionTypes() {
        java.util.List<SuggestionType> recommendations = new java.util.ArrayList<>();
        
        // High building activity - suggest chunk optimization
        if (getMetric(PlaystyleMetric.BUILDING_INTENSITY) > 0.7) {
            recommendations.add(SuggestionType.REDUCE_LOADED_CHUNKS);
            recommendations.add(SuggestionType.OPTIMIZE_ENTITY_RENDERING);
        }
        
        // High exploration - suggest render distance optimization
        if (getMetric(PlaystyleMetric.EXPLORATION_ACTIVITY) > 0.7) {
            recommendations.add(SuggestionType.REDUCE_RENDER_DISTANCE);
            recommendations.add(SuggestionType.OPTIMIZE_WORLD_GENERATION);
        }
        
        // High redstone complexity - suggest performance optimizations
        if (getMetric(PlaystyleMetric.REDSTONE_COMPLEXITY) > 0.5) {
            recommendations.add(SuggestionType.OPTIMIZE_CPU_SETTINGS);
            recommendations.add(SuggestionType.ADJUST_MEMORY_ALLOCATION);
        }
        
        // Multiplayer activity - suggest network optimizations
        if (getMetric(PlaystyleMetric.SOCIAL_ACTIVITY) > 0.5) {
            recommendations.add(SuggestionType.OPTIMIZE_NETWORK_SETTINGS);
            recommendations.add(SuggestionType.REDUCE_NETWORK_USAGE);
        }
        
        return recommendations;
    }
    
    /**
     * Gets playstyle classification based on metrics
     */
    public PlaystyleType getPlaystyleType() {
        double building = getMetric(PlaystyleMetric.BUILDING_INTENSITY);
        double exploration = getMetric(PlaystyleMetric.EXPLORATION_ACTIVITY);
        double combat = getMetric(PlaystyleMetric.COMBAT_ACTIVITY);
        double redstone = getMetric(PlaystyleMetric.REDSTONE_COMPLEXITY);
        double social = getMetric(PlaystyleMetric.SOCIAL_ACTIVITY);
        
        // Determine primary playstyle
        if (building > 0.7) return PlaystyleType.BUILDER;
        if (exploration > 0.7) return PlaystyleType.EXPLORER;
        if (combat > 0.7) return PlaystyleType.SURVIVOR;
        if (redstone > 0.6) return PlaystyleType.ENGINEER;
        if (social > 0.5) return PlaystyleType.SOCIAL;
        
        // Check for hybrid types
        if (building > 0.5 && exploration > 0.5) return PlaystyleType.CREATIVE;
        if (combat > 0.5 && exploration > 0.5) return PlaystyleType.ADVENTURER;
        
        return PlaystyleType.CASUAL;
    }
    
    private SuggestionCategory getSuggestionCategory(SuggestionType suggestionType) {
        // This would map suggestion types to categories
        // Implementation similar to OptimizationEngine.getSuggestionCategory()
        return SuggestionCategory.GENERAL; // Simplified for example
    }
    
    // Getters for profile data
    public long getTotalPlayTimeMinutes() { return totalPlayTime.get(); }
    public int getSessionCount() { return sessionCount.get(); }
    public GraphicsPreference getGraphicsPreference() { return graphicsPreference; }
    public int getPreferredRenderDistance() { return preferredRenderDistance; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    
    // Supporting enums and records
    
    public enum PlaystyleMetric {
        BUILDING_INTENSITY,
        EXPLORATION_ACTIVITY,
        COMBAT_ACTIVITY,
        REDSTONE_COMPLEXITY,
        SOCIAL_ACTIVITY
    }
    
    public enum GraphicsPreference {
        PERFORMANCE, BALANCED, QUALITY
    }
    
    public enum PlaystyleType {
        BUILDER, EXPLORER, SURVIVOR, ENGINEER, SOCIAL, CREATIVE, ADVENTURER, CASUAL
    }
    
    public enum SuggestionResponse {
        APPLIED_HELPFUL, APPLIED_NEUTRAL, DISMISSED, MARKED_UNHELPFUL
    }
    
    public record SessionData(
        int durationMinutes,
        int blockInteractions,
        int chunksVisited,
        int mobInteractions,
        int redstoneComponents,
        boolean isMultiplayer,
        double averageFps,
        String graphicsSettings,
        int renderDistance
    ) {}
}