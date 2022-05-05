/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.movement;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

import java.util.Objects;

public class EdgeJump extends Module {

    public EdgeJump() {
        super("EdgeJump", "Jumps automatically at the edges of blocks", ModuleType.MOVEMENT);
    }

    @Override
    public void tick() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.world == null) {
            return;
        }
        if (!CoffeeMain.client.player.isOnGround() || CoffeeMain.client.player.isSneaking()) {
            return;
        }

        Box bounding = CoffeeMain.client.player.getBoundingBox();
        bounding = bounding.offset(0, -0.5, 0);
        bounding = bounding.expand(-0.001, 0, -0.001);
        if (!CoffeeMain.client.world.getBlockCollisions(client.player, bounding).iterator().hasNext()) {
            Objects.requireNonNull(client.player).jump();
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
