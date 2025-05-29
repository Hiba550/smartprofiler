package dev.hiba550.smartprofiler.data.analysis;

import dev.hiba550.smartprofiler.data.models.PerformanceIssue;

import java.util.Map;

/**
 * Comprehensive statistics about bottleneck analysis performance
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public record AnalysisStatistics(
    int totalAnalysisCount,
    long lastAnalysisTime,
    double averageAnalysisTime,
    Map<PerformanceIssue, Long> issueOccurrenceCounts
) {
    
    /**
     * Gets the analysis rate in analyses per second
     */
    public double getAnalysisRate() {
        if (lastAnalysisTime == 0 || totalAnalysisCount == 0) {
            return 0.0;
        }
        
        long uptimeMs = System.currentTimeMillis() - (lastAnalysisTime - totalAnalysisCount * 50); // Approximate
        return (totalAnalysisCount * 1000.0) / uptimeMs;
    }
    
    /**
     * Gets the most frequently occurring issue
     */
    public PerformanceIssue getMostFrequentIssue() {
        return issueOccurrenceCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Gets the total number of issues detected
     */
    public long getTotalIssuesDetected() {
        return issueOccurrenceCounts.values().stream()
            .mapToLong(Long::longValue)
            .sum();
    }
}