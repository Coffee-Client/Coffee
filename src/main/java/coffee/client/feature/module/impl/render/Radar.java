/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.util.Transitions;
import net.minecraft.client.util.math.MatrixStack;

public class Radar extends Module {
    final DoubleSetting scale = this.config.create(new DoubleSetting.Builder(3).name("Scale")
        .description("How much area to show around you")
        .min(0.1)
        .max(10)
        .precision(1)
        .get());
    public double iScale = 0;

    public Radar() {
        super("Radar", "Allows you to see other players and entities around", ModuleType.RENDER);
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
    public void onFastTick() {
        iScale = Transitions.transition(iScale, scale.getValue(), 30, 0);
    }

    @Override
    public void onHudRender() {

    }
}
