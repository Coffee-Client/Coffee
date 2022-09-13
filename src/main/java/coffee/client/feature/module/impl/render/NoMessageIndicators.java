/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class NoMessageIndicators extends Module {
    public NoMessageIndicators() {
        super("NoMessageIndicators", "Shut the fuck up mojang\n(removes the colored bar at the left of each chat message)", ModuleType.RENDER);
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
}
