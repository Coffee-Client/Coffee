/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.ShouldSneakQuery;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class EdgeSneak extends Module {
    boolean shouldSneak = false;

    public EdgeSneak() {
        super("EdgeSneak", "Sneaks automatically at the edges of blocks", ModuleType.MOVEMENT);
    }

    @MessageSubscription
    void querySneak(ShouldSneakQuery sq) {
        if (this.shouldSneak) {
            sq.setShouldSneak(true);
        }
    }

    @Override
    public void tick() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.world == null) {
            return;
        }
        Box bounding = CoffeeMain.client.player.getBoundingBox();
        bounding = bounding.offset(0, -1, 0);
        bounding = bounding.expand(0.3);
        boolean shouldSneak = false;
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                double xScale = x / 3d + .5;
                double zScale = z / 3d + .5;
                BlockPos current = CoffeeMain.client.player.getBlockPos().add(x, -1, z);
                BlockState bs = CoffeeMain.client.world.getBlockState(current);
                if (bs.isAir() && bounding.contains(new Vec3d(current.getX() + xScale, current.getY() + 1, current.getZ() + zScale))) {
                    shouldSneak = true;
                    break;
                }
            }
        }
        this.shouldSneak = shouldSneak;
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
