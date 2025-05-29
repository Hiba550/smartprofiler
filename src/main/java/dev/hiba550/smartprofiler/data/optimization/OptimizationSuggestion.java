package dev.hiba550.smartprofiler.data.optimization;

import dev.hiba550.smartprofiler.data.analysis.AnalysisContext;
import dev.hiba550.smartprofiler.data.models.PerformanceIssue;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a detailed optimization suggestion with implementation guidance
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class OptimizationSuggestion {
    
    private final SuggestionType type;
    private final String title;
    private final String description;
    private final SuggestionCategory category;
    private final int priority; // 1-10 scale
    private final int difficulty; // 1-5 scale
    private final int estimatedImpact; // 1-5 scale
    private final int estimatedTimeMinutes;
    private final LocalDateTime createdAt;
    private final PerformanceIssue relatedIssue;
    private final AnalysisContext context;
    
    private final List<String> implementationSteps;
    private final List<String> warnings;
    private final List<String> requirements;
    private final Map<String, Object> relatedSettings;
    
    private final boolean canAutoImplement;
    private final Runnable autoImplementAction;
    
    // Application tracking
    private boolean applied = false;
    private LocalDateTime appliedAt;
    private int effectivenessRating = 0; // 1-5 stars
    private String userFeedback;
    
    private OptimizationSuggestion(Builder builder) {
        this.type = builder.type;
        this.title = builder.title;
        this.description = builder.description;
        this.category = builder.category;
        this.priority = builder.priority;
        this.difficulty = builder.difficulty;
        this.estimatedImpact = builder.estimatedImpact;
        this.estimatedTimeMinutes = builder.estimatedTimeMinutes;
        this.createdAt = LocalDateTime.now();
        this.relatedIssue = builder.relatedIssue;
        this.context = builder.context;
        
        this.implementationSteps = Collections.unmodifiableList(new ArrayList<>(builder.implementationSteps));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.requirements = Collections.unmodifiableList(new ArrayList<>(builder.requirements));
        this.relatedSettings = Collections.unmodifiableMap(new HashMap<>(builder.relatedSettings));
        
        this.canAutoImplement = builder.canAutoImplement;
        this.autoImplementAction = builder.autoImplementAction;
    }
    
    // Getters
    public SuggestionType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public SuggestionCategory getCategory() { return category; }
    public int getPriority() { return priority; }
    public int getDifficulty() { return difficulty; }
    public int getEstimatedImpact() { return estimatedImpact; }
    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public PerformanceIssue getRelatedIssue() { return relatedIssue; }
    public AnalysisContext getContext() { return context; }
    
    public List<String> getImplementationSteps() { return implementationSteps; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getRequirements() { return requirements; }
    public Map<String, Object> getRelatedSettings() { return relatedSettings; }
    
    public boolean canAutoImplement() { return canAutoImplement; }
    public boolean isApplied() { return applied; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public int getEffectivenessRating() { return effectivenessRating; }
    public String getUserFeedback() { return userFeedback; }
    
    /**
     * Marks this suggestion as applied
     */
    public void markAsApplied() {
        this.applied = true;
        this.appliedAt = LocalDateTime.now();
    }
    
    /**
     * Records user feedback on effectiveness
     */
    public void recordFeedback(int rating, String feedback) {
        this.effectivenessRating = Math.max(1, Math.min(5, rating));
        this.userFeedback = feedback;
    }
    
    /**
     * Executes auto-implementation if available and safe
     */
    public boolean executeAutoImplementation() {
        if (!canAutoImplement || autoImplementAction == null) {
            return false;
        }
        
        try {
            autoImplementAction.run();
            markAsApplied();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets a user-friendly difficulty description
     */
    public String getDifficultyDescription() {
        return switch (difficulty) {
            case 1 -> "Very Easy (1-2 minutes)";
            case 2 -> "Easy (2-5 minutes)";
            case 3 -> "Medium (5-15 minutes)";
            case 4 -> "Hard (15-30 minutes)";
            case 5 -> "Very Hard (30+ minutes)";
            default -> "Unknown";
        };
    }
    
    /**
     * Gets a user-friendly impact description
     */
    public String getImpactDescription() {
        return switch (estimatedImpact) {
            case 1 -> "Low Impact";
            case 2 -> "Minor Impact";
            case 3 -> "Moderate Impact";
            case 4 -> "High Impact";
            case 5 -> "Very High Impact";
            default -> "Unknown Impact";
        };
    }
    
    /**
     * Gets the age of this suggestion in minutes
     */
    public long getAgeMinutes() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Checks if this suggestion has expired (older than 24 hours)
     */
    public boolean isExpired() {
        return getAgeMinutes() > 1440; // 24 hours
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OptimizationSuggestion that = (OptimizationSuggestion) obj;
        return type == that.type && Objects.equals(relatedIssue, that.relatedIssue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, relatedIssue);
    }
    
    @Override
    public String toString() {
        return String.format("OptimizationSuggestion{type=%s, priority=%d, title='%s'}", 
            type, priority, title);
    }
    
    /**
     * Builder pattern for creating optimization suggestions
     */
    public static class Builder {
        private SuggestionType type;
        private String title;
        private String description;
        private SuggestionCategory category;
        private int priority = 5;
        private int difficulty = 3;
        private int estimatedImpact = 3;
        private int estimatedTimeMinutes = 10;
        private PerformanceIssue relatedIssue;
        private AnalysisContext context;
        
        private final List<String> implementationSteps = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> requirements = new ArrayList<>();
        private final Map<String, Object> relatedSettings = new HashMap<>();
        
        private boolean canAutoImplement = false;
        private Runnable autoImplementAction;
        
        public Builder type(SuggestionType type) {
            this.type = type;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder category(SuggestionCategory category) {
            this.category = category;
            return this;
        }
        
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder difficulty(int difficulty) {
            this.difficulty = difficulty;
            return this;
        }
        
        public Builder estimatedImpact(int estimatedImpact) {
            this.estimatedImpact = estimatedImpact;
            return this;
        }
        
        public Builder estimatedTime(int estimatedTimeMinutes) {
            this.estimatedTimeMinutes = estimatedTimeMinutes;
            return this;
        }
        
        public Builder relatedIssue(PerformanceIssue relatedIssue) {
            this.relatedIssue = relatedIssue;
            return this;
        }
        
        public Builder context(AnalysisContext context) {
            this.context = context;
            return this;
        }
        
        public Builder addImplementationStep(String step) {
            this.implementationSteps.add(step);
            return this;
        }
        
        public Builder addWarning(String warning) {
            this.warnings.add(warning);
            return this;
        }
        
        public Builder addRequirement(String requirement) {
            this.requirements.add(requirement);
            return this;
        }
        
        public Builder addRelatedSetting(String key, Object value) {
            this.relatedSettings.put(key, value);
            return this;
        }
        
        public Builder canAutoImplement(boolean canAutoImplement) {
            this.canAutoImplement = canAutoImplement;
            return this;
        }
        
        public Builder autoImplementAction(Runnable action) {
            this.autoImplementAction = action;
            return this;
        }
        
        public OptimizationSuggestion build() {
            Objects.requireNonNull(type, "Suggestion type is required");
            Objects.requireNonNull(title, "Title is required");
            Objects.requireNonNull(description, "Description is required");
            Objects.requireNonNull(category, "Category is required");
            
            return new OptimizationSuggestion(this);
        }
    }
}