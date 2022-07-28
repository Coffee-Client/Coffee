/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class Slippy extends Module {
    @Setting(name = "Slipperiness", description = "How slippy to make the blocks\n§cSetting this to > 1 can lead to never stopping", min = 0, max = 3, precision = 1)
    public final double slipperiness = .8;

    public Slippy() {
        super("Slippy", "Makes every block slippy like ice", ModuleType.MOVEMENT);
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
