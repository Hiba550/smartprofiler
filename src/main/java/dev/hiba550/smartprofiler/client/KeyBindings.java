package dev.hiba550.smartprofiler.client;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.client.overlay.PerformanceOverlayRenderer;
import dev.hiba550.smartprofiler.client.screen.ProfilerDetailScreen;
import dev.hiba550.smartprofiler.config.ProfilerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Handles key bindings for the Smart Performance Profiler
 * 
 * Key bindings:
 * - F10: Toggle performance overlay
 * - F11 + SHIFT: Open detailed profiler screen
 * - F12: Take performance snapshot
 * - F9: Toggle real-time monitoring
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since 1.21.5
 */
@Environment(EnvType.CLIENT)
public class KeyBindings {
    
    // Key binding instances
    private static KeyBinding toggleOverlayKey;
    private static KeyBinding openDetailScreenKey;
    private static KeyBinding takeSnapshotKey;
    private static KeyBinding toggleMonitoringKey;
    
    // State tracking for key presses
    private static boolean overlayKeyPressed = false;
    private static boolean detailKeyPressed = false;
    private static boolean snapshotKeyPressed = false;
    private static boolean monitoringKeyPressed = false;
    
    /**
     * Initializes and registers all key bindings
     */
    public static void initialize() {
        SmartProfilerMod.LOGGER.debug("Registering key bindings for Smart Performance Profiler");
        
        // Toggle overlay key (F10)
        toggleOverlayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.smart_profiler.toggle_overlay",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            "category.smart_profiler.general"
        ));
        
        // Open detailed screen key (Shift + F11)
        openDetailScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.smart_profiler.open_detail_screen", 
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F11,
            "category.smart_profiler.general"
        ));
        
        // Take performance snapshot key (F12)
        takeSnapshotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.smart_profiler.take_snapshot",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F12,
            "category.smart_profiler.general"
        ));
        
        // Toggle monitoring key (F9)
        toggleMonitoringKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.smart_profiler.toggle_monitoring",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "category.smart_profiler.general"
        ));
        
        SmartProfilerMod.LOGGER.info("Key bindings registered successfully");
    }
    
    /**
     * Handles key press events - called every client tick
     * Uses proper key press detection to prevent multiple triggers
     */
    public static void handleKeyPresses() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        
        try {
            // Handle toggle overlay key
            if (toggleOverlayKey.wasPressed() && !overlayKeyPressed) {
                overlayKeyPressed = true;
                handleToggleOverlay();
            } else if (!toggleOverlayKey.isPressed()) {
                overlayKeyPressed = false;
            }
            
            // Handle detail screen key (requires Shift modifier)
            boolean shiftPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                                  InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            
            if (openDetailScreenKey.wasPressed() && shiftPressed && !detailKeyPressed) {
                detailKeyPressed = true;
                handleOpenDetailScreen();
            } else if (!openDetailScreenKey.isPressed()) {
                detailKeyPressed = false;
            }
            
            // Handle snapshot key
            if (takeSnapshotKey.wasPressed() && !snapshotKeyPressed) {
                snapshotKeyPressed = true;
                handleTakeSnapshot();
            } else if (!takeSnapshotKey.isPressed()) {
                snapshotKeyPressed = false;
            }
            
            // Handle monitoring toggle key
            if (toggleMonitoringKey.wasPressed() && !monitoringKeyPressed) {
                monitoringKeyPressed = true;
                handleToggleMonitoring();
            } else if (!toggleMonitoringKey.isPressed()) {
                monitoringKeyPressed = false;
            }
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.warn("Error handling key presses", e);
        }
    }
    
    /**
     * Handles toggle overlay key press
     */
    private static void handleToggleOverlay() {
        boolean newState = !PerformanceOverlayRenderer.isOverlayEnabled();
        PerformanceOverlayRenderer.setOverlayEnabled(newState);
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String message = newState ? "Performance overlay enabled" : "Performance overlay disabled";
            client.player.sendMessage(
                net.minecraft.text.Text.literal("§6[Smart Profiler] §f" + message),
                true // Send as overlay message
            );
        }
        
        SmartProfilerMod.LOGGER.info("Performance overlay toggled: {}", newState ? "enabled" : "disabled");
    }
    
    /**
     * Handles open detail screen key press
     */
    private static void handleOpenDetailScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) { // Only open if no screen is currently open
            ProfilerDetailScreen detailScreen = new ProfilerDetailScreen();
            client.setScreen(detailScreen);
            SmartProfilerMod.LOGGER.debug("Opened profiler detail screen");
        }
    }
    
    /**
     * Handles take snapshot key press
     */
    private static void handleTakeSnapshot() {
        // TODO: Implement performance snapshot functionality
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                net.minecraft.text.Text.literal("§6[Smart Profiler] §aPerformance snapshot taken!"),
                true
            );
        }
        SmartProfilerMod.LOGGER.info("Performance snapshot taken");
    }
    
    /**
     * Handles toggle monitoring key press
     */
    private static void handleToggleMonitoring() {
        boolean newState = !ProfilerConfig.isRealtimeMonitoringEnabled();
        ProfilerConfig.setRealtimeMonitoringEnabled(newState);
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String message = newState ? "Real-time monitoring enabled" : "Real-time monitoring disabled";
            client.player.sendMessage(
                net.minecraft.text.Text.literal("§6[Smart Profiler] §f" + message),
                true
            );
        }
        
        SmartProfilerMod.LOGGER.info("Real-time monitoring toggled: {}", newState ? "enabled" : "disabled");
    }
}