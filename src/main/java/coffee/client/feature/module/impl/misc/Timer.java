/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;

public class Timer extends Module {

    //    final SliderValue newTps = this.config.create("New TPS", 20, 0.1, 100, 1);
    final DoubleSetting newTps = this.config.create(new DoubleSetting.Builder(20).name("New TPS")
            .description("To what to set the new tps to")
            .min(0.1)
            .max(100)
            .precision(1)
            .get());

    public Timer() {
        super("Timer", "Changes the speed of the game client side", ModuleType.MISC);
    }

    @Override
    public void tick() {
        Utils.setClientTps((float) (newTps.getValue() + 0d));
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        Utils.setClientTps(20f);
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
}
