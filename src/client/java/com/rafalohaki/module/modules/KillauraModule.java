package com.rafalohaki.module.modules;

import com.rafalohaki.event.ClientTickEvent;
import com.rafalohaki.event.EventBus;
import com.rafalohaki.module.Category;
import com.rafalohaki.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Advanced Killaura with proper flick-based attack pattern
 * - Single rotation packet per attack (flick technique)
 * - Random intervals between decisions
 * - Human-like aim with noise and jitter
 * - No state machine - simple conditional logic
 */
public class KillauraModule extends Module {
    private float range = 3.2f;
    private float cps = 10f;
    private boolean playersOnly = false;
    private boolean throughWalls = false;
    
    // Stan wewnętrzny, proste flagi, nie skomplikowana maszyna
    private final AtomicReference<LivingEntity> currentTarget = new AtomicReference<>(null);
    private long lastAttackTime = 0L;
    private long nextAttackDelay = 1000L; // Zostanie obliczone w onEnable
    private int tickCounter = 0;
    private int nextActionTick = 2; // Losowy interwał, np. 2-4 ticki

    private final Random random = new Random();
    private final ClientTickEvent.Handler tickHandler = event -> onTick(event);
    
    public KillauraModule() {
        super("Killaura", "Advanced combat with proper flick technique", Category.COMBAT);
    }
    
    @Override
    protected void onEnable() {
        lastAttackTime = 0L;
        nextAttackDelay = calculateRandomDelay();
        tickCounter = 0;
        nextActionTick = random.nextInt(2, 5);
    }
    
    @Override
    protected void onDisable() {
        currentTarget.set(null);
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
        if (mc.player == null || mc.world == null) return;

        // Losowe ticki - psujemy automatyzm
        tickCounter++;
        if (tickCounter < nextActionTick) {
            return; // Nic nie rób w tym ticku
        }
        tickCounter = 0;
        nextActionTick = random.nextInt(2, 5); // Ustaw następny losowy interwał

        // Scan for targets in main thread instead of background
        if (tickCounter == 0) { // Only scan occasionally
            scanForTargets();
        }
        
        LivingEntity target = currentTarget.get();
        if (target == null || !isValidTarget(target)) {
            currentTarget.set(null);
            return;
        }

        // Prosty warunek ataku, bez dodatkowych stanów
        float cooldown = mc.player.getAttackCooldownProgress(0.5f);
        long timeSinceLastAttack = System.currentTimeMillis() - lastAttackTime;

        if (cooldown >= 0.9f && timeSinceLastAttack >= nextAttackDelay) {
            // Walidacja zasięgu PRZED atakiem
            if (mc.player.distanceTo(target) <= range) {
                // Wykonaj całą akcję w jednej funkcji
                performFlickAttack(target); 
                
                lastAttackTime = System.currentTimeMillis();
                nextAttackDelay = calculateRandomDelay(); // Ustaw nowy losowy czas do ataku
            }
        }
    }
    
    // Prosta funkcja zamiast 1000/cps
    private long calculateRandomDelay() {
        float avgDelay = 1000f / cps;
        float variance = avgDelay * 0.25f; // +/- 25%
        float randomOffset = (random.nextFloat() * 2f - 1f) * variance;
        return (long) (avgDelay + randomOffset);
    }
    
