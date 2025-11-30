package com.rafalohaki.module.modules;

import com.rafalohaki.module.Category;
import com.rafalohaki.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Fullbright module - makes everything bright.
 */
public class FullbrightModule extends Module {
    private double originalGamma = 1.0;
    
    public FullbrightModule() {
        super("Fullbright", "See in the dark", Category.RENDER);
    }
    
    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            originalGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(16.0); // Max brightness
        }
    }
    
    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            mc.options.getGamma().setValue(originalGamma);
        }
    }
}
