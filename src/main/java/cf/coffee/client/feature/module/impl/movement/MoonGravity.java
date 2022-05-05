/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.movement;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class MoonGravity extends Module {

    public MoonGravity() {
        super("MoonGravity", "Imitates gravity on the moon", ModuleType.MOVEMENT);
    }

    @Override
    public void tick() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        CoffeeMain.client.player.addVelocity(0, 0.0568000030517578, 0);
        // yea that's literally it
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
