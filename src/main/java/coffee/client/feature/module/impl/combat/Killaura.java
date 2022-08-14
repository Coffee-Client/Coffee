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
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Timer;
import coffee.client.helper.util.Utils;
import com.google.common.util.concurrent.AtomicDouble;
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
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Killaura extends Module {
    Timer attackCooldown = new Timer();
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
    Random r = new Random();
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
            //            hand.getTooltip(CoffeeMain.client.player, TooltipContext.Default.ADVANCED);
            AtomicDouble speed = new AtomicDouble(CoffeeMain.client.player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));
            hand.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((entityAttribute, entityAttributeModifier) -> {
                if (entityAttribute == EntityAttributes.GENERIC_ATTACK_SPEED) {
                    speed.addAndGet(entityAttributeModifier.getValue());
                }
            });
            return (int) (20d / speed.get()) * 50 + 20; // ticks -> ms + 1 tick
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

    List<LivingEntity> selectTargets() {
        List<LivingEntity> entities = new ArrayList<>(StreamSupport.stream(client.world.getEntities().spliterator(), false)
                .filter(entity -> !entity.equals(client.player)) // filter our player out
                .filter(Entity::isAlive)
                .filter(Entity::isAttackable) // filter all entities we can't attack out
                .filter(entity -> entity instanceof LivingEntity) // filter all "entities" that aren't actual entities out
                .map(entity -> (LivingEntity) entity) // cast all entities to actual entities
                .filter(this::isEntityApplicable)
                .filter(entity -> entity.getPos()
                        .distanceTo(client.player.getEyePos()) <= getRange()) // filter all entities that are outside our range out
                .toList());
        switch (selectMode) {
            case Distance -> entities.sort(Comparator.comparingDouble(value -> value.distanceTo(client.player))); // low distance first
            case LowHealthFirst -> entities.sort(Comparator.comparingDouble(LivingEntity::getHealth)); // low health first
            case HighHealthFirst -> entities.sort(Comparator.comparingDouble(LivingEntity::getHealth).reversed()); // high health first
        }
        if (entities.isEmpty()) {
            return entities;
        }
        return switch (attackMode) {
            case Single -> List.of(entities.get(0));
            case Multi -> new ArrayList<>(entities.subList(0, Math.min(entities.size(), (int) amount)));
        };
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
            EntityHitResult ehr = ProjectileUtil.raycast(CoffeeMain.client.player, CoffeeMain.client.player.getCameraPosVec(0),
                    CoffeeMain.client.player.getCameraPosVec(0).add(ranged), allowed, Entity::isAttackable, getRange() * getRange());
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
