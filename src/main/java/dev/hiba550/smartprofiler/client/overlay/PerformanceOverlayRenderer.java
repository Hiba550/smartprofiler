package dev.hiba550.smartprofiler.client.overlay;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.config.ProfilerConfig;
import dev.hiba550.smartprofiler.data.PerformanceCollector;
import dev.hiba550.smartprofiler.data.models.PerformanceFrame;
import dev.hiba550.smartprofiler.data.models.PerformanceIssue;
import dev.hiba550.smartprofiler.util.ColorUtils;
import dev.hiba550.smartprofiler.util.MathUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.EnumMap;
import java.util.Map;

/**
 * Renders real-time performance overlay with minimal rendering overhead
 * 
 * Features:
 * - Multi-panel layout with customizable positioning
 * - Real-time performance graphs with smooth animations
 * - Color-coded warnings and alerts
 * - Efficient batched rendering
 * - Smart update throttling to reduce performance impact
 * - Responsive design that adapts to screen size
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
@Environment(EnvType.CLIENT)
public class PerformanceOverlayRenderer {
    
    // Color constants for performance indicators
    private static final int COLOR_EXCELLENT = 0xFF40FF40;  // Bright green
    private static final int COLOR_GOOD = 0xFF80FF80;       // Light green
    private static final int COLOR_WARNING = 0xFFFFFF40;    // Yellow
    private static final int COLOR_CRITICAL = 0xFFFF4040;   // Red
    private static final int COLOR_BACKGROUND = 0x80000000; // Semi-transparent black
    private static final int COLOR_BORDER = 0xFF606060;     // Gray
    private static final int COLOR_TEXT = 0xFFFFFFFF;       // White
    private static final int COLOR_TEXT_SECONDARY = 0xFFC0C0C0; // Light gray
    
    // Layout constants
    private static final int PANEL_MARGIN = 8;
    private static final int PANEL_PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    private static final int GRAPH_HEIGHT = 40;
    private static final int GRAPH_WIDTH = 120;
    
    // Update timing control
    private static final long RENDER_INTERVAL_MS = 100; // 10 FPS for overlay updates
    private static final long GRAPH_UPDATE_INTERVAL_MS = 50; // 20 FPS for graphs
    
    // State management
    private static boolean overlayEnabled = false;
    private static OverlayMode currentMode = OverlayMode.COMPACT;
    private static OverlayPosition position = OverlayPosition.TOP_LEFT;
    private static long lastRenderTime = 0;
    private static long lastGraphUpdate = 0;
    private static float overlayOpacity = 1.0f;
    
    // Performance data cache
    private static final int GRAPH_DATA_POINTS = 100;
    private static final float[] fpsGraphData = new float[GRAPH_DATA_POINTS];
    private static final float[] memoryGraphData = new float[GRAPH_DATA_POINTS];
    private static final float[] chunkGraphData = new float[GRAPH_DATA_POINTS];
    private static int graphDataIndex = 0;
    
    // Animation state
    private static float fadeAnimation = 0.0f;
    private static float warningPulse = 0.0f;
    private static final Map<PerformanceIssue, Float> alertAnimations = new EnumMap<>(PerformanceIssue.class);
    
    /**
     * Main overlay rendering method called every frame
     * Implements smart throttling and efficient rendering pipeline
     */
    public static void renderOverlay(DrawContext context, float tickDelta) {
        if (!overlayEnabled || !ProfilerConfig.isOverlayEnabled()) {
            handleFadeOut(tickDelta);
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Handle fade in animation
        handleFadeIn(tickDelta);
        
        // Throttle rendering to reduce performance impact
        if (currentTime - lastRenderTime < RENDER_INTERVAL_MS) {
            return;
        }
        lastRenderTime = currentTime;
        
        try {
            // Update performance data cache
            updatePerformanceCache(client, currentTime);
            
            // Update animations
            updateAnimations(tickDelta);
            
            // Render based on current mode
            switch (currentMode) {
                case MINIMAL -> renderMinimalOverlay(context, client);
                case COMPACT -> renderCompactOverlay(context, client);
                case DETAILED -> renderDetailedOverlay(context, client);
                case GRAPH_ONLY -> renderGraphOnlyOverlay(context, client);
            }
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.warn("Error rendering performance overlay", e);
        }
    }
    
    /**
     * Updates cached performance data for rendering
     */
    private static void updatePerformanceCache(MinecraftClient client, long currentTime) {
        if (currentTime - lastGraphUpdate < GRAPH_UPDATE_INTERVAL_MS) {
            return;
        }
        lastGraphUpdate = currentTime;
        
        // Get latest performance frame
        List<PerformanceFrame> recentFrames = PerformanceCollector.getRecentClientFrames(1);
        if (recentFrames.isEmpty()) {
            return;
        }
        
        PerformanceFrame latestFrame = recentFrames.get(0);
        
        // Update graph data arrays
        fpsGraphData[graphDataIndex] = latestFrame.getFps();
        memoryGraphData[graphDataIndex] = latestFrame.getMemoryStats().getHeapUsagePercent();
        chunkGraphData[graphDataIndex] = latestFrame.getChunkStats().getLoadedChunks();
        
        graphDataIndex = (graphDataIndex + 1) % GRAPH_DATA_POINTS;
    }
    
    /**
     * Updates overlay animations
     */
    private static void updateAnimations(float tickDelta) {
        // Update warning pulse animation
        warningPulse += tickDelta * 0.1f;
        if (warningPulse > Math.PI * 2) {
            warningPulse -= Math.PI * 2;
        }
        
        // Update alert animations
        for (PerformanceIssue issue : PerformanceIssue.values()) {
            Float currentValue = alertAnimations.get(issue);
            if (currentValue == null) {
                alertAnimations.put(issue, 0.0f);
            } else {
                // Decay animation over time
                alertAnimations.put(issue, Math.max(0.0f, currentValue - tickDelta * 0.05f));
            }
        }
    }
    
    /**
     * Handles fade in animation
     */
    private static void handleFadeIn(float tickDelta) {
        if (fadeAnimation < 1.0f) {
            fadeAnimation = Math.min(1.0f, fadeAnimation + tickDelta * 0.1f);
            overlayOpacity = MathHelper.lerp(fadeAnimation, 0.0f, 1.0f);
        }
    }
    
    /**
     * Handles fade out animation
     */
    private static void handleFadeOut(float tickDelta) {
        if (fadeAnimation > 0.0f) {
            fadeAnimation = Math.max(0.0f, fadeAnimation - tickDelta * 0.15f);
            overlayOpacity = MathHelper.lerp(fadeAnimation, 0.0f, 1.0f);
        }
    }
    
    /**
     * Renders minimal overlay - just FPS and basic info
     */
    private static void renderMinimalOverlay(DrawContext context, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;
        
        List<PerformanceFrame> frames = PerformanceCollector.getRecentClientFrames(1);
        if (frames.isEmpty()) return;
        
        PerformanceFrame frame = frames.get(0);
        
        int x = getOverlayX(client, 80);
        int y = getOverlayY(client, 20);
        
        // Background
        drawBackground(context, x - 2, y - 2, 80, 20);
        
        // FPS with color coding
        int fpsColor = getFpsColor(frame.getFps());
        String fpsText = String.format("FPS: %d", frame.getFps());
        context.drawText(textRenderer, fpsText, x, y, 
            ColorUtils.multiplyAlpha(fpsColor, overlayOpacity), false);
        
        // Memory usage
        float memoryPercent = frame.getMemoryStats().getHeapUsagePercent();
        int memoryColor = getMemoryColor(memoryPercent);
        String memoryText = String.format("Mem: %.1f%%", memoryPercent);
        context.drawText(textRenderer, memoryText, x, y + LINE_HEIGHT, 
            ColorUtils.multiplyAlpha(memoryColor, overlayOpacity), false);
    }
    
    /**
     * Renders compact overlay - main performance metrics
     */
    private static void renderCompactOverlay(DrawContext context, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;
        
        List<PerformanceFrame> frames = PerformanceCollector.getRecentClientFrames(1);
        if (frames.isEmpty()) return;
        
        PerformanceFrame frame = frames.get(0);
        
        int panelWidth = 160;
        int panelHeight = 80;
        int x = getOverlayX(client, panelWidth);
        int y = getOverlayY(client, panelHeight);
        
        // Background panel
        drawBackground(context, x - PANEL_PADDING, y - PANEL_PADDING, 
            panelWidth + PANEL_PADDING * 2, panelHeight + PANEL_PADDING * 2);
        
        int currentY = y;
        
        // Title
        context.drawText(textRenderer, "Performance Monitor", x, currentY, 
            ColorUtils.multiplyAlpha(COLOR_TEXT, overlayOpacity), false);
        currentY += LINE_HEIGHT + 2;
        
        // FPS
        int fpsColor = getFpsColor(frame.getFps());
        String fpsText = String.format("FPS: %d", frame.getFps());
        context.drawText(textRenderer, fpsText, x, currentY, 
            ColorUtils.multiplyAlpha(fpsColor, overlayOpacity), false);
        currentY += LINE_HEIGHT;
        
        // Memory
        float memoryPercent = frame.getMemoryStats().getHeapUsagePercent();
        int memoryColor = getMemoryColor(memoryPercent);
        String memoryText = String.format("Memory: %.1f%%", memoryPercent);
        context.drawText(textRenderer, memoryText, x, currentY, 
            ColorUtils.multiplyAlpha(memoryColor, overlayOpacity), false);
        currentY += LINE_HEIGHT;
        
        // Chunks
        int chunkCount = frame.getChunkStats().getLoadedChunks();
        String chunkText = String.format("Chunks: %d", chunkCount);
        context.drawText(textRenderer, chunkText, x, currentY, 
            ColorUtils.multiplyAlpha(COLOR_TEXT_SECONDARY, overlayOpacity), false);
        currentY += LINE_HEIGHT;
        
        // Entities
        int entityCount = frame.getEntityStats().getTotalEntities();
        String entityText = String.format("Entities: %d", entityCount);
        context.drawText(textRenderer, entityText, x, currentY, 
            ColorUtils.multiplyAlpha(COLOR_TEXT_SECONDARY, overlayOpacity), false);
        currentY += LINE_HEIGHT;
        
        // Performance warnings
        renderPerformanceWarnings(context, textRenderer, frame, x, currentY);
    }
    
    /**
     * Renders detailed overlay with graphs and comprehensive metrics
     */
    private static void renderDetailedOverlay(DrawContext context, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;
        
        List<PerformanceFrame> frames = PerformanceCollector.getRecentClientFrames(1);
        if (frames.isEmpty()) return;
        
        PerformanceFrame frame = frames.get(0);
        
        int panelWidth = 250;
        int panelHeight = 200;
        int x = getOverlayX(client, panelWidth);
        int y = getOverlayY(client, panelHeight);
        
        // Background panel
        drawBackground(context, x - PANEL_PADDING, y - PANEL_PADDING, 
            panelWidth + PANEL_PADDING * 2, panelHeight + PANEL_PADDING * 2);
        
        int currentY = y;
        
        // Title with status indicator
        String title = "Smart Performance Profiler";
        context.drawText(textRenderer, title, x, currentY, 
            ColorUtils.multiplyAlpha(COLOR_TEXT, overlayOpacity), false);
        
        // Status indicator
        int statusColor = frame.hasPerformanceIssues() ? COLOR_WARNING : COLOR_GOOD;
        drawStatusIndicator(context, x + textRenderer.getWidth(title) + 8, currentY + 2, statusColor);
        currentY += LINE_HEIGHT + 4;
        
        // Performance metrics section
        renderDetailedMetrics(context, textRenderer, frame, x, currentY);
        currentY += 60;
        
        // Mini graphs section
        renderMiniGraphs(context, x, currentY);
        currentY += GRAPH_HEIGHT + 10;
        
        // Active warnings
        renderActiveWarnings(context, textRenderer, frame, x, currentY);
    }
    
    /**
     * Renders graph-only overlay for minimal visual impact
     */
    private static void renderGraphOnlyOverlay(DrawContext context, MinecraftClient client) {
        int graphWidth = GRAPH_WIDTH + 20;
        int graphHeight = GRAPH_HEIGHT + 10;
        int x = getOverlayX(client, graphWidth);
        int y = getOverlayY(client, graphHeight);
        
        // Semi-transparent background
        drawBackground(context, x - 5, y - 5, graphWidth + 10, graphHeight + 10);
        
        // Render single performance graph (FPS by default)
        renderPerformanceGraph(context, fpsGraphData, x, y, GRAPH_WIDTH, GRAPH_HEIGHT, 
            "FPS", 0, 120, COLOR_GOOD);
    }
    
    /**
     * Renders detailed performance metrics
     */
    private static void renderDetailedMetrics(DrawContext context, TextRenderer textRenderer, 
                                            PerformanceFrame frame, int x, int y) {
        int currentY = y;
        
        // FPS with frame time
        int fpsColor = getFpsColor(frame.getFps());
        double frameTime = 1000.0 / Math.max(1, frame.getFps());
        String fpsText = String.format("FPS: %d (%.1fms)", frame.getFps(), frameTime);
        context.drawText(textRenderer, fpsText, x, currentY, 
            ColorUtils.multiplyAlpha(fpsColor, overlayOpacity), false);
        currentY += LINE_HEIGHT;
        
        // Memory detailed
        var memStats = frame.getMemoryStats();
        float memoryPercent = memStats.getHeapUsagePercent();
        long memoryMB = memStats.getHeapUsed() / (1024 * 1024);
        long maxMemoryMB = memStats.getHeapMax() / (1024 * 1024);
        int memoryColor = getMemoryColor(memoryPercent);
        String memoryText = String.format("Memory: %dMB/%dMB (%.1f%%)", 
            memoryMB, maxMemoryMB, memoryPercent);
        context.drawText(textRenderer, memoryText, x, currentY, 
            ColorUtils.multiplyAlpha(memoryColor, overlayOpacity), false);
        currentY += LINE_HEIGHT;
        
        // GC information
        if (memStats.getGcCollections() > 0) {
            String gcText = String.format("GC: %d collections, %dms", 
                memStats.getGcCollections(), memStats.getGcTime());
            context.drawText(textRenderer, gcText, x, currentY, 
                ColorUtils.multiplyAlpha(COLOR_TEXT_SECONDARY, overlayOpacity), false);
            currentY += LINE_HEIGHT;
        }
        
        // Chunk information
        var chunkStats = frame.getChunkStats();
        String chunkText = String.format("Chunks: %d loaded, RD: %d", 
            chunkStats.getLoadedChunks(), chunkStats.getRenderDistance());
        context.drawText(textRenderer, chunkText, x, currentY, 
            ColorUtils.multiplyAlpha(COLOR_TEXT_SECONDARY, overlayOpacity), false);
        currentY += LINE_HEIGHT;
        
        // Entity information
        var entityStats = frame.getEntityStats();
        String entityText = String.format("Entities: %d total, %d rendered", 
            entityStats.getTotalEntities(), entityStats.getRenderedEntities());
        context.drawText(textRenderer, entityText, x, currentY, 
            ColorUtils.multiplyAlpha(COLOR_TEXT_SECONDARY, overlayOpacity), false);
    }
    
    /**
     * Renders mini performance graphs
     */
    private static void renderMiniGraphs(DrawContext context, int x, int y) {
        int graphSpacing = 80;
        
        // FPS graph
        renderPerformanceGraph(context, fpsGraphData, x, y, 70, 30, 
            "FPS", 0, 120, COLOR_GOOD);
        
        // Memory graph
        renderPerformanceGraph(context, memoryGraphData, x + graphSpacing, y, 70, 30, 
            "MEM", 0, 100, COLOR_WARNING);
    }
    
    /**
     * Renders a performance graph with specified parameters
     */
    private static void renderPerformanceGraph(DrawContext context, float[] data, 
                                             int x, int y, int width, int height,
                                             String label, float minValue, float maxValue, int color) {
        MatrixStack matrices = context.getMatrices();
        
        // Draw graph background
        context.fill(x, y, x + width, y + height, 
            ColorUtils.multiplyAlpha(COLOR_BACKGROUND, overlayOpacity * 0.8f));
        
        // Draw graph border
        drawBorder(context, x, y, width, height);
        
        // Draw graph lines
        matrices.push();
        
        for (int i = 1; i < GRAPH_DATA_POINTS && i < width; i++) {
            int dataIndex1 = (graphDataIndex - GRAPH_DATA_POINTS + i - 1 + GRAPH_DATA_POINTS) % GRAPH_DATA_POINTS;
            int dataIndex2 = (graphDataIndex - GRAPH_DATA_POINTS + i + GRAPH_DATA_POINTS) % GRAPH_DATA_POINTS;
            
            float value1 = MathHelper.clamp((data[dataIndex1] - minValue) / (maxValue - minValue), 0.0f, 1.0f);
            float value2 = MathHelper.clamp((data[dataIndex2] - minValue) / (maxValue - minValue), 0.0f, 1.0f);
            
            int x1 = x + (i - 1) * width / GRAPH_DATA_POINTS;
            int y1 = y + height - (int)(value1 * height);
            int x2 = x + i * width / GRAPH_DATA_POINTS;
            int y2 = y + height - (int)(value2 * height);
            
            drawLine(context, x1, y1, x2, y2, ColorUtils.multiplyAlpha(color, overlayOpacity));
        }
        
        matrices.pop();
        
        // Draw label
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.textRenderer != null) {
            context.drawText(client.textRenderer, label, x + 2, y + 2, 
                ColorUtils.multiplyAlpha(COLOR_TEXT, overlayOpacity), false);
        }
    }
    
    /**
     * Renders performance warnings and alerts
     */
    private static void renderPerformanceWarnings(DrawContext context, TextRenderer textRenderer, 
                                                 PerformanceFrame frame, int x, int y) {
        if (!frame.hasPerformanceIssues()) {
            return;
        }
        
        int currentY = y;
        
        for (PerformanceIssue issue : frame.getDetectedIssues()) {
            // Get warning color with pulsing effect
            float pulse = (float)(0.7f + 0.3f * Math.sin(warningPulse * 3));
            int warningColor = ColorUtils.multiplyAlpha(getIssueColor(issue), overlayOpacity * pulse);
            
            String warningText = getIssueDisplayText(issue);
            context.drawText(textRenderer, "⚠ " + warningText, x, currentY, warningColor, false);
            currentY += LINE_HEIGHT;
            
            // Trigger alert animation
            alertAnimations.put(issue, 1.0f);
        }
    }
    
    /**
     * Renders active warnings in detailed view
     */
    private static void renderActiveWarnings(DrawContext context, TextRenderer textRenderer, 
                                           PerformanceFrame frame, int x, int y) {
        if (!frame.hasPerformanceIssues()) {
            context.drawText(textRenderer, "✓ No performance issues detected", x, y, 
                ColorUtils.multiplyAlpha(COLOR_GOOD, overlayOpacity), false);
            return;
        }
        
        context.drawText(textRenderer, "Active Warnings:", x, y, 
            ColorUtils.multiplyAlpha(COLOR_WARNING, overlayOpacity), false);
        
        int currentY = y + LINE_HEIGHT;
        for (PerformanceIssue issue : frame.getDetectedIssues()) {
            String warningText = "• " + getIssueDisplayText(issue);
            context.drawText(textRenderer, warningText, x + 8, currentY, 
                ColorUtils.multiplyAlpha(getIssueColor(issue), overlayOpacity), false);
            currentY += LINE_HEIGHT;
        }
    }
    
    /**
     * Draws a status indicator circle
     */
    private static void drawStatusIndicator(DrawContext context, int x, int y, int color) {
        int size = 6;
        context.fill(x, y, x + size, y + size, ColorUtils.multiplyAlpha(color, overlayOpacity));
    }
    
    /**
     * Draws a background panel with border
     */
    private static void drawBackground(DrawContext context, int x, int y, int width, int height) {
        // Background
        context.fill(x, y, x + width, y + height, 
            ColorUtils.multiplyAlpha(COLOR_BACKGROUND, overlayOpacity * 0.9f));
        
        // Border
        drawBorder(context, x, y, width, height);
    }
    
    /**
     * Draws a border around the specified area
     */
    private static void drawBorder(DrawContext context, int x, int y, int width, int height) {
        int borderColor = ColorUtils.multiplyAlpha(COLOR_BORDER, overlayOpacity);
        
        // Top and bottom
        context.fill(x, y, x + width, y + 1, borderColor);
        context.fill(x, y + height - 1, x + width, y + height, borderColor);
        
        // Left and right
        context.fill(x, y, x + 1, y + height, borderColor);
        context.fill(x + width - 1, y, x + width, y + height, borderColor);
    }
    
    /**
     * Draws a line between two points
     */
    private static void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        // Simple line drawing using fill for single pixel lines
        if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
            // More horizontal than vertical
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            for (int x = startX; x <= endX; x++) {
                int y = y1 + (y2 - y1) * (x - x1) / Math.max(1, x2 - x1);
                context.fill(x, y, x + 1, y + 1, color);
            }
        } else {
            // More vertical than horizontal
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                int x = x1 + (x2 - x1) * (y - y1) / Math.max(1, y2 - y1);
                context.fill(x, y, x + 1, y + 1, color);
            }
        }
    }
    
    // Utility methods for positioning and colors
    
    private static int getOverlayX(MinecraftClient client, int panelWidth) {
        return switch (position) {
            case TOP_LEFT, BOTTOM_LEFT -> PANEL_MARGIN;
            case TOP_RIGHT, BOTTOM_RIGHT -> client.getWindow().getScaledWidth() - panelWidth - PANEL_MARGIN;
            case CENTER -> (client.getWindow().getScaledWidth() - panelWidth) / 2;
        };
    }
    
    private static int getOverlayY(MinecraftClient client, int panelHeight) {
        return switch (position) {
            case TOP_LEFT, TOP_RIGHT -> PANEL_MARGIN;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> client.getWindow().getScaledHeight() - panelHeight - PANEL_MARGIN;
            case CENTER -> (client.getWindow().getScaledHeight() - panelHeight) / 2;
        };
    }
    
    private static int getFpsColor(int fps) {
        if (fps >= 60) return COLOR_EXCELLENT;
        if (fps >= 45) return COLOR_GOOD;
        if (fps >= 30) return COLOR_WARNING;
        return COLOR_CRITICAL;
    }
    
    private static int getMemoryColor(float memoryPercent) {
        if (memoryPercent <= 60) return COLOR_GOOD;
        if (memoryPercent <= 80) return COLOR_WARNING;
        return COLOR_CRITICAL;
    }
    
    private static int getIssueColor(PerformanceIssue issue) {
        return switch (issue.getSeverity()) {
            case LOW -> COLOR_WARNING;
            case MEDIUM -> COLOR_WARNING;
            case HIGH -> COLOR_CRITICAL;
            case CRITICAL -> COLOR_CRITICAL;
        };
    }
    
    private static String getIssueDisplayText(PerformanceIssue issue) {
        return switch (issue) {
            case LOW_FPS -> "Low FPS detected";
            case HIGH_MEMORY_USAGE -> "High memory usage";
            case MEMORY_LEAK -> "Potential memory leak";
            case EXCESSIVE_CHUNKS -> "Too many chunks loaded";
            case ENTITY_LAG -> "Entity processing lag";
            case RENDER_LAG -> "Rendering bottleneck";
            case GC_PRESSURE -> "Garbage collection pressure";
            case NETWORK_LAG -> "Network performance issues";
        };
    }
    
    // Public API methods
    
    public static boolean isOverlayEnabled() {
        return overlayEnabled;
    }
    
    public static void setOverlayEnabled(boolean enabled) {
        overlayEnabled = enabled;
        if (enabled) {
            fadeAnimation = 0.0f; // Start fade in
        }
        SmartProfilerMod.LOGGER.debug("Overlay enabled: {}", enabled);
    }
    
    public static void toggleOverlay() {
        setOverlayEnabled(!overlayEnabled);
    }
    
    public static OverlayMode getCurrentMode() {
        return currentMode;
    }
    
    public static void setOverlayMode(OverlayMode mode) {
        currentMode = mode;
        SmartProfilerMod.LOGGER.debug("Overlay mode changed to: {}", mode);
    }
    
    public static void cycleOverlayMode() {
        OverlayMode[] modes = OverlayMode.values();
        int currentIndex = currentMode.ordinal();
        int nextIndex = (currentIndex + 1) % modes.length;
        setOverlayMode(modes[nextIndex]);
    }
    
    public static OverlayPosition getPosition() {
        return position;
    }
    
    public static void setPosition(OverlayPosition newPosition) {
        position = newPosition;
        SmartProfilerMod.LOGGER.debug("Overlay position changed to: {}", newPosition);
    }
    
    public static void cyclePosition() {
        OverlayPosition[] positions = OverlayPosition.values();
        int currentIndex = position.ordinal();
        int nextIndex = (currentIndex + 1) % positions.length;
        setPosition(positions[nextIndex]);
    }
    
    /**
     * Gets current overlay opacity for external renderers
     */
    public static float getOverlayOpacity() {
        return overlayOpacity;
    }
    
    /**
     * Cleanup method called on mod shutdown
     */
    public static void cleanup() {
        overlayEnabled = false;
        fadeAnimation = 0.0f;
        alertAnimations.clear();
        SmartProfilerMod.LOGGER.debug("Overlay renderer cleanup complete");
    }
}