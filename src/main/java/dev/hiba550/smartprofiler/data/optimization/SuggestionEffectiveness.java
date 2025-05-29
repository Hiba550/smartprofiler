package dev.hiba550.smartprofiler.data.optimization;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks the effectiveness of optimization suggestions over time
 * Learns from user feedback to improve future recommendations
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class SuggestionEffectiveness {
    
    private final SuggestionType suggestionType;
    private final List<EffectivenessRating> ratings;
    private final AtomicInteger totalApplications;
    private final AtomicLong totalEffectivenessPoints;
    
    // Learning parameters
    private volatile double currentScore = 3.0; // Start with neutral score
    private volatile double confidence = 0.0; // Confidence in the score (0-1)
    private LocalDateTime lastUpdated;
    
    public SuggestionEffectiveness(SuggestionType suggestionType) {
        this.suggestionType = suggestionType;
        this.ratings = new ArrayList<>();
        this.totalApplications = new AtomicInteger(0);
        this.totalEffectivenessPoints = new AtomicLong(0);
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Adds a new effectiveness rating from user feedback
     * 
     * @param rating 1-5 star rating
     * @param feedback Optional text feedback
     */
    public synchronized void addRating(int rating, String feedback) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        EffectivenessRating effectivenessRating = new EffectivenessRating(
            rating, feedback, LocalDateTime.now()
        );
        
        ratings.add(effectivenessRating);
        totalApplications.incrementAndGet();
        totalEffectivenessPoints.addAndGet(rating);
        
        // Update current score with weighted average
        updateCurrentScore();
        updateConfidence();
        
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Updates the current effectiveness score using weighted average
     * Recent ratings have more weight than older ones
     */
    private void updateCurrentScore() {
        if (ratings.isEmpty()) {
            currentScore = 3.0;
            return;
        }
        
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        LocalDateTime now = LocalDateTime.now();
        
        for (EffectivenessRating rating : ratings) {
            // Calculate age-based weight (newer ratings have more weight)
            long daysOld = java.time.Duration.between(rating.timestamp(), now).toDays();
            double weight = Math.exp(-daysOld / 30.0); // Exponential decay over 30 days
            
            weightedSum += rating.rating() * weight;
            totalWeight += weight;
        }
        
        if (totalWeight > 0) {
            currentScore = weightedSum / totalWeight;
        }
    }
    
    /**
     * Updates confidence based on number of ratings and consistency
     */
    private void updateConfidence() {
        int ratingCount = ratings.size();
        
        if (ratingCount == 0) {
            confidence = 0.0;
            return;
        }
        
        // Base confidence from sample size
        double sampleConfidence = Math.min(1.0, ratingCount / 10.0); // Max confidence at 10 ratings
        
        // Reduce confidence if ratings are inconsistent
        double variance = calculateRatingVariance();
        double consistencyFactor = Math.max(0.1, 1.0 - (variance / 4.0)); // Variance penalty
        
        confidence = sampleConfidence * consistencyFactor;
    }
    
    /**
     * Calculates variance in ratings to measure consistency
     */
    private double calculateRatingVariance() {
        if (ratings.size() < 2) {
            return 0.0;
        }
        
        double mean = getAverageEffectiveness();
        double sumSquaredDiffs = 0.0;
        
        for (EffectivenessRating rating : ratings) {
            double diff = rating.rating() - mean;
            sumSquaredDiffs += diff * diff;
        }
        
        return sumSquaredDiffs / ratings.size();
    }
    
    /**
     * Gets the current effectiveness score (1-5 scale)
     */
    public double getCurrentScore() {
        return currentScore;
    }
    
    /**
     * Gets confidence in the current score (0-1 scale)
     */
    public double getConfidence() {
        return confidence;
    }
    
    /**
     * Gets the average effectiveness from all ratings
     */
    public double getAverageEffectiveness() {
        int applications = totalApplications.get();
        if (applications == 0) {
            return 3.0; // Neutral default
        }
        
        return (double) totalEffectivenessPoints.get() / applications;
    }
    
    /**
     * Gets the number of times this suggestion has been applied
     */
    public int getApplicationCount() {
        return totalApplications.get();
    }
    
    /**
     * Gets the most recent ratings (up to specified count)
     */
    public List<EffectivenessRating> getRecentRatings(int count) {
        int size = ratings.size();
        if (size <= count) {
            return new ArrayList<>(ratings);
        }
        
        return new ArrayList<>(ratings.subList(size - count, size));
    }
    
    /**
     * Checks if this suggestion is considered effective
     * Based on score and confidence thresholds
     */
    public boolean isEffective() {
        return currentScore >= 3.5 && confidence >= 0.3;
    }
    
    /**
     * Checks if this suggestion should be recommended
     * Considers both effectiveness and confidence
     */
    public boolean shouldRecommend() {
        // Always recommend if we don't have enough data
        if (confidence < 0.2) {
            return true;
        }
        
        // Recommend if score is good and we're confident
        return currentScore >= 3.0;
    }
    
    /**
     * Gets effectiveness trend over time
     */
    public EffectivenessTrend getTrend() {
        if (ratings.size() < 3) {
            return EffectivenessTrend.UNKNOWN;
        }
        
        // Compare recent ratings to older ones
        List<EffectivenessRating> recent = getRecentRatings(Math.min(5, ratings.size() / 2));
        List<EffectivenessRating> older = ratings.subList(0, ratings.size() - recent.size());
        
        double recentAvg = recent.stream().mapToInt(EffectivenessRating::rating).average().orElse(3.0);
        double olderAvg = older.stream().mapToInt(EffectivenessRating::rating).average().orElse(3.0);
        
        double difference = recentAvg - olderAvg;
        
        if (difference > 0.5) return EffectivenessTrend.IMPROVING;
        if (difference < -0.5) return EffectivenessTrend.DECLINING;
        return EffectivenessTrend.STABLE;
    }
    
    /**
     * Gets a summary of this suggestion's effectiveness
     */
    public EffectivenessSummary getSummary() {
        return new EffectivenessSummary(
            suggestionType,
            currentScore,
            confidence,
            totalApplications.get(),
            getTrend(),
            lastUpdated,
            isEffective(),
            shouldRecommend()
        );
    }
    
    // Record for individual effectiveness ratings
    public record EffectivenessRating(
        int rating,
        String feedback,
        LocalDateTime timestamp
    ) {}
    
    // Enum for effectiveness trends
    public enum EffectivenessTrend {
        IMPROVING("Effectiveness improving over time"),
        STABLE("Effectiveness stable"),
        DECLINING("Effectiveness declining over time"),
        UNKNOWN("Not enough data for trend analysis");
        
        private final String description;
        
        EffectivenessTrend(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Record for effectiveness summary
    public record EffectivenessSummary(
        SuggestionType suggestionType,
        double currentScore,
        double confidence,
        int applicationCount,
        EffectivenessTrend trend,
        LocalDateTime lastUpdated,
        boolean isEffective,
        boolean shouldRecommend
    ) {}
}