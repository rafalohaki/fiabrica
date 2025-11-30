package com.rafalohaki

import com.igrium.craftui.app.CraftApp
import com.rafalohaki.module.Category
import com.rafalohaki.module.ModuleManager
import com.rafalohaki.module.modules.FlyModule
import com.rafalohaki.module.modules.KillauraModule
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.MinecraftClient

class FiabricaGuiApp : CraftApp() {
    
    override fun render(client: MinecraftClient) {
        val flags = ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.AlwaysAutoResize
        
        if (ImGui.begin("Fiabrica - Grim Bypass Client", flags)) {
            ImGui.text("Humanized Modules | Silent Rotations | Event-Driven")
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
            ImGui.textDisabled("Made with love for bypassing Grim AC")
            if (ImGui.button("Close")) {
                close()
            }
            
            ImGui.end()
        }
    }
    
    private fun renderCategory(category: Category) {
        val modules = ModuleManager.getModulesByCategory(category)
        
        if (modules.isEmpty()) {
            ImGui.textDisabled("No modules in this category")
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
            when (module) {
                is FlyModule -> {
                    if (module.enabled) {
                        ImGui.indent()
                        val speed = floatArrayOf(module.speed)
                        if (ImGui.sliderFloat("##flySpeed", speed, 0.1f, 5.0f, "Speed: %.1f")) {
                            module.setSpeed(speed[0])
                        }
                        ImGui.unindent()
                    }
                }
                is KillauraModule -> {
                    if (module.enabled) {
                        ImGui.indent()
                        
                        val range = floatArrayOf(module.range)
                        if (ImGui.sliderFloat("##kaRange", range, 3.0f, 6.0f, "Range: %.1f")) {
                            module.setRange(range[0])
                        }
                        
                        val cps = floatArrayOf(module.cps)
                        if (ImGui.sliderFloat("##kaCps", cps, 8f, 20f, "CPS: %.1f")) {
                            module.setCps(cps[0])
                        }
                        
                        val rotSpeed = floatArrayOf(module.rotationSpeed)
                        if (ImGui.sliderFloat("##kaRotSpeed", rotSpeed, 5f, 30f, "Rotation: %.1f")) {
                            module.setRotationSpeed(rotSpeed[0])
                        }
                        
                        val playersOnly = booleanArrayOf(module.playersOnly)
                        if (ImGui.checkbox("Players Only##ka", playersOnly)) {
                            module.setPlayersOnly(playersOnly[0])
                        }
                        
                        val throughWalls = booleanArrayOf(module.throughWalls)
                        if (ImGui.checkbox("Through Walls##ka", throughWalls)) {
                            module.setThroughWalls(throughWalls[0])
                        }
                        
                        ImGui.unindent()
                    }
                }
            }
        }
    }
}