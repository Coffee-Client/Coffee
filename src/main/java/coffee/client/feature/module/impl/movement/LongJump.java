/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class LongJump extends Module {

    final DoubleSetting xz = this.config.create(new DoubleSetting.Builder(5).name("Speed").description("How fast to throw you forwards").min(0).max(20).precision(2).get());
    final EnumSetting<FocusType> focus = this.config.create(new EnumSetting.Builder<>(FocusType.Direction).name("Focus on").description("What to focus on when throwing you forwards").get());
    final BooleanSetting glide = this.config.create(new BooleanSetting.Builder(true).name("Glide").description("Whether to glide after the initial jump").get());
    final DoubleSetting glideVelocity = this.config.create(new DoubleSetting.Builder(0.05).name("Glide velocity").description("How strong to glide").min(-0.08).max(0.07).precision(2).get());
    final BooleanSetting keepApplying = this.config.create(new BooleanSetting.Builder(true).name("Keep applying").description("Whether to keep applying velocity after the jump").get());
    final DoubleSetting applyStrength = this.config.create(new DoubleSetting.Builder(0.3).name("Apply strength").description("How much to apply after the jump").min(0.01).max(0.3).precision(3).get());
    boolean jumped = false;

    public LongJump() {
        super("LongJump", "Jumps for a longer distance", ModuleType.MOVEMENT);
        glideVelocity.showIf(glide::getValue);
        applyStrength.showIf(keepApplying::getValue);
    }

    Vec3d getVel() {
        float f = Objects.requireNonNull(CoffeeMain.client.player).getYaw() * 0.017453292F;
        double scaled = xz.getValue() / 5;
        return switch (focus.getValue()) {
            case Direction -> new Vec3d(-MathHelper.sin(f) * scaled, 0.0D, MathHelper.cos(f) * scaled);
            case Velocity ->
                new Vec3d(CoffeeMain.client.player.getVelocity().normalize().x * scaled, 0.0D, CoffeeMain.client.player.getVelocity().normalize().z * scaled);
        };
    }

    public void applyLongJumpVelocity() {
        Vec3d v = getVel();
        Objects.requireNonNull(client.player).addVelocity(v.x, v.y, v.z);
        jumped = true;
    }

    @Override
    public void tick() {
        if (!client.options.jumpKey.isPressed()) {
            jumped = false;
        }
        if (Objects.requireNonNull(client.player).getVelocity().y < 0 && !client.player.isOnGround() && client.player.fallDistance > 0 && jumped) {
            if (glide.getValue()) {
                client.player.addVelocity(0, glideVelocity.getValue(), 0);
            }
            if (keepApplying.getValue()) {
                Vec3d newVel = getVel();
                newVel = newVel.multiply(applyStrength.getValue());
                Vec3d playerVel = client.player.getVelocity();
                Vec3d reformattedVel = new Vec3d(newVel.x, 0, newVel.z);
                reformattedVel = reformattedVel.normalize();
                reformattedVel = new Vec3d(reformattedVel.x, playerVel.y, reformattedVel.z);
                client.player.setVelocity(reformattedVel);
                client.player.velocityDirty = true;
            }
        } else if (client.player.isOnGround()) {
            jumped = false;
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
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    public enum FocusType {
        Velocity, Direction
    }
}
