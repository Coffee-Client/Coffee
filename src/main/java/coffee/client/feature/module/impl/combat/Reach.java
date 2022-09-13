/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.module.impl.combat;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class Reach extends Module {
    final DoubleSetting reachDist = this.config.create(new DoubleSetting.Builder(3).min(3).max(10).precision(1).name("Distance").description("How far to reach").get());

    public Reach() {
        super("Reach", "Reach further", ModuleType.COMBAT);
    }

    @Override
    public void tick() {

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

    public double getReachDistance() {
        return reachDist.getValue();
    }
}
