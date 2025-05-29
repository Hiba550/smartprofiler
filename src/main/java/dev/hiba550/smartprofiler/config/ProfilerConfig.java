package dev.hiba550.smartprofiler.config;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.client.overlay.OverlayMode;
import dev.hiba550.smartprofiler.client.overlay.OverlayPosition;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

/**
 * Main configuration class for Smart Performance Profiler
 * 
 * Uses Cloth Config API for automatic GUI generation and serialization.
 * All settings are automatically saved and loaded with proper validation.
 * 
 * Features:
 * - Real-time configuration updates
 * - Input validation and bounds checking
 * - Category-based organization
 * - Client/Server setting separation
 * - Performance impact indicators
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
@Config(name = SmartProfilerMod.MOD_ID)
public class ProfilerConfig implements ConfigData {
    
    // Singleton instance
    private static ProfilerConfig instance;
    
    // === GENERAL SETTINGS ===
    
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable or disable the Smart Performance Profiler entirely")
    public boolean enabled = true;
    
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable real-time performance monitoring")
    public boolean realtimeMonitoringEnabled = true;
    
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable automatic bottleneck detection")
    public boolean bottleneckAnalysisEnabled = true;
    
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable performance history database")
    public boolean databaseEnabled = true;
    
    @ConfigEntry.Category("general")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    @ConfigEntry.Gui.Tooltip
    @Comment("Data collection frequency (higher = more frequent, more CPU usage)")
    public int collectionFrequency = 20;
    
    // === OVERLAY SETTINGS ===
    
    @ConfigEntry.Category("overlay")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable performance overlay")
    public boolean overlayEnabled = true;
    
    @ConfigEntry.Category("overlay")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip
    @Comment("Default overlay display mode")
    public OverlayMode defaultOverlayMode = OverlayMode.COMPACT;
    
    @ConfigEntry.Category("overlay")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip
    @Comment("Default overlay position on screen")
    public OverlayPosition defaultOverlayPosition = OverlayPosition.TOP_LEFT;
    
    @ConfigEntry.Category("overlay")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    @ConfigEntry.Gui.Tooltip
    @Comment("Overlay opacity percentage")
    public int overlayOpacity = 90;
    
    @ConfigEntry.Category("overlay")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
    @ConfigEntry.Gui.Tooltip
    @Comment("Overlay update rate (FPS)")
    public int overlayUpdateRate = 10;
    
    @ConfigEntry.Category("overlay")
    @ConfigEntry.Gui.Tooltip
    @Comment("Show performance graphs in overlay")
    public boolean showGraphs = true;
    
    @ConfigEntry.Category("overlay")
    @ConfigEntry.Gui.Tooltip
    @Comment("Show performance warnings in overlay")
    public boolean showWarnings = true;
    
    @ConfigEntry.Category("overlay")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable overlay animations and transitions")
    public boolean enableAnimations = true;
    
    // === PERFORMANCE THRESHOLDS ===
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 120)
    @ConfigEntry.Gui.Tooltip
    @Comment("FPS threshold for low performance warning")
    public int lowFpsThreshold = 30;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 60)
    @ConfigEntry.Gui.Tooltip
    @Comment("FPS threshold for critical performance warning")
    public int criticalFpsThreshold = 15;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 99)
    @ConfigEntry.Gui.Tooltip
    @Comment("Memory usage percentage for high memory warning")
    public int highMemoryThreshold = 80;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 80, max = 99)
    @ConfigEntry.Gui.Tooltip
    @Comment("Memory usage percentage for critical memory warning")
    public int criticalMemoryThreshold = 95;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 100, max = 5000)
    @ConfigEntry.Gui.Tooltip
    @Comment("Chunk count threshold for excessive chunks warning")
    public int excessiveChunksThreshold = 1000;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 100, max = 2000)
    @ConfigEntry.Gui.Tooltip
    @Comment("Entity count threshold for excessive entities warning")
    public int excessiveEntitiesThreshold = 500;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
    @ConfigEntry.Gui.Tooltip
    @Comment("Render time threshold in milliseconds")
    public int renderTimeThreshold = 50;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 100, max = 10000)
    @ConfigEntry.Gui.Tooltip
    @Comment("Draw call count threshold")
    public int drawCallThreshold = 2000;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 1000)
    @ConfigEntry.Gui.Tooltip
    @Comment("Network latency threshold in milliseconds")
    public int networkLatencyThreshold = 150;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
    @ConfigEntry.Gui.Tooltip
    @Comment("Packet loss threshold percentage")
    public int packetLossThreshold = 5;
    
    @ConfigEntry.Category("thresholds")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    @ConfigEntry.Gui.Tooltip
    @Comment("Network bandwidth threshold in MB/s")
    public int networkBandwidthThreshold = 10;
    
    // === ANALYSIS SETTINGS ===
    
    @ConfigEntry.Category("analysis")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable server-side performance monitoring")
    public boolean serverMonitoringEnabled = true;
    
    @ConfigEntry.Category("analysis")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable memory leak detection")
    public boolean memoryLeakDetectionEnabled = true;
    
    @ConfigEntry.Category("analysis")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable GPU bottleneck detection")
    public boolean gpuBottleneckDetectionEnabled = true;
    
    @ConfigEntry.Category("analysis")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable automatic optimization suggestions")
    public boolean optimizationSuggestionsEnabled = true;
    
    @ConfigEntry.Category("analysis")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 300)
    @ConfigEntry.Gui.Tooltip
    @Comment("Analysis window size in seconds")
    public int analysisWindowSize = 60;
    
    @ConfigEntry.Category("analysis")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    @ConfigEntry.Gui.Tooltip
    @Comment("Trend detection sensitivity (1=low, 10=high)")
    public int trendSensitivity = 5;
    
    // === NOTIFICATION SETTINGS ===
    
    @ConfigEntry.Category("notifications")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable chat notifications for performance issues")
    public boolean chatNotificationsEnabled = true;
    
    @ConfigEntry.Category("notifications")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable sound notifications for critical issues")
    public boolean soundNotificationsEnabled = true;
    
    @ConfigEntry.Category("notifications")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable toast notifications")
    public boolean toastNotificationsEnabled = true;
    
    @ConfigEntry.Category("notifications")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 300)
    @ConfigEntry.Gui.Tooltip
    @Comment("Notification cooldown in seconds (prevents spam)")
    public int notificationCooldown = 30;
    
    @ConfigEntry.Category("notifications")
    @ConfigEntry.Gui.Tooltip
    @Comment("Only show notifications for critical issues")
    public boolean onlyCriticalNotifications = false;
    
    // === STORAGE SETTINGS ===
    
    @ConfigEntry.Category("storage")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable performance data persistence")
    public boolean dataPersistenceEnabled = true;
    
    @ConfigEntry.Category("storage")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30)
    @ConfigEntry.Gui.Tooltip
    @Comment("Days to keep performance history")
    public int dataRetentionDays = 7;
    
    @ConfigEntry.Category("storage")
    @ConfigEntry.BoundedDiscrete(min = 1000, max = 100000)
    @ConfigEntry.Gui.Tooltip
    @Comment("Maximum database size in MB")
    public int maxDatabaseSizeMB = 50;
    
    @ConfigEntry.Category("storage")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable automatic database cleanup")
    public boolean autoCleanupEnabled = true;
    
    @ConfigEntry.Category("storage")
    @ConfigEntry.Gui.Tooltip
    @Comment("Export performance data in CSV format")
    public boolean csvExportEnabled = false;
    
    // === ADVANCED SETTINGS ===
    
    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable debug logging (impacts performance)")
    public boolean debugLoggingEnabled = false;
    
    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable experimental features (may be unstable)")
    public boolean experimentalFeaturesEnabled = false;
    
    @ConfigEntry.Category("advanced")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
    @ConfigEntry.Gui.Tooltip
    @Comment("Number of analysis threads")
    public int analysisThreadCount = 2;
    
    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable JVM Flight Recorder integration")
    public boolean jfrIntegrationEnabled = false;
    
    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    @Comment("Enable native memory monitoring (requires JDK 14+)")
    public boolean nativeMemoryTrackingEnabled = false;
    
    @ConfigEntry.Category("advanced")
    @ConfigEntry.BoundedDiscrete(min = 100, max = 10000)
    @ConfigEntry.Gui.Tooltip
    @Comment("Performance data buffer size")
    public int dataBufferSize = 1000;
    
    /**
     * Initializes the configuration system
     * Registers serializers and loads existing config
     */
    public static void initialize() {
        try {
            AutoConfig.register(ProfilerConfig.class, GsonConfigSerializer::new);
            instance = AutoConfig.getConfigHolder(ProfilerConfig.class).getConfig();
            
            SmartProfilerMod.LOGGER.info("Configuration system initialized successfully");
            
            // Validate configuration on load
            instance.validateAndFix();
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Failed to initialize configuration", e);
            // Fall back to default config
            instance = new ProfilerConfig();
        }
    }
    
    /**
     * Gets the current configuration instance
     */
    public static ProfilerConfig getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }
    
    /**
     * Saves the current configuration to disk
     */
    public static void save() {
        try {
            if (instance != null) {
                instance.validateAndFix();
                AutoConfig.getConfigHolder(ProfilerConfig.class).save();
                SmartProfilerMod.LOGGER.debug("Configuration saved successfully");
            }
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Failed to save configuration", e);
        }
    }
    
    /**
     * Reloads configuration from disk
     */
    public static void reload() {
        try {
            AutoConfig.getConfigHolder(ProfilerConfig.class).load();
            instance = AutoConfig.getConfigHolder(ProfilerConfig.class).getConfig();
            instance.validateAndFix();
            SmartProfilerMod.LOGGER.info("Configuration reloaded successfully");
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Failed to reload configuration", e);
        }
    }
    
    /**
     * Validates and fixes configuration values
     * Ensures all values are within acceptable ranges
     */
    public void validateAndFix() {
        // Ensure thresholds are logical
        if (criticalFpsThreshold >= lowFpsThreshold) {
            criticalFpsThreshold = Math.max(5, lowFpsThreshold - 5);
            SmartProfilerMod.LOGGER.warn("Fixed critical FPS threshold to be lower than low FPS threshold");
        }
        
        if (criticalMemoryThreshold <= highMemoryThreshold) {
            criticalMemoryThreshold = Math.min(99, highMemoryThreshold + 5);
            SmartProfilerMod.LOGGER.warn("Fixed critical memory threshold to be higher than high memory threshold");
        }
        
        // Ensure collection frequency is reasonable
        if (collectionFrequency > 60) {
            collectionFrequency = 60;
            SmartProfilerMod.LOGGER.warn("Collection frequency capped at 60 to prevent performance issues");
        }
        
        // Ensure analysis thread count doesn't exceed system capabilities
        int maxThreads = Runtime.getRuntime().availableProcessors();
        if (analysisThreadCount > maxThreads) {
            analysisThreadCount = Math.max(1, maxThreads / 2);
            SmartProfilerMod.LOGGER.warn("Analysis thread count adjusted to {}", analysisThreadCount);
        }
        
        // Validate data buffer size
        if (dataBufferSize < 100) {
            dataBufferSize = 100;
            SmartProfilerMod.LOGGER.warn("Data buffer size increased to minimum value");
        }
    }
    
    /**
     * Resets configuration to default values
     */
    public void resetToDefaults() {
        ProfilerConfig defaults = new ProfilerConfig();
        
        // Copy all values from defaults
        this.enabled = defaults.enabled;
        this.realtimeMonitoringEnabled = defaults.realtimeMonitoringEnabled;
        this.bottleneckAnalysisEnabled = defaults.bottleneckAnalysisEnabled;
        this.databaseEnabled = defaults.databaseEnabled;
        this.collectionFrequency = defaults.collectionFrequency;
        
        this.overlayEnabled = defaults.overlayEnabled;
        this.defaultOverlayMode = defaults.defaultOverlayMode;
        this.defaultOverlayPosition = defaults.defaultOverlayPosition;
        this.overlayOpacity = defaults.overlayOpacity;
        this.overlayUpdateRate = defaults.overlayUpdateRate;
        this.showGraphs = defaults.showGraphs;
        this.showWarnings = defaults.showWarnings;
        this.enableAnimations = defaults.enableAnimations;
        
        // Reset all other categories...
        this.lowFpsThreshold = defaults.lowFpsThreshold;
        this.criticalFpsThreshold = defaults.criticalFpsThreshold;
        this.highMemoryThreshold = defaults.highMemoryThreshold;
        this.criticalMemoryThreshold = defaults.criticalMemoryThreshold;
        this.excessiveChunksThreshold = defaults.excessiveChunksThreshold;
        this.excessiveEntitiesThreshold = defaults.excessiveEntitiesThreshold;
        this.renderTimeThreshold = defaults.renderTimeThreshold;
        this.drawCallThreshold = defaults.drawCallThreshold;
        this.networkLatencyThreshold = defaults.networkLatencyThreshold;
        this.packetLossThreshold = defaults.packetLossThreshold;
        this.networkBandwidthThreshold = defaults.networkBandwidthThreshold;
        
        this.serverMonitoringEnabled = defaults.serverMonitoringEnabled;
        this.memoryLeakDetectionEnabled = defaults.memoryLeakDetectionEnabled;
        this.gpuBottleneckDetectionEnabled = defaults.gpuBottleneckDetectionEnabled;
        this.optimizationSuggestionsEnabled = defaults.optimizationSuggestionsEnabled;
        this.analysisWindowSize = defaults.analysisWindowSize;
        this.trendSensitivity = defaults.trendSensitivity;
        
        this.chatNotificationsEnabled = defaults.chatNotificationsEnabled;
        this.soundNotificationsEnabled = defaults.soundNotificationsEnabled;
        this.toastNotificationsEnabled = defaults.toastNotificationsEnabled;
        this.notificationCooldown = defaults.notificationCooldown;
        this.onlyCriticalNotifications = defaults.onlyCriticalNotifications;
        
        this.dataPersistenceEnabled = defaults.dataPersistenceEnabled;
        this.dataRetentionDays = defaults.dataRetentionDays;
        this.maxDatabaseSizeMB = defaults.maxDatabaseSizeMB;
        this.autoCleanupEnabled = defaults.autoCleanupEnabled;
        this.csvExportEnabled = defaults.csvExportEnabled;
        
        this.debugLoggingEnabled = defaults.debugLoggingEnabled;
        this.experimentalFeaturesEnabled = defaults.experimentalFeaturesEnabled;
        this.analysisThreadCount = defaults.analysisThreadCount;
        this.jfrIntegrationEnabled = defaults.jfrIntegrationEnabled;
        this.nativeMemoryTrackingEnabled = defaults.nativeMemoryTrackingEnabled;
        this.dataBufferSize = defaults.dataBufferSize;
        
        SmartProfilerMod.LOGGER.info("Configuration reset to default values");
    }
    
    // === CONVENIENCE METHODS FOR OTHER COMPONENTS ===
    
    // General settings
    public static boolean isEnabled() {
        return getInstance().enabled;
    }
    
    public static boolean isRealtimeMonitoringEnabled() {
        return getInstance().realtimeMonitoringEnabled;
    }
    
    public static void setRealtimeMonitoringEnabled(boolean enabled) {
        getInstance().realtimeMonitoringEnabled = enabled;
        save();
    }
    
    public static boolean isBottleneckAnalysisEnabled() {
        return getInstance().bottleneckAnalysisEnabled;
    }
    
    public static boolean isDatabaseEnabled() {
        return getInstance().databaseEnabled;
    }
    
    public static int getCollectionFrequency() {
        return getInstance().collectionFrequency;
    }
    
    // Overlay settings
    public static boolean isOverlayEnabled() {
        return getInstance().overlayEnabled;
    }
    
    public static OverlayMode getDefaultOverlayMode() {
        return getInstance().defaultOverlayMode;
    }
    
    public static OverlayPosition getDefaultOverlayPosition() {
        return getInstance().defaultOverlayPosition;
    }
    
    public static float getOverlayOpacity() {
        return getInstance().overlayOpacity / 100.0f;
    }
    
    public static int getOverlayUpdateRate() {
        return getInstance().overlayUpdateRate;
    }
    
    public static boolean isShowGraphs() {
        return getInstance().showGraphs;
    }
    
    public static boolean isShowWarnings() {
        return getInstance().showWarnings;
    }
    
    public static boolean isEnableAnimations() {
        return getInstance().enableAnimations;
    }
    
    // Threshold settings
    public static int getLowFpsThreshold() {
        return getInstance().lowFpsThreshold;
    }
    
    public static int getCriticalFpsThreshold() {
        return getInstance().criticalFpsThreshold;
    }
    
    public static int getHighMemoryThreshold() {
        return getInstance().highMemoryThreshold;
    }
    
    public static int getCriticalMemoryThreshold() {
        return getInstance().criticalMemoryThreshold;
    }
    
    public static int getExcessiveChunksThreshold() {
        return getInstance().excessiveChunksThreshold;
    }
    
    public static int getExcessiveEntitiesThreshold() {
        return getInstance().excessiveEntitiesThreshold;
    }
    
    public static double getRenderTimeThreshold() {
        return getInstance().renderTimeThreshold;
    }
    
    public static int getDrawCallThreshold() {
        return getInstance().drawCallThreshold;
    }
    
    public static double getNetworkLatencyThreshold() {
        return getInstance().networkLatencyThreshold;
    }
    
    public static double getPacketLossThreshold() {
        return getInstance().packetLossThreshold;
    }
    
    public static long getNetworkBandwidthThreshold() {
        return getInstance().networkBandwidthThreshold * 1024 * 1024; // Convert MB to bytes
    }
    
    // Analysis settings
    public static boolean isServerMonitoringEnabled() {
        return getInstance().serverMonitoringEnabled;
    }
    
    public static boolean isMemoryLeakDetectionEnabled() {
        return getInstance().memoryLeakDetectionEnabled;
    }
    
    public static boolean isGpuBottleneckDetectionEnabled() {
        return getInstance().gpuBottleneckDetectionEnabled;
    }
    
    public static boolean isOptimizationSuggestionsEnabled() {
        return getInstance().optimizationSuggestionsEnabled;
    }
    
    public static int getAnalysisWindowSize() {
        return getInstance().analysisWindowSize;
    }
    
    public static int getTrendSensitivity() {
        return getInstance().trendSensitivity;
    }
    
    // Notification settings
    public static boolean isChatNotificationsEnabled() {
        return getInstance().chatNotificationsEnabled;
    }
    
    public static boolean isSoundNotificationsEnabled() {
        return getInstance().soundNotificationsEnabled;
    }
    
    public static boolean isToastNotificationsEnabled() {
        return getInstance().toastNotificationsEnabled;
    }
    
    public static int getNotificationCooldown() {
        return getInstance().notificationCooldown;
    }
    
    public static boolean isOnlyCriticalNotifications() {
        return getInstance().onlyCriticalNotifications;
    }
    
    // Storage settings
    public static boolean isDataPersistenceEnabled() {
        return getInstance().dataPersistenceEnabled;
    }
    
    public static int getDataRetentionDays() {
        return getInstance().dataRetentionDays;
    }
    
    public static int getMaxDatabaseSizeMB() {
        return getInstance().maxDatabaseSizeMB;
    }
    
    public static boolean isAutoCleanupEnabled() {
        return getInstance().autoCleanupEnabled;
    }
    
    public static boolean isCsvExportEnabled() {
        return getInstance().csvExportEnabled;
    }
    
    // Advanced settings
    public static boolean isDebugLoggingEnabled() {
        return getInstance().debugLoggingEnabled;
    }
    
    public static boolean isExperimentalFeaturesEnabled() {
        return getInstance().experimentalFeaturesEnabled;
    }
    
    public static int getAnalysisThreadCount() {
        return getInstance().analysisThreadCount;
    }
    
    public static boolean isJfrIntegrationEnabled() {
        return getInstance().jfrIntegrationEnabled;
    }
    
    public static boolean isNativeMemoryTrackingEnabled() {
        return getInstance().nativeMemoryTrackingEnabled;
    }
    
    public static int getDataBufferSize() {
        return getInstance().dataBufferSize;
    }
}