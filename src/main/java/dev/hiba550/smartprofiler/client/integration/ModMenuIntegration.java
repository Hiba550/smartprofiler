package dev.hiba550.smartprofiler.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.hiba550.smartprofiler.config.ProfilerConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * ModMenu integration for Smart Performance Profiler
 * 
 * Provides easy access to configuration screen through ModMenu interface.
 * Automatically generates a user-friendly GUI from the ProfilerConfig class.
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ProfilerConfig.class, parent).get();
    }
}