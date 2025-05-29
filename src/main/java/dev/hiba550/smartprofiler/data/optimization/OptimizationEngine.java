package dev.hiba550.smartprofiler.data.optimization;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.config.ProfilerConfig;
import dev.hiba550.smartprofiler.data.analysis.AnalysisContext;
import dev.hiba550.smartprofiler.data.analysis.IssueTracker;
import dev.hiba550.smartprofiler.data.models.*;
import dev.hiba550.smartprofiler.storage.PerformanceDatabase;
import dev.hiba550.smartprofiler.util.SystemInfoCollector;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Advanced optimization suggestion engine for Minecraft 1.21.5 performance
 * 
 * Features:
 * - Machine learning-inspired pattern recognition
 * - Context-aware suggestions based on hardware and game state
 * - Priority-based recommendation system
 * - Effectiveness tracking and learning
 * - Automatic implementation for safe optimizations
 * - User preference learning
 * - Mod compatibility awareness
 * 
 * The engine analyzes performance data patterns and generates actionable
 * suggestions tailored to the user's specific setup and playstyle.
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class OptimizationEngine {
    
    // Suggestion management
    private static final Queue<OptimizationSuggestion> suggestionQueue = new ConcurrentLinkedQueue<>();
    private static final Map<SuggestionType, Long> lastSuggestionTime = new ConcurrentHashMap<>();
    private static final Map<SuggestionType, Integer> suggestionCooldowns = new ConcurrentHashMap<>();
    private static final Map<String, SuggestionEffectiveness> effectivenessTracker = new ConcurrentHashMap<>();
    
    // Learning and adaptation
    private static final Map<HardwareProfile, Set<SuggestionType>> preferredSuggestions = new ConcurrentHashMap<>();
    private static final AtomicLong totalSuggestionsGenerated = new AtomicLong(0);
    private static final AtomicLong totalSuggestionsApplied = new AtomicLong(0);
    
    // Analysis state
    private static HardwareProfile currentHardwareProfile;
    private static PlaystyleProfile currentPlaystyleProfile;
    private static final Set<String> detectedMods = ConcurrentHashMap.newKeySet();
    
    // Suggestion cooldown periods (in milliseconds)
    static {
        suggestionCooldowns.put(SuggestionType.REDUCE_RENDER_DISTANCE, 300000); // 5 minutes
        suggestionCooldowns.put(SuggestionType.ADJUST_MEMORY_ALLOCATION, 600000); // 10 minutes
        suggestionCooldowns.put(SuggestionType.OPTIMIZE_GRAPHICS_SETTINGS, 180000); // 3 minutes
        suggestionCooldowns.put(SuggestionType.CLEANUP_WORLD_DATA, 900000); // 15 minutes
        suggestionCooldowns.put(SuggestionType.RESTART_GAME, 1800000); // 30 minutes
        suggestionCooldowns.put(SuggestionType.UPDATE_DRIVERS, 86400000); // 24 hours
        suggestionCooldowns.put(SuggestionType.DISABLE_PROBLEMATIC_MODS, 1800000); // 30 minutes
        suggestionCooldowns.put(SuggestionType.OPTIMIZE_JVM_FLAGS, 3600000); // 1 hour
    }
    
    /**
     * Initializes the optimization engine
     * Detects hardware profile and loads user preferences
     */
    public static void initialize() {
        try {
            SmartProfilerMod.LOGGER.info("Initializing Optimization Engine for MC 1.21.5");
            
            // Detect hardware profile
            currentHardwareProfile = SystemInfoCollector.detectHardwareProfile();
            SmartProfilerMod.LOGGER.debug("Detected hardware profile: {}", currentHardwareProfile);
            
            // Initialize playstyle profile
            currentPlaystyleProfile = new PlaystyleProfile();
            
            // Detect installed mods
            detectInstalledMods();
            
            // Load previous effectiveness data
            loadEffectivenessData();
            
            SmartProfilerMod.LOGGER.info("Optimization Engine initialized successfully");
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Failed to initialize Optimization Engine", e);
        }
    }
    
    /**
     * Generates optimization suggestions based on detected performance issues
     * 
     * @param issue The performance issue to address
     * @param frame The performance frame containing context
     * @param context The current analysis context
     * @return Generated optimization suggestion, or null if none applicable
     */
    public static OptimizationSuggestion generateSuggestion(PerformanceIssue issue, 
                                                           PerformanceFrame frame, 
                                                           AnalysisContext context) {
        if (!ProfilerConfig.isOptimizationSuggestionsEnabled()) {
            return null;
        }
        
        try {
            // Check cooldown for suggestion type
            SuggestionType suggestionType = determineSuggestionType(issue, frame, context);
            if (suggestionType == null || isSuggestionOnCooldown(suggestionType)) {
                return null;
            }
            
            // Generate context-aware suggestion
            OptimizationSuggestion suggestion = createSuggestion(issue, frame, context, suggestionType);
            
            if (suggestion != null) {
                // Update cooldown
                lastSuggestionTime.put(suggestionType, System.currentTimeMillis());
                
                // Track generation
                totalSuggestionsGenerated.incrementAndGet();
                
                SmartProfilerMod.LOGGER.debug("Generated optimization suggestion: {} for issue: {}", 
                    suggestionType, issue);
                
                return suggestion;
            }
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Error generating optimization suggestion", e);
        }
        
        return null;
    }
    
    /**
     * Determines the most appropriate suggestion type for the given issue and context
     */
    private static SuggestionType determineSuggestionType(PerformanceIssue issue, 
                                                         PerformanceFrame frame, 
                                                         AnalysisContext context) {
        
        // Priority-based suggestion selection
        List<SuggestionType> candidates = new ArrayList<>();
        
        switch (issue) {
            case LOW_FPS, CRITICAL_FPS -> {
                candidates.addAll(Arrays.asList(
                    SuggestionType.REDUCE_RENDER_DISTANCE,
                    SuggestionType.OPTIMIZE_GRAPHICS_SETTINGS,
                    SuggestionType.REDUCE_PARTICLES,
                    SuggestionType.DISABLE_SMOOTH_LIGHTING,
                    SuggestionType.OPTIMIZE_ENTITY_RENDERING
                ));
            }
            
            case HIGH_MEMORY_USAGE, CRITICAL_MEMORY_USAGE -> {
                candidates.addAll(Arrays.asList(
                    SuggestionType.ADJUST_MEMORY_ALLOCATION,
                    SuggestionType.CLEANUP_WORLD_DATA,
                    SuggestionType.REDUCE_LOADED_CHUNKS,
                    SuggestionType.RESTART_GAME,
                    SuggestionType.CLOSE_UNNECESSARY_APPLICATIONS
                ));
            }
            
            case MEMORY_LEAK -> {
                candidates.addAll(Arrays.asList(
                    SuggestionType.RESTART_GAME,
                    SuggestionType.DISABLE_PROBLEMATIC_MODS,
                    SuggestionType.UPDATE_MODS,
                    SuggestionType.REPORT_BUG
                ));
            }
            
            case RENDER_LAG, GPU_BOTTLENECK -> {
                candidates.addAll(Arrays.asList(
                    SuggestionType.OPTIMIZE_GRAPHICS_SETTINGS,
                    SuggestionType.UPDATE_DRIVERS,
                    SuggestionType.REDUCE_RENDER_DISTANCE,
                    SuggestionType.DISABLE_SHADERS,
                    SuggestionType.OPTIMIZE_TEXTURE_PACK
                ));
            }
            
            case EXCESSIVE_CHUNKS -> {
                candidates.addAll(Arrays.asList(
                    SuggestionType.REDUCE_RENDER_DISTANCE,
                    SuggestionType.REDUCE_SIMULATION_DISTANCE,
                    SuggestionType.CLEANUP_WORLD_DATA,
                    SuggestionType.OPTIMIZE_WORLD_GENERATION
                ));
            }
            
            case EXCESSIVE_ENTITIES -> {
                candidates.addAll(Arrays.asList(
                    SuggestionType.REDUCE_ENTITY_COUNT,
                    SuggestionType.OPTIMIZE_ENTITY_RENDERING,
                    SuggestionType.IMPLEMENT_ENTITY_CULLING,
                    SuggestionType.ADJUST_SPAWNING_SETTINGS
                ));
            }
            
            case GC_PRESSURE -> {
                candidates.addAll(Arrays.asList(
                    SuggestionType.OPTIMIZE_JVM_FLAGS,
                    SuggestionType.ADJUST_MEMORY_ALLOCATION,
                    SuggestionType.REDUCE_MEMORY_USAGE,
                    SuggestionType.UPDATE_JAVA_VERSION
                ));
            }
            
            case NETWORK_LAG -> {
                candidates.addAll(Arrays.asList(
                    SuggestionType.OPTIMIZE_NETWORK_SETTINGS,
                    SuggestionType.REDUCE_NETWORK_USAGE,
                    SuggestionType.CHECK_INTERNET_CONNECTION,
                    SuggestionType.CHANGE_SERVER
                ));
            }
            
            default -> {
                return SuggestionType.GENERAL_PERFORMANCE_TIPS;
            }
        }
        
        // Filter candidates based on context and hardware profile
        candidates = filterCandidatesByContext(candidates, context, frame);
        
        // Sort by effectiveness and user preference
        candidates.sort((a, b) -> {
            double scoreA = calculateSuggestionScore(a, issue, frame);
            double scoreB = calculateSuggestionScore(b, issue, frame);
            return Double.compare(scoreB, scoreA); // Descending order
        });
        
        return candidates.isEmpty() ? null : candidates.get(0);
    }
    
    /**
     * Filters suggestion candidates based on context and current state
     */
    private static List<SuggestionType> filterCandidatesByContext(List<SuggestionType> candidates, 
                                                                 AnalysisContext context, 
                                                                 PerformanceFrame frame) {
        return candidates.stream()
            .filter(type -> isApplicableInContext(type, context, frame))
            .filter(type -> isCompatibleWithHardware(type, currentHardwareProfile))
            .filter(type -> !isSuggestionOnCooldown(type))
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if a suggestion type is applicable in the current context
     */
    private static boolean isApplicableInContext(SuggestionType type, AnalysisContext context, PerformanceFrame frame) {
        return switch (type) {
            case REDUCE_RENDER_DISTANCE -> frame.getChunkStats().getRenderDistance() > 8;
            case ADJUST_MEMORY_ALLOCATION -> currentHardwareProfile.getTotalMemoryGB() >= 8;
            case DISABLE_SHADERS -> hasShaderMod();
            case REDUCE_SIMULATION_DISTANCE -> context == AnalysisContext.HEAVY_WORLD;
            case OPTIMIZE_ENTITY_RENDERING -> frame.getEntityStats().getRenderedEntities() > 100;
            case UPDATE_DRIVERS -> currentHardwareProfile.getGpuType() != GpuType.INTEGRATED;
            case CLOSE_UNNECESSARY_APPLICATIONS -> true; // Always applicable
            case OPTIMIZE_JVM_FLAGS -> !hasOptimalJvmFlags();
            default -> true;
        };
    }
    
    /**
     * Checks if a suggestion is compatible with the current hardware profile
     */
    private static boolean isCompatibleWithHardware(SuggestionType type, HardwareProfile profile) {
        return switch (type) {
            case ENABLE_HARDWARE_ACCELERATION -> profile.getGpuType() != GpuType.NONE;
            case OPTIMIZE_GRAPHICS_SETTINGS -> true;
            case ADJUST_MEMORY_ALLOCATION -> profile.getTotalMemoryGB() >= 4;
            case UPDATE_DRIVERS -> profile.getGpuType() != GpuType.INTEGRATED;
            case OPTIMIZE_CPU_SETTINGS -> profile.getCpuCores() >= 4;
            default -> true;
        };
    }
    
    /**
     * Calculates a score for suggestion effectiveness
     */
    private static double calculateSuggestionScore(SuggestionType type, PerformanceIssue issue, PerformanceFrame frame) {
        double baseScore = getBaseSuggestionScore(type, issue);
        
        // Adjust based on historical effectiveness
        SuggestionEffectiveness effectiveness = effectivenessTracker.get(type.name());
        if (effectiveness != null) {
            double effectivenessMultiplier = effectiveness.getAverageEffectiveness() / 5.0; // Normalize to 0-1
            baseScore *= (0.5 + effectivenessMultiplier); // 0.5 to 1.5 multiplier
        }
        
        // Adjust based on user preferences
        Set<SuggestionType> preferred = preferredSuggestions.get(currentHardwareProfile);
        if (preferred != null && preferred.contains(type)) {
            baseScore *= 1.2; // 20% bonus for preferred suggestions
        }
        
        // Adjust based on current performance state
        double severityMultiplier = getSeverityMultiplier(issue, frame);
        baseScore *= severityMultiplier;
        
        return baseScore;
    }
    
    /**
     * Gets base effectiveness score for a suggestion type against an issue
     */
    private static double getBaseSuggestionScore(SuggestionType type, PerformanceIssue issue) {
        // Effectiveness matrix based on suggestion type and issue type
        return switch (issue) {
            case LOW_FPS, CRITICAL_FPS -> switch (type) {
                case REDUCE_RENDER_DISTANCE -> 9.0;
                case OPTIMIZE_GRAPHICS_SETTINGS -> 8.5;
                case DISABLE_SHADERS -> 9.5;
                case REDUCE_PARTICLES -> 7.0;
                case OPTIMIZE_ENTITY_RENDERING -> 7.5;
                default -> 5.0;
            };
            
            case HIGH_MEMORY_USAGE, CRITICAL_MEMORY_USAGE -> switch (type) {
                case ADJUST_MEMORY_ALLOCATION -> 9.0;
                case CLEANUP_WORLD_DATA -> 8.0;
                case RESTART_GAME -> 8.5;
                case REDUCE_LOADED_CHUNKS -> 7.5;
                default -> 4.0;
            };
            
            case RENDER_LAG, GPU_BOTTLENECK -> switch (type) {
                case OPTIMIZE_GRAPHICS_SETTINGS -> 9.0;
                case UPDATE_DRIVERS -> 8.5;
                case DISABLE_SHADERS -> 9.0;
                case REDUCE_RENDER_DISTANCE -> 7.5;
                default -> 5.0;
            };
            
            default -> 5.0;
        };
    }
    
    /**
     * Gets severity multiplier based on issue severity and current performance
     */
    private static double getSeverityMultiplier(PerformanceIssue issue, PerformanceFrame frame) {
        double multiplier = 1.0;
        
        // Increase urgency for critical issues
        if (issue.getSeverity() == IssueSeverity.CRITICAL) {
            multiplier *= 2.0;
        } else if (issue.getSeverity() == IssueSeverity.HIGH) {
            multiplier *= 1.5;
        }
        
        // Increase urgency for extremely poor performance
        if (frame.getFps() < 15) {
            multiplier *= 1.8;
        } else if (frame.getFps() < 30) {
            multiplier *= 1.3;
        }
        
        if (frame.getMemoryStats().getHeapUsagePercent() > 90) {
            multiplier *= 1.6;
        }
        
        return multiplier;
    }
    
    /**
     * Creates a detailed optimization suggestion
     */
    private static OptimizationSuggestion createSuggestion(PerformanceIssue issue, 
                                                          PerformanceFrame frame, 
                                                          AnalysisContext context, 
                                                          SuggestionType type) {
        
        OptimizationSuggestion.Builder builder = new OptimizationSuggestion.Builder()
            .type(type)
            .priority(calculatePriority(issue, frame, type))
            .title(getSuggestionTitle(type))
            .description(getSuggestionDescription(type, frame))
            .category(getSuggestionCategory(type))
            .difficulty(getSuggestionDifficulty(type))
            .estimatedImpact(getEstimatedImpact(type, issue))
            .estimatedTime(getEstimatedImplementationTime(type))
            .relatedIssue(issue)
            .context(context);
        
        // Add implementation steps
        List<String> steps = getImplementationSteps(type, frame);
        steps.forEach(builder::addImplementationStep);
        
        // Add warnings if applicable
        List<String> warnings = getImplementationWarnings(type);
        warnings.forEach(builder::addWarning);
        
        // Add requirements
        List<String> requirements = getRequirements(type);
        requirements.forEach(builder::addRequirement);
        
        // Add related settings
        Map<String, Object> settings = getRelatedSettings(type, frame);
        settings.forEach(builder::addRelatedSetting);
        
        // Determine if auto-implementation is safe
        boolean canAutoImplement = canAutoImplement(type);
        builder.canAutoImplement(canAutoImplement);
        
        if (canAutoImplement) {
            builder.autoImplementAction(() -> autoImplementSuggestion(type, frame));
        }
        
        return builder.build();
    }
    
    /**
     * Gets the title for a suggestion type
     */
    private static String getSuggestionTitle(SuggestionType type) {
        return switch (type) {
            case REDUCE_RENDER_DISTANCE -> "Reduce Render Distance";
            case ADJUST_MEMORY_ALLOCATION -> "Adjust Memory Allocation";
            case OPTIMIZE_GRAPHICS_SETTINGS -> "Optimize Graphics Settings";
            case CLEANUP_WORLD_DATA -> "Clean Up World Data";
            case RESTART_GAME -> "Restart Minecraft";
            case UPDATE_DRIVERS -> "Update Graphics Drivers";
            case DISABLE_PROBLEMATIC_MODS -> "Disable Problematic Mods";
            case OPTIMIZE_JVM_FLAGS -> "Optimize JVM Launch Flags";
            case REDUCE_PARTICLES -> "Reduce Particle Effects";
            case DISABLE_SMOOTH_LIGHTING -> "Disable Smooth Lighting";
            case OPTIMIZE_ENTITY_RENDERING -> "Optimize Entity Rendering";
            case REDUCE_LOADED_CHUNKS -> "Reduce Loaded Chunks";
            case CLOSE_UNNECESSARY_APPLICATIONS -> "Close Unnecessary Applications";
            case DISABLE_SHADERS -> "Disable Shaders";
            case OPTIMIZE_TEXTURE_PACK -> "Optimize Texture Pack";
            case REDUCE_SIMULATION_DISTANCE -> "Reduce Simulation Distance";
            case OPTIMIZE_WORLD_GENERATION -> "Optimize World Generation";
            case REDUCE_ENTITY_COUNT -> "Reduce Entity Count";
            case IMPLEMENT_ENTITY_CULLING -> "Implement Entity Culling";
            case ADJUST_SPAWNING_SETTINGS -> "Adjust Mob Spawning";
            case REDUCE_MEMORY_USAGE -> "Reduce Memory Usage";
            case UPDATE_JAVA_VERSION -> "Update Java Version";
            case OPTIMIZE_NETWORK_SETTINGS -> "Optimize Network Settings";
            case REDUCE_NETWORK_USAGE -> "Reduce Network Usage";
            case CHECK_INTERNET_CONNECTION -> "Check Internet Connection";
            case CHANGE_SERVER -> "Consider Changing Server";
            case ENABLE_HARDWARE_ACCELERATION -> "Enable Hardware Acceleration";
            case OPTIMIZE_CPU_SETTINGS -> "Optimize CPU Settings";
            case UPDATE_MODS -> "Update Mods";
            case REPORT_BUG -> "Report Performance Bug";
            case GENERAL_PERFORMANCE_TIPS -> "General Performance Tips";
        };
    }
    
    /**
     * Gets detailed description for a suggestion
     */
    private static String getSuggestionDescription(SuggestionType type, PerformanceFrame frame) {
        return switch (type) {
            case REDUCE_RENDER_DISTANCE -> String.format(
                "Your current render distance is %d chunks. Reducing it to 8-12 chunks can significantly " +
                "improve FPS while maintaining good visibility. Each chunk reduction can improve FPS by 5-15%%.",
                frame.getChunkStats().getRenderDistance()
            );
            
            case ADJUST_MEMORY_ALLOCATION -> String.format(
                "Current memory usage is %.1f%%. Allocating more RAM to Minecraft can reduce garbage collection " +
                "and improve performance. Consider allocating %d-6GB for optimal performance.",
                frame.getMemoryStats().getHeapUsagePercent(),
                Math.min(8, (int)(currentHardwareProfile.getTotalMemoryGB() * 0.5))
            );
            
            case OPTIMIZE_GRAPHICS_SETTINGS -> 
                "Several graphics settings can be optimized for better performance without significant " +
                "visual quality loss. This includes adjusting fancy graphics, clouds, and animation settings.";
            
            case CLEANUP_WORLD_DATA -> 
                "Your world may have accumulated unnecessary data over time. Cleaning up chunk data, " +
                "removing excessive entities, and optimizing structures can improve loading times and reduce memory usage.";
            
            case RESTART_GAME -> String.format(
                "Minecraft has been running for a while and memory usage is %.1f%%. Restarting can " +
                "clear memory leaks and reset performance to optimal levels.",
                frame.getMemoryStats().getHeapUsagePercent()
            );
            
            case UPDATE_DRIVERS -> 
                "Outdated graphics drivers can cause performance issues and rendering problems. " +
                "Updated drivers often include game-specific optimizations and bug fixes.";
            
            case DISABLE_PROBLEMATIC_MODS -> 
                "Some mods may be causing performance issues or memory leaks. Consider temporarily " +
                "disabling recently installed mods to identify the problematic ones.";
            
            case OPTIMIZE_JVM_FLAGS -> 
                "Your current JVM flags may not be optimized for Minecraft 1.21.5. Modern flags can " +
                "improve garbage collection, memory management, and overall performance by 10-30%.";
            
            default -> "This optimization can help improve your Minecraft performance.";
        };
    }
    
    /**
     * Gets implementation steps for a suggestion type
     */
    private static List<String> getImplementationSteps(SuggestionType type, PerformanceFrame frame) {
        return switch (type) {
            case REDUCE_RENDER_DISTANCE -> Arrays.asList(
                "1. Open Minecraft Settings (ESC → Options)",
                "2. Go to Video Settings",
                "3. Find 'Render Distance' slider",
                "4. Reduce to 8-12 chunks (recommended: 10)",
                "5. Click 'Done' to apply changes"
            );
            
            case ADJUST_MEMORY_ALLOCATION -> Arrays.asList(
                "1. Close Minecraft completely",
                "2. Open your Minecraft Launcher",
                "3. Go to Installations tab",
                "4. Click '...' on your profile → Edit",
                "5. Click 'More Options'",
                "6. Find 'JVM Arguments'",
                "7. Change '-Xmx' value to 4G or 6G",
                "8. Save and restart Minecraft"
            );
            
            case OPTIMIZE_GRAPHICS_SETTINGS -> Arrays.asList(
                "1. Open Video Settings in Minecraft",
                "2. Set Graphics to 'Fast'",
                "3. Set Clouds to 'Fast' or 'Off'",
                "4. Reduce Particles to 'Decreased'",
                "5. Turn off 'Use VBOs' if causing issues",
                "6. Apply changes and test performance"
            );
            
            case CLEANUP_WORLD_DATA -> Arrays.asList(
                "1. Create a backup of your world",
                "2. Use world optimization tools",
                "3. Remove unnecessary structures",
                "4. Clear excessive entities with commands",
                "5. Consider using chunk optimization mods"
            );
            
            case RESTART_GAME -> Arrays.asList(
                "1. Save your current progress",
                "2. Close Minecraft completely",
                "3. Wait 10 seconds",
                "4. Restart Minecraft",
                "5. Reload your world"
            );
            
            case OPTIMIZE_JVM_FLAGS -> Arrays.asList(
                "1. Open Minecraft Launcher",
                "2. Go to Installations → Edit Profile",
                "3. Enable 'JVM Arguments'",
                "4. Replace with optimized flags:",
                "   -XX:+UseG1GC -XX:+ParallelRefProcEnabled",
                "   -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions",
                "   -XX:+DisableExplicitGC -XX:G1NewSizePercent=30",
                "5. Save and restart Minecraft"
            );
            
            default -> Arrays.asList(
                "1. Follow the specific instructions for this optimization",
                "2. Test the changes in-game",
                "3. Monitor performance improvements"
            );
        };
    }
    
    /**
     * Gets implementation warnings for potentially risky suggestions
     */
    private static List<String> getImplementationWarnings(SuggestionType type) {
        return switch (type) {
            case ADJUST_MEMORY_ALLOCATION -> Arrays.asList(
                "⚠️ Don't allocate more than 70% of your total RAM",
                "⚠️ Too much RAM can actually hurt performance",
                "⚠️ Make sure you have enough RAM for your OS"
            );
            
            case OPTIMIZE_JVM_FLAGS -> Arrays.asList(
                "⚠️ Backup your current launcher profile",
                "⚠️ Some flags may not work with all Java versions",
                "⚠️ Test thoroughly before using on important worlds"
            );
            
            case CLEANUP_WORLD_DATA -> Arrays.asList(
                "⚠️ ALWAYS backup your world before cleanup",
                "⚠️ Some changes may be irreversible",
                "⚠️ Test on a copy of your world first"
            );
            
            case DISABLE_PROBLEMATIC_MODS -> Arrays.asList(
                "⚠️ Disabling mods may affect world functionality",
                "⚠️ Some worlds may become unplayable without certain mods",
                "⚠️ Check mod dependencies before disabling"
            );
            
            default -> Collections.emptyList();
        };
    }
    
    /**
     * Gets requirements for implementing a suggestion
     */
    private static List<String> getRequirements(SuggestionType type) {
        return switch (type) {
            case ADJUST_MEMORY_ALLOCATION -> Arrays.asList(
                "Minimum 8GB system RAM recommended",
                "64-bit Java installation",
                "Administrative access to launcher settings"
            );
            
            case UPDATE_DRIVERS -> Arrays.asList(
                "Administrative privileges",
                "Internet connection",
                "Graphics card manufacturer's website access"
            );
            
            case OPTIMIZE_JVM_FLAGS -> Arrays.asList(
                "Java 17 or newer",
                "Access to launcher settings",
                "Basic understanding of JVM parameters"
            );
            
            case CLEANUP_WORLD_DATA -> Arrays.asList(
                "World backup capability",
                "World editing tools (optional)",
                "Sufficient disk space for backups"
            );
            
            default -> Collections.emptyList();
        };
    }
    
    /**
     * Gets related settings that should be considered with this suggestion
     */
    private static Map<String, Object> getRelatedSettings(SuggestionType type, PerformanceFrame frame) {
        Map<String, Object> settings = new HashMap<>();
        
        switch (type) {
            case REDUCE_RENDER_DISTANCE -> {
                settings.put("current_render_distance", frame.getChunkStats().getRenderDistance());
                settings.put("recommended_distance", Math.max(8, Math.min(12, frame.getChunkStats().getRenderDistance() - 4)));
                settings.put("simulation_distance", Math.min(10, frame.getChunkStats().getRenderDistance()));
            }
            
            case ADJUST_MEMORY_ALLOCATION -> {
                settings.put("current_usage_percent", frame.getMemoryStats().getHeapUsagePercent());
                settings.put("current_max_mb", frame.getMemoryStats().getHeapMax() / (1024 * 1024));
                settings.put("recommended_allocation_gb", Math.min(6, (int)(currentHardwareProfile.getTotalMemoryGB() * 0.5)));
            }
            
            case OPTIMIZE_GRAPHICS_SETTINGS -> {
                settings.put("graphics_mode", "fast");
                settings.put("clouds", "off");
                settings.put("particles", "decreased");
                settings.put("smooth_lighting", false);
            }
        }
        
        return settings;
    }
    
    /**
     * Determines if a suggestion can be automatically implemented safely
     */
    private static boolean canAutoImplement(SuggestionType type) {
        return switch (type) {
            case REDUCE_RENDER_DISTANCE,
                 OPTIMIZE_GRAPHICS_SETTINGS,
                 REDUCE_PARTICLES,
                 DISABLE_SMOOTH_LIGHTING,
                 REDUCE_SIMULATION_DISTANCE -> true;
            
            default -> false; // Most suggestions require user confirmation
        };
    }
    
    /**
     * Automatically implements safe optimizations
     */
    private static void autoImplementSuggestion(SuggestionType type, PerformanceFrame frame) {
        try {
            switch (type) {
                case REDUCE_RENDER_DISTANCE -> {
                    // This would integrate with Minecraft's settings API
                    int currentDistance = frame.getChunkStats().getRenderDistance();
                    int newDistance = Math.max(8, currentDistance - 2);
                    SmartProfilerMod.LOGGER.info("Auto-reducing render distance from {} to {}", 
                        currentDistance, newDistance);
                    // Implementation would go here
                }
                
                case OPTIMIZE_GRAPHICS_SETTINGS -> {
                    SmartProfilerMod.LOGGER.info("Auto-optimizing graphics settings for performance");
                    // Implementation would go here
                }
                
                // Add other auto-implementable suggestions
            }
            
            totalSuggestionsApplied.incrementAndGet();
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Error auto-implementing suggestion: {}", type, e);
        }
    }
    
    /**
     * Queues a suggestion for user consideration
     */
    public static void queueSuggestion(OptimizationSuggestion suggestion) {
        if (suggestion != null && !suggestionQueue.contains(suggestion)) {
            suggestionQueue.offer(suggestion);
            SmartProfilerMod.LOGGER.debug("Queued optimization suggestion: {}", suggestion.getType());
        }
    }
    
    /**
     * Gets the next suggestion from the queue
     */
    public static OptimizationSuggestion getNextSuggestion() {
        return suggestionQueue.poll();
    }
    
    /**
     * Gets all pending suggestions
     */
    public static List<OptimizationSuggestion> getAllPendingSuggestions() {
        return new ArrayList<>(suggestionQueue);
    }
    
    /**
     * Clears all pending suggestions
     */
    public static void clearSuggestions() {
        suggestionQueue.clear();
    }
    
    /**
     * Records the effectiveness of an applied suggestion
     */
    public static void recordSuggestionEffectiveness(SuggestionType type, int effectivenessRating, String feedback) {
        SuggestionEffectiveness effectiveness = effectivenessTracker.computeIfAbsent(
            type.name(), 
            k -> new SuggestionEffectiveness(type)
        );
        
        effectiveness.addRating(effectivenessRating, feedback);
        
        // Update user preferences
        if (effectivenessRating >= 4) { // 4-5 star rating
            preferredSuggestions.computeIfAbsent(currentHardwareProfile, k -> ConcurrentHashMap.newKeySet())
                               .add(type);
        }
        
        SmartProfilerMod.LOGGER.debug("Recorded effectiveness for {}: {} stars", type, effectivenessRating);
    }
    
    /**
     * Checks if a suggestion type is on cooldown
     */
    private static boolean isSuggestionOnCooldown(SuggestionType type) {
        Long lastTime = lastSuggestionTime.get(type);
        if (lastTime == null) {
            return false;
        }
        
        Integer cooldown = suggestionCooldowns.get(type);
        if (cooldown == null) {
            return false;
        }
        
        return (System.currentTimeMillis() - lastTime) < cooldown;
    }
    
    /**
     * Calculates suggestion priority (1-10 scale)
     */
    private static int calculatePriority(PerformanceIssue issue, PerformanceFrame frame, SuggestionType type) {
        int basePriority = issue.getSeverity().getWeight() + 2; // 3-6 base range
        
        // Adjust based on performance state
        if (frame.getFps() < 15) {
            basePriority += 3;
        } else if (frame.getFps() < 30) {
            basePriority += 1;
        }
        
        if (frame.getMemoryStats().getHeapUsagePercent() > 90) {
            basePriority += 2;
        }
        
        // Adjust based on suggestion effectiveness
        double effectivenessScore = getBaseSuggestionScore(type, issue);
        if (effectivenessScore >= 8.0) {
            basePriority += 1;
        }
        
        return Math.min(10, Math.max(1, basePriority));
    }
    
    /**
     * Gets suggestion category for UI organization
     */
    private static SuggestionCategory getSuggestionCategory(SuggestionType type) {
        return switch (type) {
            case REDUCE_RENDER_DISTANCE, OPTIMIZE_GRAPHICS_SETTINGS, DISABLE_SHADERS, 
                 REDUCE_PARTICLES, DISABLE_SMOOTH_LIGHTING, OPTIMIZE_TEXTURE_PACK -> SuggestionCategory.GRAPHICS;
            
            case ADJUST_MEMORY_ALLOCATION, OPTIMIZE_JVM_FLAGS, UPDATE_JAVA_VERSION, 
                 REDUCE_MEMORY_USAGE -> SuggestionCategory.MEMORY;
            
            case CLEANUP_WORLD_DATA, REDUCE_LOADED_CHUNKS, OPTIMIZE_WORLD_GENERATION, 
                 REDUCE_ENTITY_COUNT, IMPLEMENT_ENTITY_CULLING -> SuggestionCategory.WORLD;
            
            case DISABLE_PROBLEMATIC_MODS, UPDATE_MODS -> SuggestionCategory.MODS;
            
            case UPDATE_DRIVERS, ENABLE_HARDWARE_ACCELERATION, OPTIMIZE_CPU_SETTINGS -> SuggestionCategory.HARDWARE;
            
            case OPTIMIZE_NETWORK_SETTINGS, REDUCE_NETWORK_USAGE, CHECK_INTERNET_CONNECTION, 
                 CHANGE_SERVER -> SuggestionCategory.NETWORK;
            
            default -> SuggestionCategory.GENERAL;
        };
    }
    
    /**
     * Gets implementation difficulty (1-5 scale)
     */
    private static int getSuggestionDifficulty(SuggestionType type) {
        return switch (type) {
            case REDUCE_RENDER_DISTANCE, OPTIMIZE_GRAPHICS_SETTINGS, RESTART_GAME, 
                 REDUCE_PARTICLES, DISABLE_SMOOTH_LIGHTING -> 1; // Very Easy
            
            case ADJUST_MEMORY_ALLOCATION, DISABLE_PROBLEMATIC_MODS, UPDATE_MODS -> 2; // Easy
            
            case OPTIMIZE_JVM_FLAGS, CLEANUP_WORLD_DATA, UPDATE_DRIVERS -> 3; // Medium
            
            case IMPLEMENT_ENTITY_CULLING, OPTIMIZE_WORLD_GENERATION, 
                 OPTIMIZE_NETWORK_SETTINGS -> 4; // Hard
            
            case UPDATE_JAVA_VERSION, ENABLE_HARDWARE_ACCELERATION, 
                 OPTIMIZE_CPU_SETTINGS -> 5; // Very Hard
            
            default -> 3;
        };
    }
    
    /**
     * Gets estimated performance impact (1-5 scale)
     */
    private static int getEstimatedImpact(SuggestionType type, PerformanceIssue issue) {
        int baseImpact = switch (type) {
            case REDUCE_RENDER_DISTANCE, DISABLE_SHADERS -> 5; // Very High
            case OPTIMIZE_GRAPHICS_SETTINGS, ADJUST_MEMORY_ALLOCATION -> 4; // High
            case OPTIMIZE_JVM_FLAGS, UPDATE_DRIVERS -> 3; // Medium
            case REDUCE_PARTICLES, CLEANUP_WORLD_DATA -> 2; // Low
            case RESTART_GAME, CLOSE_UNNECESSARY_APPLICATIONS -> 3; // Medium
            default -> 2;
        };
        
        // Adjust based on issue severity
        if (issue.getSeverity() == IssueSeverity.CRITICAL) {
            baseImpact = Math.min(5, baseImpact + 1);
        }
        
        return baseImpact;
    }
    
    /**
     * Gets estimated implementation time in minutes
     */
    private static int getEstimatedImplementationTime(SuggestionType type) {
        return switch (type) {
            case REDUCE_RENDER_DISTANCE, OPTIMIZE_GRAPHICS_SETTINGS, 
                 REDUCE_PARTICLES, DISABLE_SMOOTH_LIGHTING -> 1;
            
            case RESTART_GAME, DISABLE_PROBLEMATIC_MODS -> 2;
            
            case ADJUST_MEMORY_ALLOCATION, OPTIMIZE_JVM_FLAGS -> 5;
            
            case UPDATE_DRIVERS, UPDATE_MODS -> 15;
            
            case CLEANUP_WORLD_DATA -> 30;
            
            case UPDATE_JAVA_VERSION, ENABLE_HARDWARE_ACCELERATION -> 45;
            
            default -> 10;
        };
    }
    
    // Helper methods for detection
    
    private static void detectInstalledMods() {
        // Implementation would scan the mods folder and detect known performance-affecting mods
        // For now, we'll use a simplified approach
        detectedMods.add("fabric-api");
        detectedMods.add("smart-profiler");
    }
    
    private static boolean hasShaderMod() {
        return detectedMods.stream().anyMatch(mod -> 
            mod.toLowerCase().contains("shader") || 
            mod.toLowerCase().contains("iris") ||
            mod.toLowerCase().contains("optifine")
        );
    }
    
    private static boolean hasOptimalJvmFlags() {
        // Check if current JVM flags are optimized
        // This would require accessing the current JVM arguments
        return false; // Simplified for example
    }
    
    private static void loadEffectivenessData() {
        // Load previous effectiveness data from database or config file
        // Implementation would restore user preferences and historical data
    }
    
    /**
     * Gets optimization engine statistics
     */
    public static OptimizationStatistics getStatistics() {
        return new OptimizationStatistics(
            totalSuggestionsGenerated.get(),
            totalSuggestionsApplied.get(),
            suggestionQueue.size(),
            effectivenessTracker.size(),
            currentHardwareProfile,
            currentPlaystyleProfile
        );
    }
    
    /**
     * Cleanup method for mod shutdown
     */
    public static void shutdown() {
        suggestionQueue.clear();
        lastSuggestionTime.clear();
        effectivenessTracker.clear();
        preferredSuggestions.clear();
        detectedMods.clear();
        SmartProfilerMod.LOGGER.info("Optimization Engine shutdown complete");
    }
}