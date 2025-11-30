package com.rafalohaki;

import com.rafalohaki.event.ClientTickEvent;
import com.rafalohaki.event.EventBus;
import com.rafalohaki.gui.ClickGui;
import com.rafalohaki.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side mod initializer for Fiabrica.
 * Registers keybindings and initializes the module system.
 */
public class FiabricaClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("fiabrica");
    
    private KeyBinding openGuiKey;
    private KeyBinding debugKey;
    
    private static FiabricaClient INSTANCE;
    
    public static FiabricaClient getInstance() {
        return INSTANCE;
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("Fiabrica Client initializing...");
        
        // Initialize module manager
        ModuleManager.getInstance();
        LOGGER.info("ModuleManager initialized with {} modules", ModuleManager.getInstance().getModules().size());
        
        // Register primary key binding (Right Shift) - using MISC category
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.fiabrica.openGui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
            )
        );
        
        // Register debug key binding (J) - fallback if Right Shift doesn't work
        debugKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.fiabrica.debug",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                KeyBinding.Category.MISC
            )
        );
        
        LOGGER.info("Keybindings registered: Right Shift and J");
        
        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        LOGGER.info("Fiabrica Client initialized successfully!");
    }
    
    private void onClientTick(MinecraftClient client) {
        // Post event to our event bus
        EventBus.getInstance().post(new ClientTickEvent());
        
        // Handle GUI keybinds
        while (openGuiKey.wasPressed()) {
            LOGGER.debug("Open GUI key pressed (Right Shift)");
            openClickGui(client);
        }
        
        while (debugKey.wasPressed()) {
            LOGGER.info("Debug key pressed (J) - Opening ClickGui");
            openClickGui(client);
        }
    }
    
    private void openClickGui(MinecraftClient client) {
        if (client.player != null) {
            client.setScreen(new ClickGui());
        }
    }
}