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

class KillauraModule : Module(
    name = "Killaura",
    description = "Humanized combat bot with Grim AC bypass",
    category = Category.COMBAT
) {
    var range = 4.2f
        private set
    var cps = 12f
        private set
    var rotationSpeed = 15f
        private set
    var playersOnly = false
        private set
    var throughWalls = false
        private set
    
    private val currentTarget = AtomicReference<LivingEntity?>(null)
    private val lastAttackTime = AtomicReference(0L)
    private var serverYaw = 0f
    private var serverPitch = 0f
    private var isRotating = false
    
    private enum class State { IDLE, SCANNING, ROTATING, READY, ATTACKING }
    private var currentState = State.IDLE
    private var scanJob: Job? = null
    private val tickHandler: (ClientTickEvent) -> Unit = { onTick() }
    
    override fun onEnable() {
        val mc = MinecraftClient.getInstance()
        serverYaw = mc.player?.yaw ?: 0f
        serverPitch = mc.player?.pitch ?: 0f
        currentState = State.IDLE
        
        scanJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                scanForTargets()
                delay(50)
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
    
    private fun onTick() {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val target = currentTarget.get()
        
        when (currentState) {
            State.IDLE -> {
                if (target != null) currentState = State.SCANNING
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
                
                updateRotations(target)
                sendRotations()
                
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
                
                updateRotations(target)
                sendRotations()
                
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
                
                attackEntity(target)
                lastAttackTime.set(System.currentTimeMillis())
                currentState = State.ROTATING
            }
        }
    }
    
    private fun scanForTargets() {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val world = mc.world ?: return
        
        val playerPos = Vec3d(player.x, player.y, player.z)
        val box = Box.of(playerPos, range.toDouble() * 2, range.toDouble() * 2, range.toDouble() * 2)
        val entities = world.getOtherEntities(player, box) { entity ->
            entity is LivingEntity && 
            entity.isAlive && 
            player.distanceTo(entity) <= range &&
            (!playersOnly || entity is PlayerEntity)
        }
        
        val closest = entities
            .filterIsInstance<LivingEntity>()
            .filter { isValidTarget(it) }
            .minByOrNull { player.distanceTo(it) }
        
        currentTarget.set(closest)
    }
    
    private fun isValidTarget(target: LivingEntity): Boolean {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return false
        
        if (!target.isAlive || target.health <= 0f) return false
        if (player.distanceTo(target) > range) return false
        if (!throughWalls && !hasLineOfSight(target)) return false
        
        return true
    }
    
    private fun hasLineOfSight(target: Entity): Boolean {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return false
        val world = mc.world ?: return false
        
        val eyePos = Vec3d(player.x, player.eyeY, player.z)
        val targetPos = Vec3d(target.x, target.y + target.standingEyeHeight.toDouble() * 0.5, target.z)
        val direction = targetPos.subtract(eyePos).normalize()
        val distance = eyePos.distanceTo(targetPos)
        
        val context = RaycastContext(
            eyePos,
            eyePos.add(direction.multiply(distance, distance, distance)),
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        )
        
        val result = world.raycast(context)
        return result == null || result.type == net.minecraft.util.hit.HitResult.Type.MISS ||
               result.pos.distanceTo(eyePos) >= distance - 0.1
    }
    
    private fun calculateIdealRotations(target: Entity): Pair<Float, Float> {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return Pair(0f, 0f)
        
        val eyePos = Vec3d(player.x, player.eyeY, player.z)
        val targetPos = Vec3d(target.x, target.y + target.standingEyeHeight.toDouble() * 0.5, target.z)
        val diff = targetPos.subtract(eyePos)
        
        val distanceXZ = sqrt(diff.x * diff.x + diff.z * diff.z)
        val yaw = Math.toDegrees(atan2(diff.z, diff.x)).toFloat() - 90f
        val pitch = -Math.toDegrees(atan2(diff.y, distanceXZ)).toFloat()
        
        return Pair(normalizeYaw(yaw), pitch.coerceIn(-90f, 90f))
    }
    
    private fun updateRotations(target: Entity) {
        val (idealYaw, idealPitch) = calculateIdealRotations(target)
        serverYaw = normalizeYaw(serverYaw)
        
        var deltaYaw = normalizeYaw(idealYaw - serverYaw)
        val noise = Random.nextFloat() * 0.2f - 0.1f
        deltaYaw += noise
        
        val yawStep = (rotationSpeed * easeInOutCubic(abs(deltaYaw) / 180f)).coerceIn(1f, rotationSpeed)
        
        if (abs(deltaYaw) > yawStep) {
            serverYaw += if (deltaYaw > 0) yawStep else -yawStep
        } else {
            serverYaw = idealYaw
        }
        
        var deltaPitch = idealPitch - serverPitch
        deltaPitch += noise * 0.5f
        
        val pitchStep = (rotationSpeed * easeInOutCubic(abs(deltaPitch) / 90f)).coerceIn(1f, rotationSpeed)
        
        if (abs(deltaPitch) > pitchStep) {
            serverPitch += if (deltaPitch > 0) pitchStep else -pitchStep
        } else {
            serverPitch = idealPitch
        }
        
        serverPitch = serverPitch.coerceIn(-90f, 90f)
        
        if (Random.nextFloat() < 0.05f) {
            isRotating = false
            return
        }
        
        isRotating = true
    }
    
    private fun sendRotations() {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val networkHandler = mc.networkHandler ?: return
        
        val packet = PlayerMoveC2SPacket.LookAndOnGround(
            serverYaw,
            serverPitch,
            player.isOnGround,
            player.horizontalCollision
        )
        
        networkHandler.sendPacket(packet)
    }
    
    private fun attackEntity(target: Entity) {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val networkHandler = mc.networkHandler ?: return
        
        val attackPacket = PlayerInteractEntityC2SPacket.attack(target, player.isSneaking)
        networkHandler.sendPacket(attackPacket)
        
        if (Random.nextFloat() > 0.1f) {
            val swingPacket = HandSwingC2SPacket(Hand.MAIN_HAND)
            networkHandler.sendPacket(swingPacket)
        }
        
        player.resetLastAttackedTicks()
    }
    
    private fun normalizeYaw(yaw: Float): Float {
        var normalized = yaw % 360f
        if (normalized > 180f) normalized -= 360f
        if (normalized < -180f) normalized += 360f
        return normalized
    }
    
    private fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) {
            4f * t * t * t
        } else {
            1f - (-2f * t + 2f).pow(3) / 2f
        }
    }
    
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