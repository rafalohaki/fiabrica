package com.rafalohaki

import com.igrium.craftui.app.AppManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object FiabricaClient : ClientModInitializer {
    
    private lateinit var openGuiKey: KeyBinding
    
    override fun onInitializeClient() {
        // Register keybind to open GUI (Right Shift by default)
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.fiabrica.openGui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.fiabrica"
            )
        )
        
        // Listen for key press to open GUI
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (openGuiKey.wasPressed()) {
                AppManager.openApp(FiabricaGuiApp())
            }
        }
    }
}