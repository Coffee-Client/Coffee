/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Objects;


public class Jesus extends Module {

    public final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Solid).name("Mode").description("How to keep you up").get());
    final DoubleSetting velStrength = this.config.create(new DoubleSetting.Builder(0.1).name("Velocity strength")
        .description("How much velocity to apply")
        .min(0.001)
        .max(0.3)
        .precision(3)
        .get());

    public Jesus() {
        super("Jesus", "Allows you to walk on water", ModuleType.MOVEMENT);
        velStrength.showIf(() -> mode.getValue() == Mode.Velocity);
    }

    @Override
    public void tick() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        if (CoffeeMain.client.player.isWet()) {
            switch (mode.getValue()) {
                case Jump -> Objects.requireNonNull(client.player).jump();
                case Velocity -> Objects.requireNonNull(client.player).setVelocity(client.player.getVelocity().x, velStrength.getValue(), client.player.getVelocity().z);
                case Legit -> Objects.requireNonNull(client.player).addVelocity(0, 0.03999999910593033, 0); // LivingEntity:1978, vanilla velocity
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
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    public enum Mode {
        Solid, Jump, Velocity, Legit
    }
}
