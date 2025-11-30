package com.rafalohaki.module;

import com.rafalohaki.module.modules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all hack modules.
 * Singleton pattern for global access.
 */
public class ModuleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("fiabrica");
    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules;
    
    private ModuleManager() {
        this.modules = new ArrayList<>();
        
        // Combat modules
        register(new KillauraModule());
        
        // Movement modules
        register(new FlyModule());
        register(new SpeedModule());
        register(new AutoSprintModule());
        
        // Player modules
        register(new NoFallModule());
        
        // Render modules
        register(new FullbrightModule());
        
        LOGGER.info("Registered {} modules", modules.size());
    }
    
    public static ModuleManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a module.
     */
    private void register(Module module) {
        modules.add(module);
    }
    
    /**
     * Get all modules.
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }
    
    /**
     * Get modules by category.
     */
    public List<Module> getModulesByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }
    
    /**
     * Get module by name.
     */
    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
    
    /**
     * Toggle module by name.
     */
    public void toggle(String name) {
        Module module = getModule(name);
        if (module != null) {
            module.toggle();
        }
    }
}