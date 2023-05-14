/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Objects;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Always sprints when you walk", ModuleType.MOVEMENT);
    }

    @Override
    public void tick() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        if (CoffeeMain.client.options.forwardKey.isPressed() && !CoffeeMain.client.options.backKey.isPressed() && !CoffeeMain.client.player.isSneaking() &&
            !CoffeeMain.client.player.horizontalCollision) {
            Objects.requireNonNull(client.player).setSprinting(true);
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
}
