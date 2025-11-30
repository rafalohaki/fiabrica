package com.rafalohaki.event

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity

/**
 * Fired every client tick (20 times per second).
 */
class ClientTickEvent : Event()

/**
 * Fired when rendering the world.
 */
data class RenderWorldEvent(
    val client: MinecraftClient,
    val partialTicks: Float
) : Event()

/**
 * Fired when player moves.
 */
data class PlayerMoveEvent(
    val x: Double,
    val y: Double,
    val z: Double
) : CancellableEvent()

/**
 * Fired when player attacks entity.
 */
data class AttackEntityEvent(
    val target: Entity
) : CancellableEvent()

/**
 * Fired when rendering HUD/overlay.
 */
data class RenderHudEvent(
    val partialTicks: Float
) : Event()

/**
 * Fired when player sends chat message.
 */
data class SendChatMessageEvent(
    val message: String
) : CancellableEvent()