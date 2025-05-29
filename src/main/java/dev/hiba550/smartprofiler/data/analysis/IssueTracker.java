package dev.hiba550.smartprofiler.data.analysis;

import dev.hiba550.smartprofiler.data.models.PerformanceIssue;
import dev.hiba550.smartprofiler.util.RingBuffer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks occurrences and patterns of specific performance issues
 * 
 * Features:
 * - Occurrence frequency tracking
 * - Duration analysis
 * - Pattern recognition
 * - Statistical analysis of issue data
 * - Thread-safe implementation
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class IssueTracker {
    
    private static final int DATA_BUFFER_SIZE = 1000;
    private static final int TIMESTAMP_BUFFER_SIZE = 500;
    
    private final PerformanceIssue issue;
    private final RingBuffer<Float> dataPoints;
    private final RingBuffer<Long> occurrenceTimestamps;
    private final RingBuffer<Long> resolutionTimestamps;
    
    // Statistics tracking
    private final AtomicLong totalOccurrences = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);
    private volatile long firstOccurrence = 0;
    private volatile long lastOccurrence = 0;
    private volatile long lastResolution = 0;
    
    // Analysis cache
    private volatile double cachedMean = 0.0;
    private volatile double cachedStandardDeviation = 0.0;
    private volatile long lastStatisticsUpdate = 0;
    private static final long STATISTICS_CACHE_DURATION = 1000; // 1 second
    
    public IssueTracker(PerformanceIssue issue) {
        this.issue = issue;
        this.dataPoints = new RingBuffer<>(DATA_BUFFER_SIZE);
        this.occurrenceTimestamps = new RingBuffer<>(TIMESTAMP_BUFFER_SIZE);
        this.resolutionTimestamps = new RingBuffer<>(TIMESTAMP_BUFFER_SIZE);
    }
    
    /**
     * Adds a data point for analysis (e.g., FPS value, memory percentage)
     */
    public void addDataPoint(float value) {
        dataPoints.add(value);
        invalidateStatisticsCache();
    }
    
    /**
     * Records an occurrence of this issue
     */
    public void recordOccurrence(long timestamp) {
        occurrenceTimestamps.add(timestamp);
        totalOccurrences.incrementAndGet();
        
        if (firstOccurrence == 0) {
            firstOccurrence = timestamp;
        }
        lastOccurrence = timestamp;
    }
    
    /**
     * Records when this issue was resolved
     */
    public void recordResolution(long timestamp) {
        resolutionTimestamps.add(timestamp);
        lastResolution = timestamp;
        
        // Calculate duration if we have a recent occurrence
        if (lastOccurrence > 0 && timestamp > lastOccurrence) {
            long duration = timestamp - lastOccurrence;
            totalDuration.addAndGet(duration);
        }
    }
    
    /**
     * Gets the total number of occurrences
     */
    public long getOccurrenceCount() {
        return totalOccurrences.get();
    }
    
    /**
     * Gets the number of data points collected
     */
    public int getDataPointCount() {
        return dataPoints.size();
    }
    
    /**
     * Gets the associated performance issue
     */
    public PerformanceIssue getIssue() {
        return issue;
    }
    
    /**
     * Calculates the mean value of collected data points
     */
    public double getMeanValue() {
        updateStatisticsCache();
        return cachedMean;
    }
    
    /**
     * Calculates the standard deviation of collected data points
     */
    public double getStandardDeviation() {
        updateStatisticsCache();
        return cachedStandardDeviation;
    }
    
    /**
     * Gets the recent average over the specified number of data points
     */
    public double getRecentAverage(int count) {
        if (dataPoints.isEmpty()) {
            return 0.0;
        }
        
        count = Math.min(count, dataPoints.size());
        double sum = 0.0;
        
        for (int i = 0; i < count; i++) {
            Float value = dataPoints.get(dataPoints.size() - 1 - i);
            if (value != null) {
                sum += value;
            }
        }
        
        return sum / count;
    }
    
    /**
     * Calculates the trend (slope) over the specified number of data points
     * Positive values indicate increasing trend, negative values decreasing
     */
    public double calculateTrend(int count) {
        if (dataPoints.size() < 2) {
            return 0.0;
        }
        
        count = Math.min(count, dataPoints.size());
        
        // Linear regression to calculate trend
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = 0;
        
        for (int i = 0; i < count; i++) {
            Float value = dataPoints.get(dataPoints.size() - count + i);
            if (value != null) {
                double x = i;
                double y = value;
                
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
                n++;
            }
        }
        
        if (n < 2) {
            return 0.0;
        }
        
        // Calculate slope (trend)
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }
    
    /**
     * Gets the frequency of occurrences per minute
     */
    public double getOccurrenceFrequency() {
        if (occurrenceTimestamps.isEmpty() || firstOccurrence == 0) {
            return 0.0;
        }
        
        long timespan = lastOccurrence - firstOccurrence;
        if (timespan <= 0) {
            return 0.0;
        }
        
        // Convert to occurrences per minute
        double minutes = timespan / (1000.0 * 60.0);
        return totalOccurrences.get() / minutes;
    }
    
    /**
     * Gets the average duration of this issue in milliseconds
     */
    public double getAverageDuration() {
        long occurrences = totalOccurrences.get();
        if (occurrences == 0) {
            return 0.0;
        }
        
        return (double) totalDuration.get() / occurrences;
    }
    
    /**
     * Checks if this issue is currently active
     */
    public boolean isCurrentlyActive() {
        return lastOccurrence > lastResolution;
    }
    
    /**
     * Gets the time since last occurrence in milliseconds
     */
    public long getTimeSinceLastOccurrence() {
        if (lastOccurrence == 0) {
            return Long.MAX_VALUE;
        }
        return System.currentTimeMillis() - lastOccurrence;
    }
    
    /**
     * Gets the time since last resolution in milliseconds
     */
    public long getTimeSinceLastResolution() {
        if (lastResolution == 0) {
            return Long.MAX_VALUE;
        }
        return System.currentTimeMillis() - lastResolution;
    }
    
    /**
     * Updates the statistics cache if needed
     */
    private void updateStatisticsCache() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastStatisticsUpdate < STATISTICS_CACHE_DURATION) {
            return; // Cache is still valid
        }
        
        calculateStatistics();
        lastStatisticsUpdate = currentTime;
    }
    
    /**
     * Calculates statistics for cached values
     */
    private void calculateStatistics() {
        if (dataPoints.isEmpty()) {
            cachedMean = 0.0;
            cachedStandardDeviation = 0.0;
            return;
        }
        
        // Calculate mean
        double sum = 0.0;
        int count = 0;
        
        for (int i = 0; i < dataPoints.size(); i++) {
            Float value = dataPoints.get(i);
            if (value != null) {
                sum += value;
                count++;
            }
        }
        
        cachedMean = count > 0 ? sum / count : 0.0;
        
        // Calculate standard deviation
        if (count > 1) {
            double sumSquaredDifferences = 0.0;
            
            for (int i = 0; i < dataPoints.size(); i++) {
                Float value = dataPoints.get(i);
                if (value != null) {
                    double difference = value - cachedMean;
                    sumSquaredDifferences += difference * difference;
                }
            }
            
            cachedStandardDeviation = Math.sqrt(sumSquaredDifferences / (count - 1));
        } else {
            cachedStandardDeviation = 0.0;
        }
    }
    
    /**
     * Invalidates the statistics cache
     */
    private void invalidateStatisticsCache() {
        lastStatisticsUpdate = 0;
    }
    
    /**
     * Resets all tracking data
     */
    public void reset() {
        dataPoints.clear();
        occurrenceTimestamps.clear();
        resolutionTimestamps.clear();
        
        totalOccurrences.set(0);
        totalDuration.set(0);
        firstOccurrence = 0;
        lastOccurrence = 0;
        lastResolution = 0;
        
        invalidateStatisticsCache();
    }
    
    /**
     * Gets a summary of this tracker's data
     */
    public IssueTrackerSummary getSummary() {
        return new IssueTrackerSummary(
            issue,
            getOccurrenceCount(),
            getDataPointCount(),
            getMeanValue(),
            getStandardDeviation(),
            getOccurrenceFrequency(),
            getAverageDuration(),
            isCurrentlyActive(),
            getTimeSinceLastOccurrence()
        );
    }
}