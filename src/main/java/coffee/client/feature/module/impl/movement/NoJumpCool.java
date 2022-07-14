/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.mixin.entity.ILivingEntityMixin;
import net.minecraft.client.util.math.MatrixStack;

public class NoJumpCool extends Module {

    public NoJumpCool() {
        super("NoJumpCool", "Removes the jump cooldown", ModuleType.MOVEMENT);
    }

    @Override
    public void tick() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        ((ILivingEntityMixin) CoffeeMain.client.player).setJumpingCooldown(0);
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
