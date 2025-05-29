package dev.hiba550.smartprofiler.data.optimization;

import java.util.Objects;

/**
 * Represents the hardware profile of the current system
 * Used for generating hardware-specific optimization suggestions
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class HardwareProfile {
    
    private final int cpuCores;
    private final String cpuName;
    private final int totalMemoryGB;
    private final GpuType gpuType;
    private final String gpuName;
    private final String osName;
    private final String osArch;
    private final String javaVersion;
    
    // Performance tier based on hardware capabilities
    private final PerformanceTier performanceTier;
    
    public HardwareProfile(int cpuCores, String cpuName, int totalMemoryGB, 
                          GpuType gpuType, String gpuName, String osName, 
                          String osArch, String javaVersion) {
        this.cpuCores = cpuCores;
        this.cpuName = cpuName;
        this.totalMemoryGB = totalMemoryGB;
        this.gpuType = gpuType;
        this.gpuName = gpuName;
        this.osName = osName;
        this.osArch = osArch;
        this.javaVersion = javaVersion;
        
        // Calculate performance tier based on specifications
        this.performanceTier = calculatePerformanceTier();
    }
    
    /**
     * Calculates performance tier based on hardware specifications
     */
    private PerformanceTier calculatePerformanceTier() {
        int score = 0;
        
        // CPU score (0-30 points)
        if (cpuCores >= 8) score += 30;
        else if (cpuCores >= 6) score += 25;
        else if (cpuCores >= 4) score += 20;
        else if (cpuCores >= 2) score += 10;
        
        // Memory score (0-25 points)
        if (totalMemoryGB >= 32) score += 25;
        else if (totalMemoryGB >= 16) score += 20;
        else if (totalMemoryGB >= 8) score += 15;
        else if (totalMemoryGB >= 4) score += 10;
        else if (totalMemoryGB >= 2) score += 5;
        
        // GPU score (0-25 points)
        switch (gpuType) {
            case NVIDIA -> {
                if (isHighEndNvidia()) score += 25;
                else if (isMidRangeNvidia()) score += 20;
                else score += 15;
            }
            case AMD -> {
                if (isHighEndAMD()) score += 25;
                else if (isMidRangeAMD()) score += 20;
                else score += 15;
            }
            case INTEGRATED -> score += 5;
            case UNKNOWN -> score += 10; // Assume mid-range
        }
        
        // OS/Architecture score (0-20 points)
        if (osArch.contains("64")) score += 20;
        else score += 10;
        
        // Determine tier based on total score
        if (score >= 75) return PerformanceTier.HIGH_END;
        else if (score >= 50) return PerformanceTier.MID_RANGE;
        else if (score >= 25) return PerformanceTier.LOW_END;
        else return PerformanceTier.POTATO;
    }
    
    private boolean isHighEndNvidia() {
        String gpu = gpuName.toLowerCase();
        return gpu.contains("rtx 40") || gpu.contains("rtx 30") || 
               gpu.contains("gtx 1080") || gpu.contains("gtx 1070") ||
               gpu.contains("quadro") || gpu.contains("titan");
    }
    
    private boolean isMidRangeNvidia() {
        String gpu = gpuName.toLowerCase();
        return gpu.contains("gtx 1060") || gpu.contains("gtx 1050") ||
               gpu.contains("rtx 20") || gpu.contains("gtx 16");
    }
    
    private boolean isHighEndAMD() {
        String gpu = gpuName.toLowerCase();
        return gpu.contains("rx 6") || gpu.contains("rx 7") ||
               gpu.contains("rx 580") || gpu.contains("rx 570") ||
               gpu.contains("vega");
    }
    
    private boolean isMidRangeAMD() {
        String gpu = gpuName.toLowerCase();
        return gpu.contains("rx 5") || gpu.contains("rx 4") ||
               gpu.contains("rx 560") || gpu.contains("rx 550");
    }
    
    /**
     * Checks if this system can handle high-end graphics settings
     */
    public boolean canHandleHighGraphics() {
        return performanceTier == PerformanceTier.HIGH_END || 
               (performanceTier == PerformanceTier.MID_RANGE && totalMemoryGB >= 16);
    }
    
    /**
     * Checks if this system should use conservative memory settings
     */
    public boolean shouldUseConservativeMemory() {
        return totalMemoryGB <= 8 || performanceTier == PerformanceTier.LOW_END;
    }
    
    /**
     * Gets recommended render distance based on hardware
     */
    public int getRecommendedRenderDistance() {
        return switch (performanceTier) {
            case HIGH_END -> 16;
            case MID_RANGE -> 12;
            case LOW_END -> 8;
            case POTATO -> 6;
        };
    }
    
    /**
     * Gets recommended memory allocation in GB
     */
    public int getRecommendedMemoryAllocationGB() {
        return switch (performanceTier) {
            case HIGH_END -> Math.min(8, totalMemoryGB / 2);
            case MID_RANGE -> Math.min(6, totalMemoryGB / 2);
            case LOW_END -> Math.min(4, totalMemoryGB / 2);
            case POTATO -> Math.min(2, totalMemoryGB / 2);
        };
    }
    
    /**
     * Checks if system supports hardware acceleration
     */
    public boolean supportsHardwareAcceleration() {
        return gpuType != GpuType.NONE && !osArch.equals("x86");
    }
    
    /**
     * Gets default hardware profile for fallback
     */
    public static HardwareProfile getDefaultProfile() {
        return new HardwareProfile(
            4, "Unknown CPU", 8, GpuType.UNKNOWN, "Unknown GPU",
            System.getProperty("os.name"), System.getProperty("os.arch"),
            System.getProperty("java.version")
        );
    }
    
    // Getters
    public int getCpuCores() { return cpuCores; }
    public String getCpuName() { return cpuName; }
    public int getTotalMemoryGB() { return totalMemoryGB; }
    public GpuType getGpuType() { return gpuType; }
    public String getGpuName() { return gpuName; }
    public String getOsName() { return osName; }
    public String getOsArch() { return osArch; }
    public String getJavaVersion() { return javaVersion; }
    public PerformanceTier getPerformanceTier() { return performanceTier; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HardwareProfile that = (HardwareProfile) obj;
        return cpuCores == that.cpuCores &&
               totalMemoryGB == that.totalMemoryGB &&
               gpuType == that.gpuType &&
               Objects.equals(osName, that.osName) &&
               Objects.equals(osArch, that.osArch);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cpuCores, totalMemoryGB, gpuType, osName, osArch);
    }
    
    @Override
    public String toString() {
        return String.format("HardwareProfile{cores=%d, memory=%dGB, gpu=%s, tier=%s}", 
            cpuCores, totalMemoryGB, gpuType, performanceTier);
    }
}