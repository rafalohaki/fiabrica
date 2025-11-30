package com.rafalohaki.module;

import com.rafalohaki.module.modules.FlyModule;
import com.rafalohaki.module.modules.KillauraModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all hack modules.
 * Singleton pattern for global access.
 */
public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules;
    
    private ModuleManager() {
        this.modules = new ArrayList<>();
        // Register all modules here
        register(new FlyModule());
        register(new KillauraModule());
        // Add more modules:
        // register(new EspModule());
        // register(new ScaffoldModule());
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