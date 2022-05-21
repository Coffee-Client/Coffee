/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.combat;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.config.BooleanSetting;
import cf.coffee.client.feature.config.DoubleSetting;
import cf.coffee.client.feature.config.EnumSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.Packets;
import cf.coffee.client.helper.Rotations;
import cf.coffee.client.helper.Timer;
import cf.coffee.client.helper.manager.AttackManager;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Utils;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Killaura extends Module {

    final Timer delayExec = new Timer();
    final BooleanSetting capRangeAtMax = this.config.create(new BooleanSetting.Builder(true).name("Auto range")
            .description("Whether or not to set the range to the vanilla one")
            .get());
    final DoubleSetting range = this.config.create(new DoubleSetting.Builder(3.2).name("Range")
            .description("How far to reach")
            .min(0.1)
            .max(7)
            .precision(1)
            .get());

    final BooleanSetting automaticDelay = this.config.create(new BooleanSetting.Builder(true).name("Auto delay")
            .description("Whether or not to automatically calculate perfect delay")
            .get());
    final DoubleSetting delay = this.config.create(new DoubleSetting.Builder(0).name("Delay")
            .description("How much to wait between attacks")
            .min(0)
            .max(20)
            .precision(1)
            .get());

    final BooleanSetting attackOnlyCombatPartner = this.config.create(new BooleanSetting.Builder(true).name("Attack only combat")
            .description("Whether or not to only aim at the combat partner")
            .get());
    final BooleanSetting attackPlayers = this.config.create(new BooleanSetting.Builder(true).name("Attack players")
            .description("Whether or not to aim at players")
            .get());
    final BooleanSetting attackHostile = this.config.create(new BooleanSetting.Builder(true).name("Attack hostile")
            .description("Whether or not to aim at hostile entities")
            .get());
    final BooleanSetting attackNeutral = this.config.create(new BooleanSetting.Builder(true).name("Attack neutral")
            .description("Whether or not to aim at neutral entities")
            .get());
    final BooleanSetting attackPassive = this.config.create(new BooleanSetting.Builder(true).name("Attack passive")
            .description("Whether or nott o aim at passive entities")
            .get());
    final BooleanSetting attackEverything = this.config.create(new BooleanSetting.Builder(true).name("Attack everything")
            .description("Whether or not to aim at everything else")
            .get());

    final EnumSetting<SelectMode> mode = this.config.create(new EnumSetting.Builder<>(SelectMode.Single).name("Mode")
            .description("How to attack the entities")
            .get());
    final DoubleSetting multiLimit = this.config.create(new DoubleSetting.Builder(1).name("Targets")
            .description("How many multi targets to attack")
            .min(1)
            .max(10)
            .precision(0)
            .get());
    final EnumSetting<PriorityMode> prio = this.config.create(new EnumSetting.Builder<>(PriorityMode.Distance).name("Priority")
            .description("What to prioritize when aiming")
            .get());


    final BooleanSetting enableConfuse = this.config.create(new BooleanSetting.Builder(false).name("Enable confuse")
            .description("Whether or not to enable confuse")
            .get());
    final EnumSetting<ConfuseMode> confuseMode = this.config.create(new EnumSetting.Builder<>(ConfuseMode.TP).name("Confuse mode")
            .description("How to confuse the enemy")
            .get());
    final BooleanSetting confuseAllowClip = this.config.create(new BooleanSetting.Builder(false).name("Confuse into solid")
            .description("Allow confuse to tp into blocks")
            .get());
    final List<Entity> attacks = new ArrayList<>();
    Entity combatPartner;
    double circleProg = 0;

    public Killaura() {
        super("Killaura", "Automatically attacks entities around you", ModuleType.COMBAT);
        range.showIf(() -> !capRangeAtMax.getValue());
        delay.showIf(() -> !automaticDelay.getValue());
        multiLimit.showIf(() -> mode.getValue() == SelectMode.Multi && !attackOnlyCombatPartner.getValue());
        mode.showIf(() -> !attackOnlyCombatPartner.getValue());
        attackPlayers.showIf(() -> !attackOnlyCombatPartner.getValue());
        attackHostile.showIf(() -> !attackOnlyCombatPartner.getValue());
        attackNeutral.showIf(() -> !attackOnlyCombatPartner.getValue());
        attackPassive.showIf(() -> !attackOnlyCombatPartner.getValue());
        attackEverything.showIf(() -> !attackOnlyCombatPartner.getValue());
        prio.showIf(() -> mode.getValue() == SelectMode.Single && !attackOnlyCombatPartner.getValue());
        enableConfuse.showIf(() -> mode.getValue() == SelectMode.Single);
        confuseMode.showIf(() -> enableConfuse.getValue() && mode.getValue() == SelectMode.Single);
        confuseAllowClip.showIf(() -> enableConfuse.getValue() && mode.getValue() == SelectMode.Single);
    }

    int getDelay() {
        if (CoffeeMain.client.player == null) {
            return 0;
        }
        if (!automaticDelay.getValue()) {
            return (int) (delay.getValue() + 0);
        } else {
            ItemStack hand = CoffeeMain.client.player.getMainHandStack();
            if (hand == null) {
                hand = CoffeeMain.client.player.getOffHandStack();
            }
            if (hand == null) {
                return 10;
            }
            hand.getTooltip(CoffeeMain.client.player, TooltipContext.Default.ADVANCED);
            AtomicDouble speed = new AtomicDouble(CoffeeMain.client.player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));
            hand.getAttributeModifiers(EquipmentSlot.MAINHAND).forEach((entityAttribute, entityAttributeModifier) -> {
                if (entityAttribute == EntityAttributes.GENERIC_ATTACK_SPEED) {
                    speed.addAndGet(entityAttributeModifier.getValue());
                }
            });
            return (int) (20d / speed.get());
        }
    }

    double getRange() {
        if (CoffeeMain.client.interactionManager == null) {
            return 0;
        }
        if (capRangeAtMax.getValue()) {
            return CoffeeMain.client.interactionManager.getReachDistance();
        } else {
            return range.getValue();
        }
    }

    void doConfuse(Entity e) { // This also contains a range check
        Vec3d updatePos = Objects.requireNonNull(CoffeeMain.client.player).getPos();
        switch (confuseMode.getValue()) {
            case Behind -> {
                Vec3d p = e.getRotationVecClient();
                p = new Vec3d(p.x, 0, p.z).normalize().multiply(1.5);
                updatePos = e.getPos().add(p.multiply(-1));
            }
            case TP ->
                    updatePos = new Vec3d(e.getX() + (Math.random() * 4 - 2), e.getY(), e.getZ() + (Math.random() * 4 - 2));
            case Circle -> {
                circleProg += 20;
                circleProg %= 360;
                double radians = Math.toRadians(circleProg);
                double sin = Math.sin(radians) * 2;
                double cos = Math.cos(radians) * 2;
                updatePos = new Vec3d(e.getX() + sin, e.getY(), e.getZ() + cos);
            }
        }
        if (!confuseAllowClip.getValue() && Objects.requireNonNull(CoffeeMain.client.world)
                .getBlockState(new BlockPos(updatePos))
                .getMaterial()
                .blocksMovement()) {
            return;
        }
        if (e.getPos().distanceTo(updatePos) <= getRange()) {
            CoffeeMain.client.player.updatePosition(updatePos.x, updatePos.y, updatePos.z);
        }
    }

    @Override
    public void tick() {
        if (CoffeeMain.client.world == null || CoffeeMain.client.player == null || CoffeeMain.client.interactionManager == null) {
            return;
        }
        boolean delayHasPassed = this.delayExec.hasExpired(getDelay() * 50L);
        if (attackOnlyCombatPartner.getValue()) {
            if (AttackManager.getLastAttackInTimeRange() != null) {
                combatPartner = (AttackManager.getLastAttackInTimeRange());
            } else {
                combatPartner = null;
            }
            if (combatPartner == null) {
                return;
            }
            if (!combatPartner.isAttackable()) {
                return;
            }
            if (combatPartner.equals(CoffeeMain.client.player)) {
                return;
            }
            if (!combatPartner.isAlive()) {
                return;
            }
            if (enableConfuse.getValue()) {
                doConfuse(combatPartner);
            }
            if (combatPartner.getPos().distanceTo(CoffeeMain.client.player.getPos()) > getRange()) {
                return;
            }
            Packets.sendServerSideLook(combatPartner.getEyePos());
            Rotations.lookAtV3(combatPartner.getPos().add(0, combatPartner.getHeight() / 2, 0));
            if (delayHasPassed) {
                CoffeeMain.client.interactionManager.attackEntity(CoffeeMain.client.player, combatPartner);
                CoffeeMain.client.player.swingHand(Hand.MAIN_HAND);
                delayExec.reset();
            }
            return;
        }
        attacks.clear();
        for (Entity entity : Objects.requireNonNull(CoffeeMain.client.world).getEntities()) {
            if (attacks.size() > multiLimit.getValue()) {
                break;
            }
            if (!entity.isAttackable()) {
                continue;
            }
            if (entity.equals(CoffeeMain.client.player)) {
                continue;
            }
            if (!entity.isAlive()) {
                continue;
            }
            if (entity.getPos().distanceTo(CoffeeMain.client.player.getPos()) > getRange()) {
                continue;
            }

            if (attackEverything.getValue()) {
                attacks.add(entity);
            } else {
                if (entity instanceof Angerable) {
                    if (((Angerable) entity).getAngryAt() == CoffeeMain.client.player.getUuid()) {
                        if (attackHostile.getValue()) {
                            attacks.add(entity);
                        } else if (attackNeutral.getValue()) {
                            attacks.add(entity);
                        }
                    }
                } else {
                    if (entity instanceof PlayerEntity) {
                        if (attackPlayers.getValue()) {
                            attacks.add(entity);
                        }
                    } else if (entity instanceof HostileEntity) {
                        if (attackHostile.getValue()) {
                            attacks.add(entity);
                        }
                    } else if (entity instanceof PassiveEntity) {
                        if (attackPassive.getValue()) {
                            attacks.add(entity);
                        }
                    }
                }
            }
        }

        if (attacks.isEmpty()) {
            return;
        }
        if (mode.getValue() == SelectMode.Single) {
            Entity tar = null;
            if (prio.getValue() == PriorityMode.Distance) {
                tar = attacks.stream()
                        .sorted(Comparator.comparingDouble(value -> value.getPos()
                                .distanceTo(Objects.requireNonNull(CoffeeMain.client.player).getPos())))
                        .toList()
                        .get(0);
            } else if (prio.getValue() == PriorityMode.Health_ascending || prio.getValue() == PriorityMode.Health_descending) { // almost missed this
                // get entity with the least health if mode is ascending, else get most health
                tar = attacks.stream().sorted(Comparator.comparingDouble(value -> {
                    if (value instanceof LivingEntity e) {
                        return e.getHealth() * (prio.getValue() == PriorityMode.Health_ascending ? -1 : 1);
                    }
                    return Integer.MAX_VALUE; // not a living entity, discard
                })).toList().get(0);
            } else if (prio.getValue() == PriorityMode.Angle) {
                // get entity in front of you (or closest to the front)
                tar = attacks.stream()
                        .sorted(Comparator.comparingDouble(value -> {
                            Vec3d center = value.getBoundingBox().getCenter();
                            double offX = center.x - CoffeeMain.client.player.getX();
                            double offZ = center.z - CoffeeMain.client.player.getZ();
                            float yaw = (float) Math.toDegrees(Math.atan2(offZ, offX)) - 90F;
                            float pitch = (float) -Math.toDegrees(Math.atan2(center.y - CoffeeMain.client.player.getEyeY(), Math.sqrt(offX * offX + offZ * offZ)));
                            return Math.abs(MathHelper.wrapDegrees(yaw - CoffeeMain.client.player.getYaw())) + Math.abs(MathHelper.wrapDegrees(pitch - CoffeeMain.client.player.getPitch()));
                        }))
                        .sorted(Comparator.comparingDouble(value -> value.getPos()
                                .distanceTo(Objects.requireNonNull(CoffeeMain.client.player).getPos())))
                        .toList()
                        .get(0);
            }
            if (tar == null) {
                return;
            }
            if (enableConfuse.getValue()) {
                doConfuse(tar);
            }
            if (tar.getPos().distanceTo(CoffeeMain.client.player.getPos()) > getRange()) {
                return;
            }
            Packets.sendServerSideLook(tar.getEyePos());
            Rotations.lookAtV3(tar.getPos().add(0, tar.getHeight() / 2, 0));
            if (delayHasPassed) {
                CoffeeMain.client.interactionManager.attackEntity(CoffeeMain.client.player, tar);
                CoffeeMain.client.player.swingHand(Hand.MAIN_HAND);
                delayExec.reset();
            }
            return;
        }
        for (Entity attack : attacks) {
            Packets.sendServerSideLook(attack.getEyePos());
            Rotations.lookAtV3(attack.getPos().add(0, attack.getHeight() / 2, 0));
            if (delayHasPassed) {
                CoffeeMain.client.interactionManager.attackEntity(CoffeeMain.client.player, attack);
                CoffeeMain.client.player.swingHand(Hand.MAIN_HAND);
                delayExec.reset();
            }
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
        List<String> t = new ArrayList<>();
        t.add("T" + attacks.size());
        t.add("D" + getDelay());
        t.add("R" + getRange());
        t.add(mode.getValue().name());
        return "[" + String.join(";", t) + "]";
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (!attackOnlyCombatPartner.getValue()) {
            return;
        }
        if (combatPartner != null) {
            Vec3d origin = combatPartner.getPos();
            float h = combatPartner.getHeight();
            Renderer.R3D.renderLine(origin, origin.add(0, h, 0), Utils.getCurrentRGB(), matrices);
        }
    }

    @Override
    public void onHudRender() {

    }

    public enum SelectMode {
        Single, Multi
    }

    public enum PriorityMode {
        Distance, Health_ascending, Health_descending, Angle
    }

    public enum ConfuseMode {
        TP, Behind, Circle
    }
}
