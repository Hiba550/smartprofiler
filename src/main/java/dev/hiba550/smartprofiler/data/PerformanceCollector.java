package dev.hiba550.smartprofiler.data;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.config.ProfilerConfig;
import dev.hiba550.smartprofiler.data.analysis.BottleneckAnalyzer;
import dev.hiba550.smartprofiler.data.models.*;
import dev.hiba550.smartprofiler.util.RingBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkManager;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects performance metrics from various game systems
 * 
 * Features:
 * - Thread-safe data collection
 * - Minimal performance impact
 * - Automatic bottleneck detection
 * - Historical data tracking
 * - Client and server monitoring
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since 1.21.5
 */
public class PerformanceCollector {
    
    // Buffer sizes for performance history
    private static final int CLIENT_BUFFER_SIZE = 1200; // 60 seconds at 20 FPS collection
    private static final int SERVER_BUFFER_SIZE = 600;  // 30 seconds at 20 TPS collection
    
    // Collection intervals
    private static final long CLIENT_COLLECTION_INTERVAL_MS = 50; // 20 FPS
    private static final long SERVER_COLLECTION_INTERVAL_MS = 50; // 20 TPS
    
    // Data storage
    private static final RingBuffer<PerformanceFrame> clientFrameBuffer = new RingBuffer<>(CLIENT_BUFFER_SIZE);
    private static final RingBuffer<PerformanceFrame> serverFrameBuffer = new RingBuffer<>(SERVER_BUFFER_SIZE);
    
    // Thread management
    private static ScheduledExecutorService analysisExecutor;
    private static final ConcurrentLinkedQueue<PerformanceFrame> pendingAnalysis = new ConcurrentLinkedQueue<>();
    
    // Timing control
    private static final AtomicLong lastClientCollection = new AtomicLong(0);
    private static final AtomicLong lastServerCollection = new AtomicLong(0);
    
    // System monitoring
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    // Initialization flags
    private static boolean clientInitialized = false;
    private static boolean serverInitialized = false;
    
    /**
     * Initializes client-side performance collection
     */
    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        if (clientInitialized) {
            return;
        }
        
        SmartProfilerMod.LOGGER.debug("Initializing client-side performance collector");
        
