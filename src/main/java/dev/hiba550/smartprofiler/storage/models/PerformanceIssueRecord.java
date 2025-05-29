package dev.hiba550.smartprofiler.storage.models;

import dev.hiba550.smartprofiler.data.models.PerformanceFrame;
import dev.hiba550.smartprofiler.data.models.PerformanceIssue;

/**
 * Database record for performance issues
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class PerformanceIssueRecord {
    private final PerformanceIssue issue;
    private final long timestamp;
    private final String description;
    private final double value;
    private final double threshold;
    private final Long frameId;
    
    public PerformanceIssueRecord(PerformanceIssue issue, PerformanceFrame frame, String description) {
        this.issue = issue;
        this.timestamp = frame.getTimestamp();
        this.description = description;
        this.value = extractValueFromFrame(issue, frame);
        this.threshold = getThresholdForIssue(issue);
        this.frameId = null; // Will be set during insertion
    }
    
    private double extractValueFromFrame(PerformanceIssue issue, PerformanceFrame frame) {
        return switch (issue) {
            case LOW_FPS, CRITICAL_FPS -> frame.getFps();
            case HIGH_MEMORY_USAGE, CRITICAL_MEMORY_USAGE -> frame.getMemoryStats().getHeapUsagePercent();
            case EXCESSIVE_CHUNKS -> frame.getChunkStats().getLoadedChunks();
            case EXCESSIVE_ENTITIES -> frame.getEntityStats().getTotalEntities();
            case RENDER_LAG -> frame.getRenderStats().getRenderTimeMs();
            case NETWORK_LAG -> frame.getNetworkStats().getLatencyMs();
            default -> 0.0;
        };
    }
    
    private double getThresholdForIssue(PerformanceIssue issue) {
        // Get thresholds from configuration
        return switch (issue) {
            case LOW_FPS -> dev.hiba550.smartprofiler.config.ProfilerConfig.getLowFpsThreshold();
            case CRITICAL_FPS -> dev.hiba550.smartprofiler.config.ProfilerConfig.getCriticalFpsThreshold();
            case HIGH_MEMORY_USAGE -> dev.hiba550.smartprofiler.config.ProfilerConfig.getHighMemoryThreshold();
            case CRITICAL_MEMORY_USAGE -> dev.hiba550.smartprofiler.config.ProfilerConfig.getCriticalMemoryThreshold();
            case EXCESSIVE_CHUNKS -> dev.hiba550.smartprofiler.config.ProfilerConfig.getExcessiveChunksThreshold();
            case EXCESSIVE_ENTITIES -> dev.hiba550.smartprofiler.config.ProfilerConfig.getExcessiveEntitiesThreshold();
            default -> 0.0;
        };
    }
    
    // Getters
    public PerformanceIssue getIssue() { return issue; }
    public long getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
    public double getValue() { return value; }
    public double getThreshold() { return threshold; }
    public Long getFrameId() { return frameId; }
}