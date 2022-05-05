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
import cf.coffee.client.helper.Rotations;
import cf.coffee.client.helper.manager.AttackManager;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AimAssist extends Module {

    final BooleanSetting attackPlayers = this.config.create(new BooleanSetting.Builder(true).name("Attack players").description("Whether or not to aim at players").get());
    final BooleanSetting attackHostile = this.config.create(new BooleanSetting.Builder(true).name("Attack hostile").description("Whether or not to aim at hostile entities").get());
    final BooleanSetting attackNeutral = this.config.create(new BooleanSetting.Builder(true).name("Attack neutral").description("Whether or not to aim at neutral entities").get());
    final BooleanSetting attackPassive = this.config.create(new BooleanSetting.Builder(true).name("Attack passive").description("Whether or nott o aim at passive entities").get());
    final BooleanSetting attackEverything = this.config.create(new BooleanSetting.Builder(true).name("Attack everything").description("Whether or not to aim at everything else").get());
    final BooleanSetting aimAtCombatPartner = this.config.create(new BooleanSetting.Builder(true).name("Aim at combat").description("Whether or not to only aim at the combat partner").get());
    final EnumSetting<PriorityMode> priority = this.config.create(new EnumSetting.Builder<>(PriorityMode.Distance).name("Priority").description("What to prioritize when aiminig").get());
    final DoubleSetting laziness = this.config.create(new DoubleSetting.Builder(1).name("Laziness").description("How lazy to get when aiming").min(0.1).max(5).precision(1).get());
    final BooleanSetting aimInstant = this.config.create(new BooleanSetting.Builder(false).name("Aim instantly").description("Whether or not to aim instantly instead of smoothly").get());
    Entity le;

    public AimAssist() {
        super("AimAssist", "Automatically aims at people around you", ModuleType.COMBAT);
        attackPlayers.showIf(() -> !aimAtCombatPartner.getValue());
        attackHostile.showIf(() -> !aimAtCombatPartner.getValue());
        attackNeutral.showIf(() -> !aimAtCombatPartner.getValue());
        attackPassive.showIf(() -> !aimAtCombatPartner.getValue());
        attackEverything.showIf(() -> !aimAtCombatPartner.getValue());
        laziness.showIf(() -> !aimInstant.getValue());
    }

    @Override
    public void tick() {
        List<Entity> attacks = new ArrayList<>();
        if (aimAtCombatPartner.getValue()) {
            if (AttackManager.getLastAttackInTimeRange() != null) {
                attacks.add(AttackManager.getLastAttackInTimeRange());
            }
        } else {
            for (Entity entity : Objects.requireNonNull(CoffeeMain.client.world).getEntities()) {
                if (!entity.isAttackable()) {
                    continue;
                }
                if (entity.equals(CoffeeMain.client.player)) {
                    continue;
                }
                if (!entity.isAlive()) {
                    continue;
                }
                if (entity.getPos().distanceTo(CoffeeMain.client.player.getPos()) > Objects.requireNonNull(CoffeeMain.client.interactionManager).getReachDistance()) {
                    continue;
                }
                boolean checked = false;
                if (entity instanceof Angerable) {
                    checked = true;
                    if (attackNeutral.getValue()) {
                        attacks.add(entity);
                    } else {
                        continue;
                    }
                }
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
                } else if (attackEverything.getValue() && !checked) {
                    attacks.add(entity);
                }
            }
        }
        if (attacks.isEmpty()) {
            le = null;
            return;
        }
        if (priority.getValue() == PriorityMode.Distance) {
            le = attacks.stream().sorted(Comparator.comparingDouble(value -> value.getPos().distanceTo(Objects.requireNonNull(CoffeeMain.client.player).getPos()))).toList().get(0);
        } else {
            // get entity with the least health if mode is ascending, else get most health
            le = attacks.stream().sorted(Comparator.comparingDouble(value -> {
                if (value instanceof LivingEntity e) {
                    return e.getHealth() * (priority.getValue() == PriorityMode.Health_ascending ? -1 : 1);
                }
                return Integer.MAX_VALUE; // not a living entity, discard
            })).toList().get(0);
        }

    }

    @Override
    public void onFastTick() {
        aimAtTarget();
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    void aimAtTarget() {
        if (!aimInstant.getValue()) {
            Rotations.lookAtPositionSmooth(le.getPos().add(0, le.getHeight() / 2d, 0), laziness.getValue());
        } else {
            Vec2f py = Rotations.getPitchYaw(le.getPos().add(0, le.getHeight() / 2d, 0));
            Objects.requireNonNull(CoffeeMain.client.player).setPitch(py.x);
            CoffeeMain.client.player.setYaw(py.y);
        }
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (le != null) {
            Vec3d origin = le.getPos();
            float h = le.getHeight();
            Renderer.R3D.renderLine(origin, origin.add(0, h, 0), Utils.getCurrentRGB(), matrices);
        }
    }

    @Override
    public void onHudRender() {

    }

    public enum PriorityMode {
        Distance, Health_ascending, Health_descending
    }
}
