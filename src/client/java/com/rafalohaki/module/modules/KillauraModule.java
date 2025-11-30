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

        tickCounter++;
        
        // Skanuj co 3 ticki zamiast w każdym (optymalizacja)
        if (tickCounter % 3 == 0) {
            scanForTargets();
        }
        
        // Losowe opóźnienie między atakami
        if (tickCounter < nextActionTick) {
            return;
        }
        tickCounter = 0;
        nextActionTick = random.nextInt(2, 5);
        
        LivingEntity target = currentTarget.get();
        if (target == null || !isValidTarget(target)) {
            currentTarget.set(null);
            return;
        }

        // Sprawdź cooldown
        float cooldown = mc.player.getAttackCooldownProgress(0.5f);
        long timeSinceLastAttack = System.currentTimeMillis() - lastAttackTime;

        if (cooldown >= 0.9f && timeSinceLastAttack >= nextAttackDelay) {
            if (mc.player.distanceTo(target) <= range) {
                performFlickAttack(target); 
                lastAttackTime = System.currentTimeMillis();
                nextAttackDelay = calculateRandomDelay();
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

        // Pobierz aktualną pozycję targeta
        Vec3d currentPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d velocity = target.getVelocity();
        double velocityMagnitude = velocity.length();
        
        // === POPRAWIONA DETEKCJA KNOCKBACKU ===
        // Oblicz kierunek: czy velocity oddala target od gracza czy zbliża?
        Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Vec3d toTarget = currentPos.subtract(playerPos);
        double toTargetLength = toTarget.length();
        
        boolean isMovingAway = false;
        if (toTargetLength > 0.01 && velocityMagnitude > 0.05) {
            Vec3d toTargetNorm = toTarget.normalize();
            Vec3d velocityNorm = velocity.normalize();
            double dot = toTargetNorm.dotProduct(velocityNorm);
            // dot > 0 = zbliża się do gracza, dot < 0 = oddala się (knockback)
            isMovingAway = dot > 0.2; // Target oddala się od gracza
        }
        
        // Knockback = wysoka velocity LUB oddala się z jakąkolwiek velocity > 0.12
        // Próg obniżony z 0.35 na 0.15 żeby objąć knockback decay phase
        boolean isKnockback = velocityMagnitude > 0.15 || (isMovingAway && velocityMagnitude > 0.12);
        
        // Ping
        int pingMs = 100;
        if (mc.getNetworkHandler().getPlayerListEntry(target.getUuid()) != null) {
            pingMs = mc.getNetworkHandler().getPlayerListEntry(target.getUuid()).getLatency();
        }

        // === POPRAWIONA PREDYKCJA ===
        Vec3d predictedPos;
        if (isKnockback || velocityMagnitude < 0.08) {
            // Knockback (włącznie z decay) lub stoi - BEZ predykcji
            // Target jest w knockbacku - celuj w aktualną pozycję
            predictedPos = currentPos;
        } else {
            // Prawdziwy normalny ruch (chodzenie/sprint) - predykcja ping-based
            // Tylko gdy target się rusza NORMALNIE (nie knockback decay)
            float predictionTicks = Math.min(2.0f, (pingMs / 50.0f) * 0.6f);
            predictedPos = new Vec3d(
                currentPos.x + velocity.x * predictionTicks,
                currentPos.y + velocity.y * predictionTicks,
                currentPos.z + velocity.z * predictionTicks
            );
        }

        // Oblicz rotacje
        Vec3d eyePos = mc.player.getEyePos();
        
        // Konsystentna wysokość celowania (55% + mała losowość)
        double heightOffset = target.getStandingEyeHeight() * 0.55;
        heightOffset += (random.nextDouble() - 0.5) * 0.1 * target.getStandingEyeHeight();
        
        Vec3d aimPoint = new Vec3d(predictedPos.x, predictedPos.y + heightOffset, predictedPos.z);
        Vec3d diff = aimPoint.subtract(eyePos);
        double distanceXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        
        float idealYaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
        float idealPitch = (float) -Math.toDegrees(Math.atan2(diff.y, distanceXZ));
        
        // Jitter - skalowany względem CPS i knockbacku
        float cpsScale = Math.min(1.0f, cps / 12.0f);
        float jitterScale = isKnockback ? 0.2f : (0.6f * cpsScale);
        float jitterYaw = (random.nextFloat() - 0.5f) * 1.2f * jitterScale;
        float jitterPitch = (random.nextFloat() - 0.5f) * 0.8f * jitterScale;
        
        float finalYaw = normalizeYaw(idealYaw + jitterYaw);
        float finalPitch = Math.max(-90f, Math.min(90f, idealPitch + jitterPitch));

        // POPRAWIONY pakiet - użyj konstruktora z Vec3d
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
            new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()),
            finalYaw, 
            finalPitch, 
            mc.player.isOnGround(),
            mc.player.horizontalCollision
        ));

        mc.getNetworkHandler().sendPacket(
            PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking())
        );

        if (random.nextFloat() > 0.1f) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        
        mc.player.resetLastAttackedTicks();
    }
    
    private void scanForTargets() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        Vec3d playerPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
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
        
        // Podstawowe checki
        if (!target.isAlive() || target.getHealth() <= 0f) return false;
        if (target.isRemoved() || target.isSpectator()) return false;
        if (mc.player.distanceTo(target) > range) return false;
        
        // Team check - nie atakuj teammatów
        if (mc.player.isTeammate(target)) return false;
        
        // Players only filter
        if (playersOnly && !(target instanceof PlayerEntity)) return false;
        
        // Line of sight
        if (!throughWalls && !hasLineOfSight(target)) return false;
        
        return true;
    }
    
    private boolean hasLineOfSight(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;
        
        Vec3d eyePos = mc.player.getEyePos();
        
        // Testuj 3 punkty hitboxa
        Vec3d[] testPoints = {
            new Vec3d(target.getX(), target.getY() + 0.2, target.getZ()),
            new Vec3d(target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ()),
            target.getEyePos()
        };
        
        for (Vec3d testPoint : testPoints) {
            RaycastContext context = new RaycastContext(
                eyePos, testPoint,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                mc.player
            );
            
            var result = mc.world.raycast(context);
            if (result.getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
                return true;
            }
        }
        
        return false;
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