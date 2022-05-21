/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.movement;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.config.DoubleSetting;
import cf.coffee.client.feature.config.EnumSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.feature.module.NoNotificationDefault;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

@NoNotificationDefault
public class Boost extends Module {

    final DoubleSetting strength = this.config.create(new DoubleSetting.Builder(3).name("Strength")
            .description("How much to boost you with")
            .min(0.1)
            .max(10)
            .precision(1)
            .get());
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Add).name("Mode")
            .description("How to boost you")
            .get());

    public Boost() {
        super("Boost", "Boosts you into the air", ModuleType.MOVEMENT);
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        setEnabled(false);
        Vec3d newVelocity = CoffeeMain.client.player.getRotationVector().multiply(strength.getValue());
        if (this.mode.getValue() == Mode.Add) {
            CoffeeMain.client.player.addVelocity(newVelocity.x, newVelocity.y, newVelocity.z);
        } else {
            CoffeeMain.client.player.setVelocity(newVelocity);
        }
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
        Add, Overwrite
    }
}
