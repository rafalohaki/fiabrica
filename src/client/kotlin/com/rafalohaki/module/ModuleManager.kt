package com.rafalohaki.module

import com.rafalohaki.module.modules.FlyModule
import com.rafalohaki.module.modules.KillauraModule

/**
 * Manages all hack modules.
 * Singleton pattern for global access.
 */
object ModuleManager {
    private val modules = mutableListOf<Module>()
    
    init {
        // Register all modules here
        register(FlyModule())
        register(KillauraModule())
        // Add more modules:
        // register(EspModule())
        // register(ScaffoldModule())
    }
    
    /**
     * Register a module.
     */
    private fun register(module: Module) {
        modules.add(module)
    }
    
    /**
     * Get all modules.
     */
    fun getModules(): List<Module> = modules
    
    /**
     * Get modules by category.
     */
    fun getModulesByCategory(category: Category): List<Module> {
        return modules.filter { it.category == category }
    }
    
    /**
     * Get module by name.
     */
    fun getModule(name: String): Module? {
        return modules.find { it.name.equals(name, ignoreCase = true) }
    }
    
    /**
     * Toggle module by name.
     */
    fun toggle(name: String) {
        getModule(name)?.toggle()
    }
}