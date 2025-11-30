package com.rafalohaki.module.modules

import com.rafalohaki.event.ClientTickEvent
import com.rafalohaki.event.EventBus
import com.rafalohaki.module.Category
import com.rafalohaki.module.Module
import kotlinx.coroutines.*
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.*
import kotlin.random.Random

/**
 * Humanized Killaura with Grim bypass.
 * Uses silent rotations, smooth interpolation, raycast LOS checks,
 * attack cooldown respect, and noise injection to simulate human behavior.
 */
class KillauraModule : Module(
    name = "Killaura",
    description = "Humanized combat bot with Grim AC bypass",
    category = Category.COMBAT
) {
    // Settings (exposed to GUI)
    var range = 4.2f
        private set
    var cps = 12f // Clicks per second
        private set
    var rotationSpeed = 15f // Degrees per tick
        private set
    var playersOnly = false
        private set
    var throughWalls = false
        private set
    
    // Internal state (thread-safe)
    private val currentTarget = AtomicReference<LivingEntity?>(null)
    private val lastAttackTime = AtomicReference(0L)
    
    // Silent rotations (server-side only, client camera unchanged)
    private var serverYaw = 0f
    private var serverPitch = 0f
    private var isRotating = false
    
    // State machine
    private enum class State { IDLE, SCANNING, ROTATING, READY, ATTACKING }
    private var currentState = State.IDLE
    
    // Coroutine scope
    private var scanJob: Job? = null
    
    private val tickHandler: (ClientTickEvent) -> Unit = { onTick() }
    
    override fun onEnable() {
        val mc = MinecraftClient.getInstance()
        serverYaw = mc.player?.yaw ?: 0f
        serverPitch = mc.player?.pitch ?: 0f
        currentState = State.IDLE
        
        // Start background scanning coroutine
        scanJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                scanForTargets()
                delay(50) // Scan every 50ms, not every tick
            }
        }
    }
    
    override fun onDisable() {
        scanJob?.cancel()
        currentTarget.set(null)
        currentState = State.IDLE
        isRotating = false
    }
    
    override fun registerEvents() {
        EventBus.register(tickHandler)
    }
    
    override fun unregisterEvents() {
        EventBus.unregister(tickHandler)
    }
    
    /**
     * Main tick handler (runs on main thread).
     */
    private fun onTick() {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val target = currentTarget.get()
        
        when (currentState) {
            State.IDLE -> {
                if (target != null) {
                    currentState = State.SCANNING
                }
            }
            State.SCANNING -> {
                if (target == null || !isValidTarget(target)) {
                    currentState = State.IDLE
                    return
                }
                currentState = State.ROTATING
            }
            State.ROTATING -> {
                if (target == null || !isValidTarget(target)) {
                    currentState = State.IDLE
                    return
                }
                
                // Smooth rotation to target
                updateRotations(target)
                sendRotations()
                
                // Check if rotations are close enough
                val (idealYaw, idealPitch) = calculateIdealRotations(target)
                val yawDelta = abs(normalizeYaw(idealYaw - serverYaw))
                val pitchDelta = abs(idealPitch - serverPitch)
                
                if (yawDelta < 5f && pitchDelta < 5f) {
                    currentState = State.READY
                }
            }
            State.READY -> {
                if (target == null || !isValidTarget(target)) {
                    currentState = State.IDLE
                    return
                }
                
                // Keep rotating (micro-corrections)
                updateRotations(target)
                sendRotations()
                
                // Check attack cooldown
                val cooldown = player.getAttackCooldownProgress(0.5f)
                val timeSinceLastAttack = System.currentTimeMillis() - lastAttackTime.get()
                val minDelay = (1000f / cps).toLong()
                
                if (cooldown >= 0.99f && timeSinceLastAttack >= minDelay) {
                    currentState = State.ATTACKING
                }
            }
            State.ATTACKING -> {
                if (target == null || !isValidTarget(target)) {
                    currentState = State.IDLE
                    return
                }
                
                // Attack!
                attackEntity(target)
                lastAttackTime.set(System.currentTimeMillis())
                
                // Return to rotating for next attack
                currentState = State.ROTATING
            }
        }
    }
    
    /**
     * Scan for targets (background thread).
     */
    private fun scanForTargets() {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val world = mc.world ?: return
        
        // Find all entities in range
        val box = Box.of(player.pos, range.toDouble() * 2, range.toDouble() * 2, range.toDouble() * 2)
        val entities = world.getOtherEntities(player, box) { entity ->
            entity is LivingEntity && 
            entity.isAlive && 
            player.distanceTo(entity) <= range &&
            (!playersOnly || entity is PlayerEntity)
        }
        
        // Find closest valid target
        val closest = entities
            .filterIsInstance<LivingEntity>()
            .filter { isValidTarget(it) }
            .minByOrNull { player.distanceTo(it) }
        
        currentTarget.set(closest)
    }
    
    /**
     * Check if target is valid (alive, in range, visible if needed).
     */
    private fun isValidTarget(target: LivingEntity): Boolean {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return false
        
        if (!target.isAlive || target.health <= 0f) return false
        if (player.distanceTo(target) > range) return false
        
        // Raycast check (line of sight)
        if (!throughWalls && !hasLineOfSight(target)) return false
        
        return true
    }
    
    /**
     * Raycast to check if target is visible (no blocks between).
     */
    private fun hasLineOfSight(target: Entity): Boolean {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return false
        val world = mc.world ?: return false
        
        val eyePos = player.getEyePos()
        val targetPos = target.getPos().add(0.0, target.standingEyeHeight.toDouble() * 0.5, 0.0)
        val direction = targetPos.subtract(eyePos).normalize()
        val distance = eyePos.distanceTo(targetPos)
        
        val context = RaycastContext(
            eyePos,
            eyePos.add(direction.multiply(distance)),
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        )
        
        val result = world.raycast(context)
        
        // If raycast hits block before reaching target, no LOS
        return result == null || result.type == net.minecraft.util.hit.HitResult.Type.MISS ||
               result.pos.distanceTo(eyePos) >= distance - 0.1
    }
    
    /**
     * Calculate ideal rotations to target.
     */
    private fun calculateIdealRotations(target: Entity): Pair<Float, Float> {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return Pair(0f, 0f)
        
        val eyePos = player.getEyePos()
        val targetPos = target.getPos().add(0.0, target.standingEyeHeight.toDouble() * 0.5, 0.0)
        val diff = targetPos.subtract(eyePos)
        
        val distance = sqrt(diff.x * diff.x + diff.z * diff.z)
        val yaw = Math.toDegrees(atan2(diff.z, diff.x)).toFloat() - 90f
        val pitch = -Math.toDegrees(atan2(diff.y, distance)).toFloat()
        
        return Pair(normalizeYaw(yaw), pitch.coerceIn(-90f, 90f))
    }
    
    /**
     * Update silent rotations with smooth interpolation + noise.
     */
    private fun updateRotations(target: Entity) {
        val (idealYaw, idealPitch) = calculateIdealRotations(target)
        
        // Normalize current yaw
        serverYaw = normalizeYaw(serverYaw)
        
        // Calculate yaw delta (shortest path)
        var deltaYaw = normalizeYaw(idealYaw - serverYaw)
        
        // Add human-like noise (micro-corrections)
        val noise = Random.nextFloat() * 0.2f - 0.1f
        deltaYaw += noise
        
        // Smooth interpolation with ease-in-out curve
        val yawStep = (rotationSpeed * easeInOutCubic(abs(deltaYaw) / 180f)).coerceIn(1f, rotationSpeed)
        
        if (abs(deltaYaw) > yawStep) {
            serverYaw += if (deltaYaw > 0) yawStep else -yawStep
        } else {
            serverYaw = idealYaw
        }
        
        // Same for pitch (simpler, no wrap-around)
        var deltaPitch = idealPitch - serverPitch
        deltaPitch += noise * 0.5f
        
        val pitchStep = (rotationSpeed * easeInOutCubic(abs(deltaPitch) / 90f)).coerceIn(1f, rotationSpeed)
        
        if (abs(deltaPitch) > pitchStep) {
            serverPitch += if (deltaPitch > 0) pitchStep else -pitchStep
        } else {
            serverPitch = idealPitch
        }
        
        serverPitch = serverPitch.coerceIn(-90f, 90f)
        
        // Randomly skip packet send (simulate human distraction)
        if (Random.nextFloat() < 0.05f) { // 5% chance to skip
            isRotating = false
            return
        }
        
        isRotating = true
    }
    
    /**
     * Send silent rotations to server (client camera unchanged).
     */
    private fun sendRotations() {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val networkHandler = mc.networkHandler ?: return
        
        val packet = PlayerMoveC2SPacket.LookAndOnGround(
            serverYaw,
            serverPitch,
            player.isOnGround
        )
        
        networkHandler.sendPacket(packet)
    }
    
    /**
     * Attack entity with packets.
     */
    private fun attackEntity(target: Entity) {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val networkHandler = mc.networkHandler ?: return
        
        // Send attack packet
        val attackPacket = PlayerInteractEntityC2SPacket.attack(target, player.isSneaking)
        networkHandler.sendPacket(attackPacket)
        
        // Send swing animation (humanized - not always)
        if (Random.nextFloat() > 0.1f) { // 90% of the time
            val swingPacket = HandSwingC2SPacket(Hand.MAIN_HAND)
            networkHandler.sendPacket(swingPacket)
        }
        
        // Reset sprint (vanilla behavior)
        player.resetLastAttackedTicks()
    }
    
    /**
     * Normalize yaw to -180 to 180 range.
     */
    private fun normalizeYaw(yaw: Float): Float {
        var normalized = yaw % 360f
        if (normalized > 180f) normalized -= 360f
        if (normalized < -180f) normalized += 360f
        return normalized
    }
    
    /**
     * Ease-in-out cubic function for smooth acceleration/deceleration.
     */
    private fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) {
            4f * t * t * t
        } else {
            1f - (-2f * t + 2f).pow(3) / 2f
        }
    }
    
    // Setters for GUI
    fun setRange(newRange: Float) {
        range = newRange.coerceIn(3.0f, 6.0f)
    }
    
    fun setCps(newCps: Float) {
        cps = newCps.coerceIn(8f, 20f)
    }
    
    fun setRotationSpeed(newSpeed: Float) {
        rotationSpeed = newSpeed.coerceIn(5f, 30f)
    }
    
    fun setPlayersOnly(value: Boolean) {
        playersOnly = value
    }
    
    fun setThroughWalls(value: Boolean) {
        throughWalls = value
    }
}