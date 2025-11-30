package com.rafalohaki

import com.rafalohaki.event.ClientTickEvent
import com.rafalohaki.event.EventBus
import com.rafalohaki.gui.ClickGui
import com.rafalohaki.module.ModuleManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

object FiabricaClient : ClientModInitializer {
    private lateinit var openGuiKey: KeyBinding
    private val keybindCategory = KeyBinding.Category.create(Identifier.of("fiabrica", "main"))

    override fun onInitializeClient() {
        ModuleManager
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.fiabrica.openGui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                keybindCategory
            )
        )
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            EventBus.post(ClientTickEvent())
            while (openGuiKey.wasPressed()) {
                client.setScreen(ClickGui())
            }
        }
    }
}