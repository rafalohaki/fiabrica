package com.rafalohaki.module.modules;

import com.rafalohaki.event.ClientTickEvent;
import com.rafalohaki.event.EventBus;
import com.rafalohaki.module.Category;
import com.rafalohaki.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * NoFall module - prevents fall damage.
 */
public class NoFallModule extends Module {
    private final ClientTickEvent.Handler tickHandler = this::onTick;
    
    public NoFallModule() {
        super("NoFall", "Prevents fall damage", Category.PLAYER);
    }
    
    @Override
    protected void registerEvents() {
        EventBus.getInstance().register(tickHandler);
    }
    
    @Override
    protected void unregisterEvents() {
        EventBus.getInstance().unregister(tickHandler);
    }
    
    private void onTick(ClientTickEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        
        // Send on-ground packet when falling
        if (mc.player.fallDistance > 2.5f) {
            mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision)
            );
        }
    }
}
