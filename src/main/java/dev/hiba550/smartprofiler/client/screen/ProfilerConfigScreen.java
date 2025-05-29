package dev.hiba550.smartprofiler.client.screen;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.config.ProfilerConfig;
import dev.hiba550.smartprofiler.client.overlay.OverlayMode;
import dev.hiba550.smartprofiler.client.overlay.OverlayPosition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom configuration screen for Smart Performance Profiler
 * 
 * Provides a more streamlined interface compared to the auto-generated ModMenu screen.
 * Focuses on the most commonly used settings with real-time preview.
 * 
 * Features:
 * - Live preview of overlay changes
 * - Quick access to common settings
 * - Performance impact indicators
 * - Reset to defaults functionality
 * - Import/Export configuration
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
@Environment(EnvType.CLIENT)
public class ProfilerConfigScreen extends Screen {
    
    private final Screen parent;
    private final ProfilerConfig config;
    
    // Widget lists for easy management
    private final List<ButtonWidget> buttons = new ArrayList<>();
    private final List<SliderWidget> sliders = new ArrayList<>();
    
    // Layout constants
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 25;
    private static final int LEFT_COLUMN = 0;
    private static final int RIGHT_COLUMN = 220;
    
    public ProfilerConfigScreen(Screen parent) {
        super(Text.literal("Smart Performance Profiler - Configuration"));
        this.parent = parent;
        this.config = ProfilerConfig.getInstance();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 40;
        int currentY = startY;
        
        // Title
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Smart Performance Profiler Configuration")
                .formatted(Formatting.BOLD, Formatting.GOLD),
            button -> {}
        ).dimensions(centerX - 150, 20, 300, 20).build());
        
        // === LEFT COLUMN - OVERLAY SETTINGS ===
        
        addSectionTitle("Overlay Settings", centerX - 200 + LEFT_COLUMN, currentY);
        currentY += 30;
        
        // Enable Overlay
        ButtonWidget overlayToggle = ButtonWidget.builder(
            getToggleText("Enable Overlay", config.overlayEnabled),
            button -> {
                config.overlayEnabled = !config.overlayEnabled;
                button.setMessage(getToggleText("Enable Overlay", config.overlayEnabled));
                ProfilerConfig.save();
            }
        ).dimensions(centerX - 200 + LEFT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(overlayToggle);
        buttons.add(overlayToggle);
        currentY += SPACING;
        
        // Overlay Mode
        CyclingButtonWidget<OverlayMode> overlayModeButton = CyclingButtonWidget.builder(
            (OverlayMode mode) -> Text.literal("Mode: " + mode.getDisplayName())
        ).values(OverlayMode.values())
         .initially(config.defaultOverlayMode)
         .build(centerX - 200 + LEFT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT, 
               Text.literal("Overlay Mode"), (button, mode) -> {
                   config.defaultOverlayMode = mode;
                   ProfilerConfig.save();
               });
        addDrawableChild(overlayModeButton);
        currentY += SPACING;
        
        // Overlay Position
        CyclingButtonWidget<OverlayPosition> overlayPositionButton = CyclingButtonWidget.builder(
            (OverlayPosition pos) -> Text.literal("Position: " + pos.getDisplayName())
        ).values(OverlayPosition.values())
         .initially(config.defaultOverlayPosition)
         .build(centerX - 200 + LEFT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
               Text.literal("Overlay Position"), (button, position) -> {
                   config.defaultOverlayPosition = position;
                   ProfilerConfig.save();
               });
        addDrawableChild(overlayPositionButton);
        currentY += SPACING;
        
        // Overlay Opacity Slider
        SliderWidget opacitySlider = new SliderWidget(
            centerX - 200 + LEFT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
            Text.literal("Opacity: " + config.overlayOpacity + "%"),
            config.overlayOpacity / 100.0
        ) {
            @Override
            protected void updateMessage() {
                int opacity = (int)(this.value * 100);
                this.setMessage(Text.literal("Opacity: " + opacity + "%"));
            }
            
            @Override
            protected void applyValue() {
                config.overlayOpacity = (int)(this.value * 100);
                ProfilerConfig.save();
            }
        };
        addDrawableChild(opacitySlider);
        sliders.add(opacitySlider);
        currentY += SPACING;
        
        // Show Graphs Toggle
        ButtonWidget graphsToggle = ButtonWidget.builder(
            getToggleText("Show Graphs", config.showGraphs),
            button -> {
                config.showGraphs = !config.showGraphs;
                button.setMessage(getToggleText("Show Graphs", config.showGraphs));
                ProfilerConfig.save();
            }
        ).dimensions(centerX - 200 + LEFT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(graphsToggle);
        buttons.add(graphsToggle);
        currentY += SPACING;
        
        // Show Warnings Toggle
        ButtonWidget warningsToggle = ButtonWidget.builder(
            getToggleText("Show Warnings", config.showWarnings),
            button -> {
                config.showWarnings = !config.showWarnings;
                button.setMessage(getToggleText("Show Warnings", config.showWarnings));
                ProfilerConfig.save();
            }
        ).dimensions(centerX - 200 + LEFT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(warningsToggle);
        buttons.add(warningsToggle);
        
        // === RIGHT COLUMN - ANALYSIS SETTINGS ===
        
        currentY = startY;
        addSectionTitle("Analysis Settings", centerX - 200 + RIGHT_COLUMN, currentY);
        currentY += 30;
        
        // Enable Real-time Monitoring
        ButtonWidget monitoringToggle = ButtonWidget.builder(
            getToggleText("Real-time Monitoring", config.realtimeMonitoringEnabled),
            button -> {
                config.realtimeMonitoringEnabled = !config.realtimeMonitoringEnabled;
                button.setMessage(getToggleText("Real-time Monitoring", config.realtimeMonitoringEnabled));
                ProfilerConfig.save();
            }
        ).dimensions(centerX - 200 + RIGHT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(monitoringToggle);
        buttons.add(monitoringToggle);
        currentY += SPACING;
        
        // Enable Bottleneck Analysis
        ButtonWidget analysisToggle = ButtonWidget.builder(
            getToggleText("Bottleneck Analysis", config.bottleneckAnalysisEnabled),
            button -> {
                config.bottleneckAnalysisEnabled = !config.bottleneckAnalysisEnabled;
                button.setMessage(getToggleText("Bottleneck Analysis", config.bottleneckAnalysisEnabled));
                ProfilerConfig.save();
            }
        ).dimensions(centerX - 200 + RIGHT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(analysisToggle);
        buttons.add(analysisToggle);
        currentY += SPACING;
        
        // Low FPS Threshold Slider
        SliderWidget fpsSlider = new SliderWidget(
            centerX - 200 + RIGHT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
            Text.literal("Low FPS Threshold: " + config.lowFpsThreshold),
            (config.lowFpsThreshold - 10) / 110.0 // 10-120 range
        ) {
            @Override
            protected void updateMessage() {
                int fps = (int)(this.value * 110) + 10;
                this.setMessage(Text.literal("Low FPS Threshold: " + fps));
            }
            
            @Override
            protected void applyValue() {
                config.lowFpsThreshold = (int)(this.value * 110) + 10;
                ProfilerConfig.save();
            }
        };
        addDrawableChild(fpsSlider);
        sliders.add(fpsSlider);
        currentY += SPACING;
        
        // Memory Threshold Slider
        SliderWidget memorySlider = new SliderWidget(
            centerX - 200 + RIGHT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT,
            Text.literal("Memory Threshold: " + config.highMemoryThreshold + "%"),
            (config.highMemoryThreshold - 50) / 49.0 // 50-99 range
        ) {
            @Override
            protected void updateMessage() {
                int memory = (int)(this.value * 49) + 50;
                this.setMessage(Text.literal("Memory Threshold: " + memory + "%"));
            }
            
            @Override
            protected void applyValue() {
                config.highMemoryThreshold = (int)(this.value * 49) + 50;
                ProfilerConfig.save();
            }
        };
        addDrawableChild(memorySlider);
        sliders.add(memorySlider);
        currentY += SPACING;
        
        // Enable Notifications
        ButtonWidget notificationsToggle = ButtonWidget.builder(
            getToggleText("Chat Notifications", config.chatNotificationsEnabled),
            button -> {
                config.chatNotificationsEnabled = !config.chatNotificationsEnabled;
                button.setMessage(getToggleText("Chat Notifications", config.chatNotificationsEnabled));
                ProfilerConfig.save();
            }
        ).dimensions(centerX - 200 + RIGHT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(notificationsToggle);
        buttons.add(notificationsToggle);
        currentY += SPACING;
        
        // Enable Database
        ButtonWidget databaseToggle = ButtonWidget.builder(
            getToggleText("Performance Database", config.databaseEnabled),
            button -> {
                config.databaseEnabled = !config.databaseEnabled;
                button.setMessage(getToggleText("Performance Database", config.databaseEnabled));
                ProfilerConfig.save();
            }
        ).dimensions(centerX - 200 + RIGHT_COLUMN, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(databaseToggle);
        buttons.add(databaseToggle);
        
        // === BOTTOM BUTTONS ===
        
        int bottomY = this.height - 60;
        
        // Advanced Settings Button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Advanced Settings..."),
            button -> {
                // Open the full auto-generated config screen
                if (this.client != null) {
                    this.client.setScreen(
                        me.shedaniel.autoconfig.AutoConfig.getConfigScreen(
                            ProfilerConfig.class, this
                        ).get()
                    );
                }
            }
        ).dimensions(centerX - 200, bottomY, 120, BUTTON_HEIGHT).build());
        
        // Reset to Defaults Button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Reset to Defaults").formatted(Formatting.RED),
            button -> {
                config.resetToDefaults();
                ProfilerConfig.save();
                // Refresh the screen
                this.clearAndInit();
                SmartProfilerMod.LOGGER.info("Configuration reset to defaults");
            }
        ).dimensions(centerX - 70, bottomY, 140, BUTTON_HEIGHT).build());
        
        // Done Button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(parent);
                }
            }
        ).dimensions(centerX + 80, bottomY, 80, BUTTON_HEIGHT).build());
    }
    
    /**
     * Adds a section title to the screen
     */
    private void addSectionTitle(String title, int x, int y) {
        // Section titles are drawn in the render method, just store the info
    }
    
    /**
     * Creates toggle button text based on boolean state
     */
    private Text getToggleText(String label, boolean enabled) {
        return Text.literal(label + ": ")
            .append(Text.literal(enabled ? "ON" : "OFF")
                .formatted(enabled ? Formatting.GREEN : Formatting.RED));
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Render section titles
        int centerX = this.width / 2;
        int startY = 40;
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Overlay Settings").formatted(Formatting.UNDERLINE),
            centerX - 200 + LEFT_COLUMN + BUTTON_WIDTH / 2, startY, 0xFFFFFF);
            
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("Analysis Settings").formatted(Formatting.UNDERLINE),
            centerX - 200 + RIGHT_COLUMN + BUTTON_WIDTH / 2, startY, 0xFFFFFF);
        
        // Render performance impact warning if high settings are enabled
        if (config.collectionFrequency > 50 || config.debugLoggingEnabled) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("⚠ High performance impact settings enabled")
                    .formatted(Formatting.YELLOW),
                centerX, this.height - 80, 0xFFFFFF);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        ProfilerConfig.save();
        super.close();
    }
    
    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game while configuring
    }
}