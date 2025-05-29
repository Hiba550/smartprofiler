package dev.hiba550.smartprofiler.data.analysis;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.config.ProfilerConfig;
import dev.hiba550.smartprofiler.data.models.*;
import dev.hiba550.smartprofiler.data.optimization.OptimizationSuggestion;
import dev.hiba550.smartprofiler.data.optimization.OptimizationEngine;
import dev.hiba550.smartprofiler.util.StatisticsTracker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Advanced bottleneck analysis engine for Minecraft 1.21.5
 * 
 * Features:
 * - Real-time performance issue detection
 * - Pattern recognition for recurring problems
 * - Severity assessment and prioritization
 * - Automated optimization suggestions
 * - Machine learning-inspired trend analysis
 * - Context-aware analysis based on game state
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class BottleneckAnalyzer {
    
    // Analysis thresholds - configurable via ProfilerConfig
    private static final int LOW_FPS_THRESHOLD = 30;
    private static final int CRITICAL_FPS_THRESHOLD = 15;
    private static final float HIGH_MEMORY_THRESHOLD = 80.0f; // percentage
    private static final float CRITICAL_MEMORY_THRESHOLD = 95.0f;
    private static final int EXCESSIVE_CHUNKS_THRESHOLD = 1000;
    private static final int EXCESSIVE_ENTITIES_THRESHOLD = 500;
    
    // Analysis windows for trend detection
    private static final int SHORT_TERM_WINDOW = 60;   // 3 seconds at 20 FPS
    private static final int MEDIUM_TERM_WINDOW = 200;  // 10 seconds
    private static final int LONG_TERM_WINDOW = 600;    // 30 seconds
    
    // Performance tracking
    private static final Map<PerformanceIssue, IssueTracker> issueTrackers = new ConcurrentHashMap<>();
    private static final StatisticsTracker performanceStats = new StatisticsTracker();
    private static final AtomicInteger totalAnalysisCount = new AtomicInteger(0);
    
    // Analysis state
    private static AnalysisContext currentContext = AnalysisContext.UNKNOWN;
    private static long lastAnalysisTime = 0;
    private static final Map<String, Object> analysisCache = new ConcurrentHashMap<>();
    
    static {
        // Initialize issue trackers
        for (PerformanceIssue issue : PerformanceIssue.values()) {
            issueTrackers.put(issue, new IssueTracker(issue));
        }
    }
    
    /**
     * Analyzes a single performance frame for bottlenecks
     * Called asynchronously to avoid blocking the main thread
     * 
     * @param frame The performance frame to analyze
     * @return Set of detected issues with severity levels
     */
    public static Set<PerformanceIssue> analyzeFrame(PerformanceFrame frame) {
        if (frame == null || !ProfilerConfig.isBottleneckAnalysisEnabled()) {
            return Collections.emptySet();
        }
        
        long startTime = System.nanoTime();
        Set<PerformanceIssue> detectedIssues = new HashSet<>();
        
        try {
            totalAnalysisCount.incrementAndGet();
            lastAnalysisTime = System.currentTimeMillis();
            
            // Update analysis context based on game state
            updateAnalysisContext(frame);
            
            // Perform multi-level analysis
            detectedIssues.addAll(analyzeFpsPerformance(frame));
            detectedIssues.addAll(analyzeMemoryPerformance(frame));
            detectedIssues.addAll(analyzeRenderingPerformance(frame));
            detectedIssues.addAll(analyzeWorldPerformance(frame));
            detectedIssues.addAll(analyzeNetworkPerformance(frame));
            detectedIssues.addAll(analyzeSystemPerformance(frame));
            
            // Update issue trackers with results
            updateIssueTrackers(detectedIssues, frame);
            
            // Generate optimization suggestions for critical issues
            if (!detectedIssues.isEmpty()) {
                generateOptimizationSuggestions(detectedIssues, frame);
            }
            
            // Update performance statistics
            long analysisTime = System.nanoTime() - startTime;
            performanceStats.recordAnalysisTime(analysisTime);
            
            SmartProfilerMod.LOGGER.debug("Frame analysis completed: {} issues detected in {}μs", 
                detectedIssues.size(), analysisTime / 1000);
                
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Error during bottleneck analysis", e);
        }
        
        return detectedIssues;
    }
    
    /**
     * Analyzes FPS-related performance issues
     */
    private static Set<PerformanceIssue> analyzeFpsPerformance(PerformanceFrame frame) {
        Set<PerformanceIssue> issues = new HashSet<>();
        
        int currentFps = frame.getFps();
        
        // Direct FPS threshold analysis
        if (currentFps <= CRITICAL_FPS_THRESHOLD) {
            issues.add(PerformanceIssue.CRITICAL_FPS);
            SmartProfilerMod.LOGGER.warn("Critical FPS detected: {}", currentFps);
        } else if (currentFps <= LOW_FPS_THRESHOLD) {
            issues.add(PerformanceIssue.LOW_FPS);
        }
        
        // FPS stability analysis
        if (analyzeFpsStability(frame)) {
            issues.add(PerformanceIssue.FPS_INSTABILITY);
        }
        
        // Frame time spike detection
        if (analyzeFrameTimeSpikes(frame)) {
            issues.add(PerformanceIssue.FRAME_TIME_SPIKES);
        }
        
        return issues;
    }
    
    /**
     * Analyzes memory-related performance issues
     */
    private static Set<PerformanceIssue> analyzeMemoryPerformance(PerformanceFrame frame) {
        Set<PerformanceIssue> issues = new HashSet<>();
        
        MemoryStats memStats = frame.getMemoryStats();
        float memoryPercent = memStats.getHeapUsagePercent();
        
        // Memory usage threshold analysis
        if (memoryPercent >= CRITICAL_MEMORY_THRESHOLD) {
            issues.add(PerformanceIssue.CRITICAL_MEMORY_USAGE);
            SmartProfilerMod.LOGGER.warn("Critical memory usage: {:.1f}%", memoryPercent);
        } else if (memoryPercent >= HIGH_MEMORY_THRESHOLD) {
            issues.add(PerformanceIssue.HIGH_MEMORY_USAGE);
        }
        
        // Memory leak detection
        if (detectMemoryLeak(frame)) {
            issues.add(PerformanceIssue.MEMORY_LEAK);
            SmartProfilerMod.LOGGER.warn("Potential memory leak detected");
        }
        
        // GC pressure analysis
        if (analyzeGarbageCollection(frame)) {
            issues.add(PerformanceIssue.GC_PRESSURE);
        }
        
        // Off-heap memory analysis
        if (analyzeOffHeapMemory(frame)) {
            issues.add(PerformanceIssue.OFF_HEAP_MEMORY_ISSUE);
        }
        
        return issues;
    }
    
    /**
     * Analyzes rendering-related performance issues
     */
    private static Set<PerformanceIssue> analyzeRenderingPerformance(PerformanceFrame frame) {
        Set<PerformanceIssue> issues = new HashSet<>();
        
        RenderStats renderStats = frame.getRenderStats();
        
        // Render time analysis
        if (renderStats.getRenderTimeMs() > getRenderTimeThreshold()) {
            issues.add(PerformanceIssue.RENDER_LAG);
        }
        
        // GPU bottleneck detection
        if (detectGpuBottleneck(frame)) {
            issues.add(PerformanceIssue.GPU_BOTTLENECK);
        }
        
        // Shader performance analysis
        if (analyzeShaderPerformance(frame)) {
            issues.add(PerformanceIssue.SHADER_PERFORMANCE);
        }
        
        // Draw call analysis
        if (renderStats.getDrawCalls() > getDrawCallThreshold()) {
            issues.add(PerformanceIssue.EXCESSIVE_DRAW_CALLS);
        }
        
        return issues;
    }
    
    /**
     * Analyzes world-related performance issues
     */
    private static Set<PerformanceIssue> analyzeWorldPerformance(PerformanceFrame frame) {
        Set<PerformanceIssue> issues = new HashSet<>();
        
        ChunkStats chunkStats = frame.getChunkStats();
        EntityStats entityStats = frame.getEntityStats();
        
        // Chunk loading analysis
        if (chunkStats.getLoadedChunks() > EXCESSIVE_CHUNKS_THRESHOLD) {
            issues.add(PerformanceIssue.EXCESSIVE_CHUNKS);
        }
        
        // Chunk update performance
        if (analyzeChunkUpdates(frame)) {
            issues.add(PerformanceIssue.CHUNK_UPDATE_LAG);
        }
        
        // Entity analysis
        if (entityStats.getTotalEntities() > EXCESSIVE_ENTITIES_THRESHOLD) {
            issues.add(PerformanceIssue.EXCESSIVE_ENTITIES);
        }
        
        // Entity processing lag
        if (analyzeEntityProcessing(frame)) {
            issues.add(PerformanceIssue.ENTITY_LAG);
        }
        
        // Tile entity analysis
        if (analyzeTileEntityPerformance(frame)) {
            issues.add(PerformanceIssue.TILE_ENTITY_LAG);
        }
        
        return issues;
    }
    
    /**
     * Analyzes network-related performance issues
     */
    private static Set<PerformanceIssue> analyzeNetworkPerformance(PerformanceFrame frame) {
        Set<PerformanceIssue> issues = new HashSet<>();
        
        NetworkStats networkStats = frame.getNetworkStats();
        
        // Network latency analysis
        if (networkStats.getLatencyMs() > getNetworkLatencyThreshold()) {
            issues.add(PerformanceIssue.NETWORK_LAG);
        }
        
        // Packet loss analysis
        if (networkStats.getPacketLoss() > getPacketLossThreshold()) {
            issues.add(PerformanceIssue.PACKET_LOSS);
        }
        
        // Bandwidth usage analysis
        if (analyzeNetworkBandwidth(frame)) {
            issues.add(PerformanceIssue.NETWORK_BANDWIDTH);
        }
        
        return issues;
    }
    
    /**
     * Analyzes system-level performance issues
     */
    private static Set<PerformanceIssue> analyzeSystemPerformance(PerformanceFrame frame) {
        Set<PerformanceIssue> issues = new HashSet<>();
        
        // CPU usage analysis
        if (analyzeSystemCpuUsage(frame)) {
            issues.add(PerformanceIssue.HIGH_CPU_USAGE);
        }
        
        // Disk I/O analysis
        if (analyzeSystemDiskIO(frame)) {
            issues.add(PerformanceIssue.DISK_IO_BOTTLENECK);
        }
        
        // Thread contention analysis
        if (analyzeThreadContention(frame)) {
            issues.add(PerformanceIssue.THREAD_CONTENTION);
        }
        
        return issues;
    }
    
    /**
     * Advanced FPS stability analysis using statistical methods
     */
    private static boolean analyzeFpsStability(PerformanceFrame frame) {
        IssueTracker tracker = issueTrackers.get(PerformanceIssue.FPS_INSTABILITY);
        tracker.addDataPoint(frame.getFps());
        
        if (tracker.getDataPointCount() < SHORT_TERM_WINDOW) {
            return false; // Not enough data
        }
        
        // Calculate coefficient of variation (CV) for FPS stability
        double mean = tracker.getMeanValue();
        double standardDeviation = tracker.getStandardDeviation();
        double coefficientOfVariation = standardDeviation / mean;
        
        // FPS is considered unstable if CV > 0.3 (30% variation)
        boolean isUnstable = coefficientOfVariation > 0.3;
        
        if (isUnstable) {
            SmartProfilerMod.LOGGER.debug("FPS instability detected: CV = {:.3f}", coefficientOfVariation);
        }
        
        return isUnstable;
    }
    
    /**
     * Detects frame time spikes that cause stuttering
     */
    private static boolean analyzeFrameTimeSpikes(PerformanceFrame frame) {
        double frameTimeMs = 1000.0 / Math.max(1, frame.getFps());
        
        IssueTracker tracker = issueTrackers.get(PerformanceIssue.FRAME_TIME_SPIKES);
        tracker.addDataPoint((float)frameTimeMs);
        
        if (tracker.getDataPointCount() < SHORT_TERM_WINDOW) {
            return false;
        }
        
        // Detect spikes: frame time > 2x the recent average
        double recentAverage = tracker.getRecentAverage(SHORT_TERM_WINDOW / 4);
        boolean hasSpike = frameTimeMs > recentAverage * 2.0;
        
        if (hasSpike) {
            SmartProfilerMod.LOGGER.debug("Frame time spike detected: {:.1f}ms (avg: {:.1f}ms)", 
                frameTimeMs, recentAverage);
        }
        
        return hasSpike;
    }
    
    /**
     * Advanced memory leak detection using trend analysis
     */
    private static boolean detectMemoryLeak(PerformanceFrame frame) {
        IssueTracker tracker = issueTrackers.get(PerformanceIssue.MEMORY_LEAK);
        tracker.addDataPoint(frame.getMemoryStats().getHeapUsagePercent());
        
        if (tracker.getDataPointCount() < LONG_TERM_WINDOW) {
            return false; // Need long-term data for leak detection
        }
        
        // Calculate memory usage trend over time
        double trend = tracker.calculateTrend(LONG_TERM_WINDOW);
        
        // Memory leak suspected if consistent upward trend > 0.1% per frame
        boolean potentialLeak = trend > 0.1;
        
        if (potentialLeak) {
            SmartProfilerMod.LOGGER.warn("Potential memory leak: trend = {:.3f}% per frame", trend);
        }
        
        return potentialLeak;
    }
    
    /**
     * Analyzes garbage collection performance impact
     */
    private static boolean analyzeGarbageCollection(PerformanceFrame frame) {
        MemoryStats memStats = frame.getMemoryStats();
        
        // GC pressure indicators
        boolean highGcTime = memStats.getGcTime() > 100; // >100ms GC time per measurement
        boolean frequentGc = memStats.getGcCollections() > 5; // >5 collections per measurement
        
        return highGcTime || frequentGc;
    }
    
    /**
     * Analyzes off-heap memory usage (DirectByteBuffers, etc.)
     */
    private static boolean analyzeOffHeapMemory(PerformanceFrame frame) {
        MemoryStats memStats = frame.getMemoryStats();
        
        // Check if non-heap memory is growing excessively
        long nonHeapMB = memStats.getNonHeapUsed() / (1024 * 1024);
        
        // Alert if non-heap memory > 512MB (typical for normal operation)
        return nonHeapMB > 512;
    }
    
    /**
     * Detects GPU bottlenecks by analyzing render performance patterns
     */
    private static boolean detectGpuBottleneck(PerformanceFrame frame) {
        // GPU bottleneck indicators:
        // 1. High GPU usage with low CPU usage
        // 2. Frame rate limited by vsync or GPU capability
        // 3. Render time significantly higher than tick time
        
        RenderStats renderStats = frame.getRenderStats();
        
        // Simple heuristic: render time > 80% of frame time
        double frameTimeMs = 1000.0 / Math.max(1, frame.getFps());
        double renderRatio = renderStats.getRenderTimeMs() / frameTimeMs;
        
        return renderRatio > 0.8;
    }
    
    /**
     * Analyzes shader performance impact
     */
    private static boolean analyzeShaderPerformance(PerformanceFrame frame) {
        RenderStats renderStats = frame.getRenderStats();
        
        // Shader performance issues:
        // 1. High fragment shader complexity
        // 2. Excessive texture sampling
        // 3. Overdraw issues
        
        return renderStats.getShaderCompileTime() > 50 || // >50ms shader compile time
               renderStats.getOverdrawFactor() > 3.0;      // >3x overdraw
    }
    
    /**
     * Analyzes chunk update performance
     */
    private static boolean analyzeChunkUpdates(PerformanceFrame frame) {
        ChunkStats chunkStats = frame.getChunkStats();
        
        // Too many chunk updates per frame can cause stuttering
        return chunkStats.getChunkUpdates() > 20; // >20 updates per frame
    }
    
    /**
     * Analyzes entity processing performance
     */
    private static boolean analyzeEntityProcessing(PerformanceFrame frame) {
        EntityStats entityStats = frame.getEntityStats();
        
        // Entity processing issues
        return entityStats.getEntityTickTime() > 10 || // >10ms entity ticking
               entityStats.getEntityCount() / Math.max(1, entityStats.getRenderedEntities()) > 5; // Too many invisible entities
    }
    
    /**
     * Analyzes tile entity (block entity) performance
     */
    private static boolean analyzeTileEntityPerformance(PerformanceFrame frame) {
        EntityStats entityStats = frame.getEntityStats();
        
        // Tile entity performance issues
        return entityStats.getTileEntityCount() > 1000 || // >1000 tile entities
               entityStats.getTileEntityTickTime() > 5;    // >5ms tile entity ticking
    }
    
    /**
     * Analyzes system CPU usage impact
     */
    private static boolean analyzeSystemCpuUsage(PerformanceFrame frame) {
        // System-level CPU analysis would require JMX or native calls
        // For now, use indirect indicators
        
        // High CPU usage indicators:
        // 1. Low FPS with normal GPU performance
        // 2. High tick time
        // 3. Thread contention
        
        return frame.getWorldStats().getTickTimeMs() > 40; // >40ms tick time (should be ~50ms)
    }
    
    /**
     * Analyzes system disk I/O performance
     */
    private static boolean analyzeSystemDiskIO(PerformanceFrame frame) {
        ChunkStats chunkStats = frame.getChunkStats();
        
        // Disk I/O bottleneck indicators:
        // 1. Slow chunk loading/saving
        // 2. High chunk generation time
        
        return chunkStats.getChunkLoadTime() > 100; // >100ms chunk load time
    }
    
    /**
     * Analyzes thread contention issues
     */
    private static boolean analyzeThreadContention(PerformanceFrame frame) {
        // Thread contention indicators:
        // 1. High tick time variance
        // 2. Synchronization bottlenecks
        
        WorldStats worldStats = frame.getWorldStats();
        return worldStats.getTickTimeMs() > worldStats.getAverageTickTime() * 2;
    }
    
    /**
     * Analyzes network bandwidth usage
     */
    private static boolean analyzeNetworkBandwidth(PerformanceFrame frame) {
        NetworkStats networkStats = frame.getNetworkStats();
        
        // Bandwidth issues
        return networkStats.getBytesPerSecond() > getNetworkBandwidthThreshold();
    }
    
    /**
     * Updates the analysis context based on current game state
     */
    private static void updateAnalysisContext(PerformanceFrame frame) {
        // Determine context based on game state
        if (frame.getFrameType() == FrameType.CLIENT) {
            if (frame.getChunkStats().getLoadedChunks() > 500) {
                currentContext = AnalysisContext.HEAVY_WORLD;
            } else if (frame.getEntityStats().getTotalEntities() > 200) {
                currentContext = AnalysisContext.ENTITY_HEAVY;
            } else {
                currentContext = AnalysisContext.NORMAL_GAMEPLAY;
            }
        } else {
            currentContext = AnalysisContext.SERVER_PROCESSING;
        }
    }
    
    /**
     * Updates issue trackers with analysis results
     */
    private static void updateIssueTrackers(Set<PerformanceIssue> detectedIssues, PerformanceFrame frame) {
        for (PerformanceIssue issue : PerformanceIssue.values()) {
            IssueTracker tracker = issueTrackers.get(issue);
            
            if (detectedIssues.contains(issue)) {
                tracker.recordOccurrence(frame.getTimestamp());
            } else {
                tracker.recordResolution(frame.getTimestamp());
            }
        }
    }
    
    /**
     * Generates optimization suggestions for detected issues
     */
    private static void generateOptimizationSuggestions(Set<PerformanceIssue> issues, PerformanceFrame frame) {
        for (PerformanceIssue issue : issues) {
            if (issue.getSeverity().ordinal() >= IssueSeverity.HIGH.ordinal()) {
                OptimizationSuggestion suggestion = OptimizationEngine.generateSuggestion(issue, frame, currentContext);
                if (suggestion != null) {
                    OptimizationEngine.queueSuggestion(suggestion);
                }
            }
        }
    }
    
    // Threshold getters - configurable through ProfilerConfig
    
    private static double getRenderTimeThreshold() {
        return ProfilerConfig.getRenderTimeThreshold();
    }
    
    private static int getDrawCallThreshold() {
        return ProfilerConfig.getDrawCallThreshold();
    }
    
    private static double getNetworkLatencyThreshold() {
        return ProfilerConfig.getNetworkLatencyThreshold();
    }
    
    private static double getPacketLossThreshold() {
        return ProfilerConfig.getPacketLossThreshold();
    }
    
    private static long getNetworkBandwidthThreshold() {
        return ProfilerConfig.getNetworkBandwidthThreshold();
    }
    
    /**
     * Gets comprehensive analysis statistics
     */
    public static AnalysisStatistics getAnalysisStatistics() {
        return new AnalysisStatistics(
            totalAnalysisCount.get(),
            lastAnalysisTime,
            performanceStats.getAverageAnalysisTime(),
            issueTrackers.values().stream()
                .collect(Collectors.toMap(
                    tracker -> tracker.getIssue(),
                    IssueTracker::getOccurrenceCount
                ))
        );
    }
    
    /**
     * Gets issue tracker for a specific performance issue
     */
    public static IssueTracker getIssueTracker(PerformanceIssue issue) {
        return issueTrackers.get(issue);
    }
    
    /**
     * Resets all analysis data - useful for testing or after configuration changes
     */
    public static void resetAnalysisData() {
        issueTrackers.values().forEach(IssueTracker::reset);
        performanceStats.reset();
        totalAnalysisCount.set(0);
        analysisCache.clear();
        SmartProfilerMod.LOGGER.info("Bottleneck analysis data reset");
    }
    
    /**
     * Cleanup method for mod shutdown
     */
    public static void shutdown() {
        resetAnalysisData();
        SmartProfilerMod.LOGGER.info("Bottleneck analyzer shutdown complete");
    }
}