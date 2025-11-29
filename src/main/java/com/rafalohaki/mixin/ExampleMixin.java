package com.rafalohaki.mixin

import net.minecraft.server.MinecraftServer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(MinecraftServer::class)
class ExampleMixin {
    @Inject(at = [At("HEAD")], method = ["loadWorld"], cancellable = true)
    fun init(info: CallbackInfo) {
        // Code injected into MinecraftServer.loadWorld()
    }
}