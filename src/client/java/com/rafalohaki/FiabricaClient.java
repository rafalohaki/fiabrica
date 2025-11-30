package com.rafalohaki;

import com.rafalohaki.event.ClientTickEvent;
import com.rafalohaki.event.EventBus;
import com.rafalohaki.gui.ClickGui;
import com.rafalohaki.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FiabricaClient implements ClientModInitializer {
    private KeyBinding openGuiKey;
    private static final KeyBinding.Category KEYBIND_CATEGORY = KeyBinding.Category.create(Identifier.of("fiabrica", "main"));

    @Override
    public void onInitializeClient() {
        // Initialize module manager
        ModuleManager.getInstance();
        
        // Register key binding
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.fiabrica.openGui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KEYBIND_CATEGORY
            )
        );
        
        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            EventBus.getInstance().post(new ClientTickEvent());
            
            while (openGuiKey.wasPressed()) {
                client.setScreen(new ClickGui());
            }
        });
    }
}