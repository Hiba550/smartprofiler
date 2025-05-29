package dev.hiba550.smartprofiler.data.models;

/**
 * Comprehensive enumeration of performance issues that can be detected
 * 
 * Each issue has an associated severity level and category for proper
 * prioritization and handling in the analysis system.
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum PerformanceIssue {
    
    // FPS and Frame Rate Issues
    LOW_FPS(IssueSeverity.MEDIUM, IssueCategory.RENDERING, 
        "Low frame rate detected", "FPS below acceptable threshold"),
    
    CRITICAL_FPS(IssueSeverity.CRITICAL, IssueCategory.RENDERING,
        "Critical frame rate", "FPS critically low, game barely playable"),
    
    FPS_INSTABILITY(IssueSeverity.MEDIUM, IssueCategory.RENDERING,
        "FPS instability", "Frame rate varies significantly causing stuttering"),
    
    FRAME_TIME_SPIKES(IssueSeverity.HIGH, IssueCategory.RENDERING,
        "Frame time spikes", "Sudden frame time increases causing stutters"),
    
    // Memory Issues
    HIGH_MEMORY_USAGE(IssueSeverity.MEDIUM, IssueCategory.MEMORY,
        "High memory usage", "Memory usage above recommended threshold"),
    
    CRITICAL_MEMORY_USAGE(IssueSeverity.CRITICAL, IssueCategory.MEMORY,
        "Critical memory usage", "Memory usage critically high, risk of OOM"),
    
    MEMORY_LEAK(IssueSeverity.HIGH, IssueCategory.MEMORY,
        "Potential memory leak", "Memory usage increasing consistently over time"),
    
    GC_PRESSURE(IssueSeverity.MEDIUM, IssueCategory.MEMORY,
        "Garbage collection pressure", "Frequent or long garbage collection cycles"),
    
    OFF_HEAP_MEMORY_ISSUE(IssueSeverity.MEDIUM, IssueCategory.MEMORY,
        "Off-heap memory issue", "Non-heap memory usage abnormally high"),
    
    // Rendering Issues
    RENDER_LAG(IssueSeverity.MEDIUM, IssueCategory.RENDERING,
        "Rendering lag", "Render pipeline taking too long per frame"),
    
    GPU_BOTTLENECK(IssueSeverity.HIGH, IssueCategory.RENDERING,
        "GPU bottleneck", "Graphics processing unit limiting performance"),
    
    SHADER_PERFORMANCE(IssueSeverity.MEDIUM, IssueCategory.RENDERING,
        "Shader performance issue", "Shader compilation or execution problems"),
    
    EXCESSIVE_DRAW_CALLS(IssueSeverity.MEDIUM, IssueCategory.RENDERING,
        "Excessive draw calls", "Too many render calls per frame"),
    
    // World and Game Logic Issues
    EXCESSIVE_CHUNKS(IssueSeverity.MEDIUM, IssueCategory.WORLD,
        "Too many chunks loaded", "Chunk count exceeding optimal threshold"),
    
    CHUNK_UPDATE_LAG(IssueSeverity.MEDIUM, IssueCategory.WORLD,
        "Chunk update lag", "Chunk updates taking too long to process"),
    
    EXCESSIVE_ENTITIES(IssueSeverity.MEDIUM, IssueCategory.WORLD,
        "Too many entities", "Entity count affecting performance"),
    
    ENTITY_LAG(IssueSeverity.MEDIUM, IssueCategory.WORLD,
        "Entity processing lag", "Entity updates taking excessive time"),
    
    TILE_ENTITY_LAG(IssueSeverity.MEDIUM, IssueCategory.WORLD,
        "Tile entity lag", "Block entity processing causing performance issues"),
    
    // Network Issues
    NETWORK_LAG(IssueSeverity.LOW, IssueCategory.NETWORK,
        "Network lag", "High network latency affecting performance"),
    
    PACKET_LOSS(IssueSeverity.MEDIUM, IssueCategory.NETWORK,
        "Packet loss", "Network packets being lost"),
    
    NETWORK_BANDWIDTH(IssueSeverity.LOW, IssueCategory.NETWORK,
        "Network bandwidth issue", "Network bandwidth usage too high"),
    
    // System Issues
    HIGH_CPU_USAGE(IssueSeverity.MEDIUM, IssueCategory.SYSTEM,
        "High CPU usage", "CPU usage affecting game performance"),
    
    DISK_IO_BOTTLENECK(IssueSeverity.MEDIUM, IssueCategory.SYSTEM,
        "Disk I/O bottleneck", "Disk operations causing performance issues"),
    
    THREAD_CONTENTION(IssueSeverity.HIGH, IssueCategory.SYSTEM,
        "Thread contention", "Thread synchronization causing delays");
    
    private final IssueSeverity severity;
    private final IssueCategory category;
    private final String displayName;
    private final String description;
    
    PerformanceIssue(IssueSeverity severity, IssueCategory category, 
                    String displayName, String description) {
        this.severity = severity;
        this.category = category;
        this.displayName = displayName;
        this.description = description;
    }
    
    public IssueSeverity getSeverity() {
        return severity;
    }
    
    public IssueCategory getCategory() {
        return category;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the priority score for this issue (higher = more urgent)
     */
    public int getPriorityScore() {
        return severity.getWeight() * 10 + category.getWeight();
    }
    
    /**
     * Checks if this issue is critical and requires immediate attention
     */
    public boolean isCritical() {
        return severity == IssueSeverity.CRITICAL;
    }
    
    /**
     * Gets all issues in a specific category
     */
    public static PerformanceIssue[] getIssuesInCategory(IssueCategory category) {
        return java.util.Arrays.stream(values())
            .filter(issue -> issue.getCategory() == category)
            .toArray(PerformanceIssue[]::new);
    }
    
    /**
     * Gets all issues with minimum severity
     */
    public static PerformanceIssue[] getIssuesWithMinSeverity(IssueSeverity minSeverity) {
        return java.util.Arrays.stream(values())
            .filter(issue -> issue.getSeverity().ordinal() >= minSeverity.ordinal())
            .toArray(PerformanceIssue[]::new);
    }
}