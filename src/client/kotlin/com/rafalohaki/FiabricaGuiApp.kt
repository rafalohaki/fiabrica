package com.rafalohaki

import com.igrium.craftui.app.CraftApp
import com.rafalohaki.module.Category
import com.rafalohaki.module.ModuleManager
import com.rafalohaki.module.modules.FlyModule
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.MinecraftClient

class FiabricaGuiApp : CraftApp() {
    
    override fun render(client: MinecraftClient) {
        val flags = ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.AlwaysAutoResize
        
        if (ImGui.begin("Fiabrica Hacked Client", flags)) {
            ImGui.text("Kotlin + Event System + CraftUI")
            ImGui.separator()
            
            // Movement category
            if (ImGui.collapsingHeader("Movement")) {
                renderCategory(Category.MOVEMENT)
            }
            
            // Combat category
            if (ImGui.collapsingHeader("Combat")) {
                renderCategory(Category.COMBAT)
            }
            
            // Render category
            if (ImGui.collapsingHeader("Render")) {
                renderCategory(Category.RENDER)
            }
            
            ImGui.separator()
            if (ImGui.button("Close")) {
                close()
            }
            
            ImGui.end()
        }
    }
    
    private fun renderCategory(category: Category) {
        val modules = ModuleManager.getModulesByCategory(category)
        
        if (modules.isEmpty()) {
            ImGui.textDisabled("No modules")
            return
        }
        
        modules.forEach { module ->
            val enabled = booleanArrayOf(module.enabled)
            if (ImGui.checkbox(module.name, enabled)) {
                module.toggle()
            }
            
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip(module.description)
            }
            
            // Module-specific settings
            if (module is FlyModule && module.enabled) {
                ImGui.indent()
                val speed = floatArrayOf(module.speed)
                if (ImGui.sliderFloat("##flySpeed", speed, 0.1f, 5.0f, "Speed: %.1f")) {
                    module.setSpeed(speed[0])
                }
                ImGui.unindent()
            }
        }
    }
}