        // Initialize analysis thread pool
        if (analysisExecutor == null) {
            analysisExecutor = Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "SmartProfiler-Analysis");
                t.setDaemon(true);
                return t;
            });
            
            // Schedule periodic analysis
            analysisExecutor.scheduleWithFixedDelay(
                PerformanceCollector::processAnalysisQueue,
                5, 5, TimeUnit.SECONDS
            );
        }
        
        clientInitialized = true;
        SmartProfilerMod.LOGGER.info("Client-side performance collector initialized");
    }
    
    /**
     * Initializes server-side performance collection
     */
    public static void initializeServer() {
        if (serverInitialized) {
            return;
        }
        
        SmartProfilerMod.LOGGER.debug("Initializing server-side performance collector");
        
        // Server uses the same analysis executor as client
        if (analysisExecutor == null) {
            analysisExecutor = Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "SmartProfiler-ServerAnalysis");
                t.setDaemon(true);
                return t;
            });
        }
        
        serverInitialized = true;
        SmartProfilerMod.LOGGER.info("Server-side performance collector initialized");
    }
    
    /**
     * Collects client-side performance data
     * Called every client tick with rate limiting
     */
    @Environment(EnvType.CLIENT)
    public static void collectClientData(MinecraftClient client) {
        if (!clientInitialized || !ProfilerConfig.isRealtimeMonitoringEnabled()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClientCollection.get() < CLIENT_COLLECTION_INTERVAL_MS) {
            return;
        }
        
        lastClientCollection.set(currentTime);
        
        try {
            PerformanceFrame frame = createClientFrame(client, currentTime);
            if (frame != null) {
                clientFrameBuffer.add(frame);
                
                // Queue for analysis if frame shows potential issues
                if (frame.hasPerformanceIssues()) {
                    pendingAnalysis.offer(frame);
                }
            }
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.warn("Error collecting client performance data", e);
        }
    }
    
    /**
     * Collects server-side performance data
     * Called every server tick with rate limiting
     */
    public static void collectServerData(MinecraftServer server) {
        if (!serverInitialized || !ProfilerConfig.isServerMonitoringEnabled()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastServerCollection.get() < SERVER_COLLECTION_INTERVAL_MS) {
            return;
        }
        
        lastServerCollection.set(currentTime);
        
        try {
            PerformanceFrame frame = createServerFrame(server, currentTime);
            if (frame != null) {
                serverFrameBuffer.add(frame);
                
                // Queue for analysis if frame shows potential issues
                if (frame.hasPerformanceIssues()) {
                    pendingAnalysis.offer(frame);
                }
            }
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.warn("Error collecting server performance data", e);
        }
    }
    
    /**
     * Creates a performance frame from client-side data
     */
    @Environment(EnvType.CLIENT)
    private static PerformanceFrame createClientFrame(MinecraftClient client, long timestamp) {
        if (client.world == null || client.player == null) {
            return null;
        }
        
        ClientWorld world = client.world;
        
        return new PerformanceFrame.Builder()
            .timestamp(timestamp)
            .frameType(FrameType.CLIENT)
            .fps(client.getCurrentFps())
            .memoryStats(collectMemoryStats())
            .chunkStats(collectClientChunkStats(world))
            .entityStats(collectClientEntityStats(world))
            .renderStats(collectRenderStats(client))
            .networkStats(collectClientNetworkStats(client))
            .build();
    }
    
    /**
     * Creates a performance frame from server-side data
     */
    private static PerformanceFrame createServerFrame(MinecraftServer server, long timestamp) {
        if (server.getOverworld() == null) {
            return null;
        }
        
        return new PerformanceFrame.Builder()
            .timestamp(timestamp)
            .frameType(FrameType.SERVER)
            .tps(calculateServerTPS(server))
            .memoryStats(collectMemoryStats())
            .chunkStats(collectServerChunkStats(server))
            .entityStats(collectServerEntityStats(server))
            .networkStats(collectServerNetworkStats(server))
            .build();
    }
    
    /**
     * Collects memory statistics using JVM management beans
     */
    private static MemoryStats collectMemoryStats() {
        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            // Calculate GC statistics
            long totalGcTime = 0;
            long totalGcCollections = 0;
            
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                long gcTime = gcBean.getCollectionTime();
                long gcCount = gcBean.getCollectionCount();
                
                if (gcTime > 0) totalGcTime += gcTime;
                if (gcCount > 0) totalGcCollections += gcCount;
            }
            
            return new MemoryStats.Builder()
                .heapUsed(heapUsage.getUsed())
                .heapMax(heapUsage.getMax())
                .heapCommitted(heapUsage.getCommitted())
                .nonHeapUsed(nonHeapUsage.getUsed())
                .nonHeapMax(nonHeapUsage.getMax())
                .nonHeapCommitted(nonHeapUsage.getCommitted())
                .gcTime(totalGcTime)
                .gcCollections(totalGcCollections)
                .build();
                
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.warn("Error collecting memory statistics", e);
            return MemoryStats.EMPTY;
        }
    }
    
    /**
     * Collects chunk statistics for client-side analysis
     */
    @Environment(EnvType.CLIENT)
    private static ChunkStats collectClientChunkStats(ClientWorld world) {
        try {
            ChunkManager chunkManager = world.getChunkManager();
            
            return new ChunkStats.Builder()
                .loadedChunks(getLoadedChunkCount(chunkManager))
                .renderDistance(MinecraftClient.getInstance().options.getViewDistance().getValue())
                .chunkUpdates(getChunkUpdateCount())
                .build();
                
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.warn("Error collecting client chunk statistics", e);
            return ChunkStats.EMPTY;
        }
    }
    
    /**
     * Processes the analysis queue for bottleneck detection
     */
    private static void processAnalysisQueue() {
        try {
            PerformanceFrame frame;
            int processed = 0;
            
            while ((frame = pendingAnalysis.poll()) != null && processed < 10) {
                BottleneckAnalyzer.analyzeFrame(frame);
                processed++;
            }
            
            if (processed > 0) {
                SmartProfilerMod.LOGGER.debug("Processed {} performance frames for analysis", processed);
            }
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.warn("Error processing analysis queue", e);
        }
    }
    
    /**
     * Called when server starts
     */
    public static void onServerStarted(MinecraftServer server) {
        SmartProfilerMod.LOGGER.info("Server started - performance monitoring active");
        // Reset server performance data
        serverFrameBuffer.clear();
    }
    
    /**
     * Gets recent client performance frames
     */
    @Environment(EnvType.CLIENT)
    public static List<PerformanceFrame> getRecentClientFrames(int count) {
        return clientFrameBuffer.getRecent(count);
    }
    
    /**
     * Gets recent server performance frames  
     */
    public static List<PerformanceFrame> getRecentServerFrames(int count) {
        return serverFrameBuffer.getRecent(count);
    }
    
    /**
     * Cleanup method for mod shutdown
     */
    public static void shutdown() {
        if (analysisExecutor != null && !analysisExecutor.isShutdown()) {
            analysisExecutor.shutdown();
            try {
                if (!analysisExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    analysisExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                analysisExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        SmartProfilerMod.LOGGER.info("Performance collector shutdown complete");
    }
    
    // Helper methods for data collection
    private static int getLoadedChunkCount(ChunkManager chunkManager) {
        // Implementation depends on accessing chunk manager internals
        return 0; // Placeholder
    }
    
    private static int getChunkUpdateCount() {
        // Track chunk updates per frame
        return 0; // Placeholder  
    }
    
    private static EntityStats collectClientEntityStats(ClientWorld world) {
        return EntityStats.EMPTY; // Placeholder
    }
    
    private static RenderStats collectRenderStats(MinecraftClient client) {
        return RenderStats.EMPTY; // Placeholder
    }
    
    private static NetworkStats collectClientNetworkStats(MinecraftClient client) {
        return NetworkStats.EMPTY; // Placeholder
    }
    
    private static double calculateServerTPS(MinecraftServer server) {
        return server.getTickTime(); // Simplified placeholder
    }
    
    private static ChunkStats collectServerChunkStats(MinecraftServer server) {
        return ChunkStats.EMPTY; // Placeholder
    }
    
    private static EntityStats collectServerEntityStats(MinecraftServer server) {
        return EntityStats.EMPTY; // Placeholder
    }
    
    private static NetworkStats collectServerNetworkStats(MinecraftServer server) {
        return NetworkStats.EMPTY; // Placeholder
    }
}