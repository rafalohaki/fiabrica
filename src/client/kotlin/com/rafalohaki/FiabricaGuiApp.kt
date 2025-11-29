package com.rafalohaki

import com.igrium.craftui.app.CraftApp
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.MinecraftClient

class FiabricaGuiApp : CraftApp() {
    
    private var flyEnabled = false
    private var flySpeed = floatArrayOf(1.0f)
    private var espColor = floatArrayOf(1.0f, 0.0f, 0.0f)
    
    override fun render(client: MinecraftClient) {
        // Main window flags
        val flags = ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.AlwaysAutoResize
        
        // Begin main GUI window
        if (ImGui.begin("Fiabrica Hacked Client", flags)) {
            ImGui.text("Kotlin + CraftUI + ImGui")
            ImGui.separator()
            
            // Movement hacks section
            if (ImGui.collapsingHeader("Movement")) {
                ImGui.checkbox("Fly Hack", booleanArrayOf(flyEnabled))
                ImGui.sliderFloat("Fly Speed", flySpeed, 0.1f, 5.0f)
                ImGui.text("Status: ${if (flyEnabled) "Active" else "Disabled"}")
            }
            
            // Visual hacks section
            if (ImGui.collapsingHeader("Visuals")) {
                ImGui.colorEdit3("ESP Color", espColor)
                ImGui.checkbox("Fullbright", booleanArrayOf(false))
                ImGui.checkbox("X-Ray", booleanArrayOf(false))
            }
            
            // Combat hacks section
            if (ImGui.collapsingHeader("Combat")) {
                ImGui.checkbox("Killaura", booleanArrayOf(false))
                ImGui.checkbox("Auto Totem", booleanArrayOf(false))
            }
            
            ImGui.separator()
            if (ImGui.button("Close")) {
                close()
            }
            
            ImGui.end()
        }
    }
}