    private void performFlickAttack(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // --- Krok 1: Prognozowanie ---
        // Pobieramy ping w tickach, uproszczony model
        int pingInTicks = (mc.getNetworkHandler().getPlayerListEntry(target.getUuid()) != null) 
            ? mc.getNetworkHandler().getPlayerListEntry(target.getUuid()).getLatency() / 50 
            : 2; // Zakładamy 2 ticki, jeśli nie mamy pinga
        float predictionTimeTicks = pingInTicks + 1; // Dodajemy 1 tick bufora na serwer

        // Oblicz przewidywaną pozycję
        Vec3d predictedPos = new Vec3d(
            target.getX() + target.getVelocity().x * predictionTimeTicks,
            target.getY() + target.getVelocity().y * predictionTimeTicks,
            target.getZ() + target.getVelocity().z * predictionTimeTicks
        );

        // --- Krok 2: Obliczanie rotacji do przewidzianej pozycji ---
        Vec3d eyePos = mc.player.getEyePos();
        // Celuj w losowy punkt na hitboxie celu
        double heightOffset = target.getStandingEyeHeight() * (0.3 + random.nextDouble() * 0.4);
        Vec3d aimPoint = new Vec3d(predictedPos.x, predictedPos.y + heightOffset, predictedPos.z);
        
        Vec3d diff = aimPoint.subtract(eyePos);
        double distanceXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        
        float idealYaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
        float idealPitch = (float) -Math.toDegrees(Math.atan2(diff.y, distanceXZ));
        
        // --- Krok 3: Dodawanie ludzkiego szumu ---
        // Proste jittery
        float jitterYaw = (random.nextFloat() - 0.5f) * 1.5f;
        float jitterPitch = (random.nextFloat() - 0.5f) * 1.0f;
        
        float finalYaw = normalizeYaw(idealYaw + jitterYaw);
        float finalPitch = Math.max(-90f, Math.min(90f, idealPitch + jitterPitch));

        // --- Krok 4: Wysyłanie pakietów (FLICK!) ---
        // A. Wysyłamy PAKIET Z OBROTEM pozycję i rotację. To jest "flick".
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
            mc.player.getX(), mc.player.getY(), mc.player.getZ(), 
            finalYaw, finalPitch, mc.player.isOnGround()
        ));

        // B. NATYCHMIAST wysyłamy pakiet ATAKU.
        mc.getNetworkHandler().sendPacket(new PlayerInteractEntityC2SPacket(target, mc.player.isSneaking()));

        // C. Opcjonalnie, wymach ręką (losowy)
        if (random.nextFloat() > 0.15f) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        
        mc.player.resetLastAttackedTicks();
    }
    
    private void scanForTargets() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        Vec3d playerPos = mc.player.getPos();
        Box box = new Box(playerPos, playerPos).expand(range * 2, range * 2, range * 2);
        
        List<Entity> entities = mc.world.getOtherEntities(mc.player, box, entity -> 
            entity instanceof LivingEntity && 
            entity.isAlive() && 
            mc.player.distanceTo(entity) <= range &&
            (!playersOnly || entity instanceof PlayerEntity)
        );
        
        LivingEntity closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && isValidTarget((LivingEntity) entity)) {
                double distance = mc.player.distanceTo(entity);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = (LivingEntity) entity;
                }
            }
        }
        
        currentTarget.set(closest);
    }
    
    private boolean isValidTarget(LivingEntity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        
        if (!target.isAlive() || target.getHealth() <= 0f) return false;
        if (mc.player.distanceTo(target) > range) return false;
        if (!throughWalls && !hasLineOfSight(target)) return false;
        
        return true;
    }
    
    private boolean hasLineOfSight(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;
        
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetCenter = new Vec3d(
            target.getX(),
            target.getY() + target.getStandingEyeHeight() * 0.5,
            target.getZ()
        );
        Vec3d direction = targetCenter.subtract(eyePos).normalize();
        double distance = eyePos.distanceTo(targetCenter);
        
        RaycastContext context = new RaycastContext(
            eyePos,
            eyePos.add(direction.multiply(distance, distance, distance)),
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            mc.player
        );
        
        var result = mc.world.raycast(context);
        return result == null ||
               result.getType() == net.minecraft.util.hit.HitResult.Type.MISS ||
               result.getPos().distanceTo(eyePos) >= distance - 0.1;
    }
    
    private float normalizeYaw(float yaw) {
        float normalized = yaw % 360f;
        if (normalized > 180f) normalized -= 360f;
        if (normalized < -180f) normalized += 360f;
        return normalized;
    }
    
    // Settings getters and setters
    public float getRange() { return range; }
    public float getCps() { return cps; }
    public boolean isPlayersOnly() { return playersOnly; }
    public boolean isThroughWalls() { return throughWalls; }
    
    public void setRange(float newRange) {
        range = Math.max(2.8f, Math.min(3.5f, newRange));
    }
    
    public void setCps(float newCps) {
        cps = Math.max(8f, Math.min(12f, newCps));
    }
    
    public void setPlayersOnly(boolean value) {
        playersOnly = value;
    }
    
    public void setThroughWalls(boolean value) {
        throughWalls = value;
    }
}