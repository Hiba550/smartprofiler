package dev.hiba550.smartprofiler;

import dev.hiba550.smartprofiler.config.ProfilerConfig;
import dev.hiba550.smartprofiler.data.PerformanceCollector;
import dev.hiba550.smartprofiler.network.ProfilerNetworking;
import dev.hiba550.smartprofiler.storage.PerformanceDatabase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smart Performance Profiler - Main mod class for server-side initialization
 * 
 * This mod provides real-time performance analysis for Minecraft 1.21.5 with:
 * - Automated bottleneck detection
 * - Performance optimization suggestions
 * - Historical performance tracking
 * - Cross-dimensional analysis
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since 1.21.5
 */
public class SmartProfilerMod implements ModInitializer {
    public static final String MOD_ID = "smart_profiler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Network packet identifiers
    public static final Identifier PERFORMANCE_DATA_PACKET = new Identifier(MOD_ID, "performance_data");
    public static final Identifier CONFIG_SYNC_PACKET = new Identifier(MOD_ID, "config_sync");
    public static final Identifier OPTIMIZATION_SUGGESTION_PACKET = new Identifier(MOD_ID, "optimization_suggestion");
    
    private static SmartProfilerMod instance;
    private PerformanceDatabase database;
    private boolean initialized = false;
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Smart Performance Profiler for Minecraft 1.21.5");
        
        try {
            instance = this;
            
            // Initialize configuration system
            ProfilerConfig.initialize();
            LOGGER.debug("Configuration system initialized");
            
            // Initialize database for performance history
            initializeDatabase();
            
            // Register networking packets
            ProfilerNetworking.registerServerPackets();
            LOGGER.debug("Server networking registered");
            
            // Register server-side event listeners
            registerServerEvents();
            
            // Initialize server-side performance collection
            PerformanceCollector.initializeServer();
            
            initialized = true;
            LOGGER.info("Smart Performance Profiler successfully initialized!");
            
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Smart Performance Profiler", e);
            throw new RuntimeException("Mod initialization failed", e);
        }
    }
    
    /**
     * Initializes the performance database for storing historical data
     * Uses H2 embedded database for cross-platform compatibility
     */
    private void initializeDatabase() {
        try {
            database = new PerformanceDatabase();
            database.initialize();
            LOGGER.debug("Performance database initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize performance database", e);
            // Continue without database - mod will work with in-memory data only
            database = null;
        }
    }
    
    /**
     * Registers server-side event listeners for performance monitoring
     */
    private void registerServerEvents() {
        // Server startup event
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("Server started - beginning performance monitoring");
            PerformanceCollector.onServerStarted(server);
        });
        
        // Server shutdown event
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Server stopping - saving performance data");
            if (database != null) {
                database.savePerformanceData();
            }
        });
        
        // Server tick event for performance collection
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (ProfilerConfig.isServerMonitoringEnabled()) {
                PerformanceCollector.collectServerData(server);
            }
        });
    }
    
    /**
     * Gets the mod instance
     * @return The SmartProfilerMod instance, or null if not initialized
     */
    public static SmartProfilerMod getInstance() {
        return instance;
    }
    
    /**
     * Gets the performance database instance
     * @return The PerformanceDatabase instance, or null if not available
     */
    public PerformanceDatabase getDatabase() {
        return database;
    }
    
    /**
     * Checks if the mod is fully initialized
     * @return true if initialization completed successfully
     */
    public boolean isInitialized() {
        return initialized;
    }
}