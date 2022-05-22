/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.util.Transitions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class Fullbright extends Module {

    double og;

    public Fullbright() {
        super("Fullbright", "Allows you to see in complete darkness", ModuleType.RENDER);
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {
        og = MathHelper.clamp(CoffeeMain.client.options.gamma, 0, 1);
    }

    @Override
    public void disable() {
        CoffeeMain.client.options.gamma = og;
    }

    @Override
    public void onFastTick() {
        CoffeeMain.client.options.gamma = Transitions.transition(CoffeeMain.client.options.gamma, 10, 300);
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
