package dev.hiba550.smartprofiler;

import dev.hiba550.smartprofiler.client.KeyBindings;
import dev.hiba550.smartprofiler.client.overlay.PerformanceOverlayRenderer;
import dev.hiba550.smartprofiler.client.screen.ProfilerConfigScreen;
import dev.hiba550.smartprofiler.data.PerformanceCollector;
import dev.hiba550.smartprofiler.network.ProfilerNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Client-side initialization for Smart Performance Profiler
 * 
 * Handles:
 * - Client-side performance monitoring
 * - Overlay rendering
 * - Key binding registration
 * - Network packet handling
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since 1.21.5
 */
@Environment(EnvType.CLIENT)
public class SmartProfilerClientMod implements ClientModInitializer {
    
    private static boolean clientInitialized = false;
    
    @Override
    public void onInitializeClient() {
        SmartProfilerMod.LOGGER.info("Initializing Smart Performance Profiler client-side features");
        
        try {
            // Register key bindings for profiler controls
            KeyBindings.initialize();
            SmartProfilerMod.LOGGER.debug("Key bindings registered");
            
            // Register client networking
            ProfilerNetworking.registerClientPackets();
            SmartProfilerMod.LOGGER.debug("Client networking registered");
            
            // Register HUD overlay renderer
            HudRenderCallback.EVENT.register(PerformanceOverlayRenderer::renderOverlay);
            SmartProfilerMod.LOGGER.debug("Overlay renderer registered");
            
            // Register client tick events for performance collection
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                try {
                    // Handle key bindings
                    KeyBindings.handleKeyPresses();
                    
                    // Collect client-side performance data
                    if (client.world != null && client.player != null) {
                        PerformanceCollector.collectClientData(client);
                    }
                } catch (Exception e) {
                    SmartProfilerMod.LOGGER.warn("Error during client tick processing", e);
                }
            });
            
            // Initialize client-side performance collector
            PerformanceCollector.initializeClient();
            
            clientInitialized = true;
            SmartProfilerMod.LOGGER.info("Smart Performance Profiler client initialized successfully!");
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Failed to initialize client-side features", e);
            throw new RuntimeException("Client initialization failed", e);
        }
    }
    
    /**
     * Checks if client-side features are initialized
     * @return true if client initialization completed successfully
     */
    public static boolean isClientInitialized() {
        return clientInitialized;
    }
}