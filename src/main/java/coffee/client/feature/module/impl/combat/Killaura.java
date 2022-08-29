/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.combat;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.RangeSetting;
import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.config.annotation.VisibilitySpecifier;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Timer;
import coffee.client.helper.util.Utils;
import coffee.client.mixin.IPlayerListEntryMixin;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Killaura extends Module {
    private static final double[][] HITBOX_POSITION_OFFSETS = new double[][] {
        /*  -- Y --
         |  1 --- 2
         X  |     |
         |  0 --- 3
        */
        { 0, 0, 0 }, { 1, 0, 0 }, { 1, 0, 1 }, { 0, 0, 1 },

        { 0, 1, 0 }, { 1, 1, 0 }, { 1, 1, 1 }, { 0, 1, 1 }, };
    public static Int2DoubleArrayMap playersWhoHaveSpawnedAndStayedInOurRange = new Int2DoubleArrayMap();

    private static Vec3d[] getHitboxPoints(LivingEntity le) {
        float width = le.getWidth();
        float height = le.getHeight();
        Vec3d root = le.getPos().subtract(width / 2d, 0, width / 2d);
        Vec3d[] t = new Vec3d[HITBOX_POSITION_OFFSETS.length];
        for (int i = 0; i < HITBOX_POSITION_OFFSETS.length; i++) {
            double[] entry = HITBOX_POSITION_OFFSETS[i];
            Vec3d offset = new Vec3d(entry[0], entry[1], entry[2]).multiply(width, height, width);
            t[i] = root.add(offset);
        }
        return t;
    }

    @EventListener(EventType.PACKET_RECEIVE)
    void onPacketRecv(PacketEvent pe) {
        Packet<?> packet = pe.getPacket();
        if (packet instanceof PlayerSpawnS2CPacket ps) {
            Vec3d lastKnownServerPos = Rotations.getLastKnownServerPos();
            Vec3d f = new Vec3d(ps.getX(), ps.getY(), ps.getZ());
            double v = f.distanceTo(lastKnownServerPos);
            playersWhoHaveSpawnedAndStayedInOurRange.put(ps.getId(), v);
        }
    }

    boolean isWithinSusRange(Vec3d d) {
        return d.distanceTo(Rotations.getLastKnownServerPos()) < 10;
    }

    final Timer attackCooldown = new Timer();
    final Random r = new Random();
    @Setting(name = "Attack mode", description = "How to attack the selected entities")
    AttackMode attackMode = AttackMode.Single;
    @Setting(name = "Amount", description = "Amount of entities to attack at once (in multi mode)", min = 1, max = 10, precision = 0)
    double amount = 3;
    @Setting(name = "Select mode", description = "How to select the next target")
    SelectMode selectMode = SelectMode.Distance;
    @Setting(name = "Automatic delay", description = "Automatically sets the delay")
    boolean automaticDelay = true;
    @Setting(name = "Delay", description = "Delay in milliseconds", min = 0, max = 2000, precision = 0)
    double delay = 500;
    @Setting(name = "Delay random", description = "How much randomness to apply to the delay (in ms)", min = 0, max = 1000, precision = 0)
    RangeSetting.Range delayRandom = new RangeSetting.Range(0, 200);
    @Setting(name = "Automatic range", description = "Automatically uses your max range as range")
    boolean automaticRange = true;
    @Setting(name = "Range", description = "How far to attack entities", min = 1, max = 7, precision = 1)
    double range = 5;
    @Setting(name = "Smooth look", description = "Smoothly looks at the target entity before attacking it\nHelps bypass anticheats")
    boolean smoothLook = true;
    @Setting(name = "Attack passive", description = "Attacks passive mobs")
    boolean attackPassive = false;
    @Setting(name = "Attack hostile", description = "Attacks hostile mobs")
    boolean attackHostile = true;
    @Setting(name = "Attack players", description = "Attacks players")
    boolean attackPlayers = true;
    @Setting(name = "Attack all", description = "Attacks all remaining entities")
    boolean attackAll = false;
    @Setting(name = "Matrix antibot", description = "Filters the matrix bots out of the target list")
    boolean matrixAntibot = true;
    @Setting(name = "Matrix confidence", description = "How confident the antibot needs to be before filtering\n(0 = 0% confident, 1 = 100%)", min = 0, max = 1, precision = 1)
    double matrixConfidence = 0.7;
    List<LivingEntity> targets = new ArrayList<>();
    int currentRandomDelay = 0;

    public Killaura() {
        super("Killaura", "Automatically attacks all entities in range", ModuleType.COMBAT);
    }

    @VisibilitySpecifier("Delay")
    boolean shouldShowDelay() {
        return !automaticDelay;
    }

    @VisibilitySpecifier("Amount")
    boolean shouldShowAmount() {
        return attackMode == AttackMode.Multi;
    }

    @VisibilitySpecifier("Range")
    boolean shouldShowRange() {
        return !automaticRange;
    }

    @VisibilitySpecifier("Smooth look")
    boolean shouldShowSmoothLook() {
        return attackMode == AttackMode.Single;
    }

    @VisibilitySpecifier("Matrix confidence")
    boolean shouldShowConfidence() {
        return matrixAntibot;
    }

    int getDelay() {
        if (client.player == null) {
            return 0;
        }
        if (!automaticDelay) {
            return (int) (delay);
        } else {
            ItemStack hand = CoffeeMain.client.player.getMainHandStack();
            if (hand == null) {
                hand = CoffeeMain.client.player.getOffHandStack();
            }
            if (hand == null) {
                return 10;
            }
            AtomicDouble speed = new AtomicDouble(CoffeeMain.client.player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));
            hand.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((entityAttribute, entityAttributeModifier) -> {
                if (entityAttribute == EntityAttributes.GENERIC_ATTACK_SPEED) {
                    speed.addAndGet(entityAttributeModifier.getValue());
                }
            });
            return (int) (20d / speed.get()) * 50 + 20; // ticks -> ms + 1 tick
        }
    }

    @EventListener(value = EventType.PACKET_SEND, prio = 10)
    void onPacketSend(PacketEvent pe) {
        if (pe.getPacket() instanceof PlayerMoveC2SPacket) {
            for (Integer integer : playersWhoHaveSpawnedAndStayedInOurRange.keySet().toArray(Integer[]::new)) {
                Entity entityById = client.world.getEntityById(integer);
                if (entityById == null || !isWithinSusRange(entityById.getPos())) {
                    playersWhoHaveSpawnedAndStayedInOurRange.remove((int) integer); // no longer applicable
                } else {
                    playersWhoHaveSpawnedAndStayedInOurRange.put((int) integer, entityById.getPos().distanceTo(Rotations.getLastKnownServerPos()));
                }
            }
        }
    }


    double getRange() {
        if (CoffeeMain.client.interactionManager == null) {
            return 0;
        }
        if (automaticRange) {
            return CoffeeMain.client.interactionManager.getReachDistance();
        } else {
            return range;
        }
    }

    double mapFlags(boolean... d) {
        double s = 0;
        for (boolean b : d) {
            s += b ? 1 : 0;
        }
        return s / d.length;
    }

    boolean isInRange(Vec3d pos) {
        return pos.distanceTo(client.player.getEyePos()) <= getRange();
    }

    List<LivingEntity> selectTargets() {
        List<LivingEntity> entities = new ArrayList<>(StreamSupport.stream(client.world.getEntities().spliterator(), false).filter(entity -> !entity.equals(client.player)) // filter our player out
            .filter(Entity::isAlive).filter(Entity::isAttackable) // filter all entities we can't attack out
            .filter(entity -> entity instanceof LivingEntity) // filter all "entities" that aren't actual entities out
            .map(entity -> (LivingEntity) entity) // cast all entities to actual entities
            .filter(this::isEntityApplicable).filter(entity -> Arrays.stream(getHitboxPoints(entity)).anyMatch(this::isInRange)) // filter all entities that are outside our range out
            .filter(livingEntity -> {
                if (matrixAntibot) {
                    return Antibot.MATRIX.computeConfidence(livingEntity) < matrixConfidence; // true = include, required confidence must be above current confidence
                } else {
                    return true;
                }
            }).toList());
        switch (selectMode) {
            case Distance ->
                entities.sort(Comparator.comparingDouble(value -> value.distanceTo(client.player))); // low distance first
            case LowHealthFirst ->
                entities.sort(Comparator.comparingDouble(LivingEntity::getHealth)); // low health first
            case HighHealthFirst ->
                entities.sort(Comparator.comparingDouble(LivingEntity::getHealth).reversed()); // high health first
        }
        if (entities.isEmpty()) {
            return entities;
        }
        return switch (attackMode) {
            case Single -> List.of(entities.get(0));
            case Multi -> new ArrayList<>(entities.subList(0, Math.min(entities.size(), (int) amount)));
        };
    }

    public static class Antibot {
        public static AntibotEntry MATRIX = new AntibotEntry("Matrix", AntibotCheck.from("NameLowercase", 0.3, e -> StringUtils.isAllLowerCase(e.getEntityName())), AntibotCheck.from("NoVelocity", 0.1, e -> e.getVelocity().equals(Vec3d.ZERO)), AntibotCheck.from("DefaultSkin", 0.1, e -> {
            PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(e.getUuid());
            return playerListEntry != null && ((IPlayerListEntryMixin) playerListEntry).coffee_getTextures().get(MinecraftProfileTexture.Type.SKIN) == null;
        }), AntibotCheck.from("YTooClose", 0.1, e -> {
            double diff = e.getPos().y - Rotations.getLastKnownServerPos().y;
            return Math.abs(diff) < 1.4;
        }), AntibotCheck.from("IllegalSprint", 0.2, e -> {
            if (!e.isSprinting()) {
                return false;
            }
            Vec3d lookDir1 = e.getRotationVector();
            Vec3d lookDir = new Vec3d(lookDir1.x, 0, lookDir1.z).normalize();
            Vec3d positionDiff = e.getPos().subtract(e.prevX, e.prevY, e.prevZ);
            if (positionDiff.length() < 0.15) {
                return true; // diff too small to be sprinting
            }
            Vec3d positionDiff1 = new Vec3d(positionDiff.x, 0, positionDiff.z).normalize();
            double diff = Math.abs(Math.acos(lookDir.z) - Math.acos(positionDiff1.z));
            return diff >= 0.9; // illegal angle at which to be sprinting
        }), AntibotCheck.from("SpawnedAndStayedWithinRange", 0.3, e -> playersWhoHaveSpawnedAndStayedInOurRange.containsKey(e.getId())));

        public interface Testable {
            boolean violates(LivingEntity e);
        }

        public abstract static class AntibotCheck implements Testable {
            public static AntibotCheck from(String name, double important, Testable f) {
                return new AntibotCheck() {
                    @Override
                    public String name() {
                        return name;
                    }

                    @Override
                    double importance() {
                        return important;
                    }

                    @Override
                    public boolean violates(LivingEntity e) {
                        return f.violates(e);
                    }
                };
            }

            abstract String name();

            abstract double importance();

            @Override
            public String toString() {
                return name();
            }
        }

        public record AntibotEntry(String name, AntibotCheck... checks) {
            public double computeConfidence(LivingEntity e) {
                double d = Arrays.stream(checks).map(antibotCheck -> antibotCheck.violates(e) ? antibotCheck.importance() : 0).reduce(Double::sum).orElse(0d);
                return d / Arrays.stream(checks).map(AntibotCheck::importance).reduce(Double::sum).orElse(0d);
            }

            public AntibotCheck[] getViolatingChecks(LivingEntity e) {
                return Arrays.stream(checks).filter(antibotCheck -> antibotCheck.violates(e)).toArray(AntibotCheck[]::new);
            }
        }
    }

    boolean isEntityApplicable(LivingEntity le) {
        if (le instanceof PlayerEntity) {
            return attackPlayers;
        } else if (le instanceof Monster) {
            return attackHostile;
        } else if (le instanceof PassiveEntity) {
            return attackPassive;
        }
        return attackAll;
    }

    @Override
    public void tick() {
        targets = selectTargets();
        if (!attackCooldown.hasExpired(getDelay() + currentRandomDelay)) {
            return;
        }
        if (targets.isEmpty()) {
            return;
        }
        boolean smooth = smoothLook && attackMode == AttackMode.Single;
        if (smooth) {
            LivingEntity target = targets.get(0);
            Vec3d ranged = Rotations.getRotationVector(Rotations.getClientPitch(), Rotations.getClientYaw()).multiply(getRange());
            Box allowed = client.player.getBoundingBox().stretch(ranged).expand(1, 1, 1);
            EntityHitResult ehr = ProjectileUtil.raycast(CoffeeMain.client.player, CoffeeMain.client.player.getCameraPosVec(0), CoffeeMain.client.player.getCameraPosVec(0).add(ranged), allowed, Entity::isAttackable, getRange() * getRange());
            if (ehr != null && ehr.getEntity().equals(target)) {
                attack(target);
                pickNextRandomDelay();
                attackCooldown.reset();
            }
        } else {
            pickNextRandomDelay();
            attackCooldown.reset();
            for (LivingEntity target : targets) {
                attack(target);
            }
        }
    }

    void pickNextRandomDelay() {
        int min = (int) delayRandom.getMin();
        int max = (int) delayRandom.getMax();
        if (min >= max) {
            currentRandomDelay = 0;
        } else {
            currentRandomDelay = r.nextInt(min, max);
        }
    }

    void attack(LivingEntity target) {
        CoffeeMain.client.interactionManager.attackEntity(CoffeeMain.client.player, target);
        CoffeeMain.client.player.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public void onFastTick() {
        boolean smooth = smoothLook && attackMode == AttackMode.Single;
        if (smooth && !targets.isEmpty()) {
            LivingEntity le = targets.get(0);
            Rotations.lookAtPositionSmoothServerSide(Utils.getInterpolatedEntityPosition(le).add(0, le.getHeight() / 2d, 0), 10);
        }
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("Del", getDelay() + "+" + currentRandomDelay);
        data.put("Ran", getRange());
        data.put("Tar", targets.size());
        return data.keySet().stream().map(s -> s + ":" + data.get(s).toString()).collect(Collectors.joining(" | "));
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {


    }

    @Override
    public void onHudRender() {

    }

    public enum AttackMode {
        Single, Multi
    }

    public enum SelectMode {
        Distance, LowHealthFirst, HighHealthFirst
    }
}
