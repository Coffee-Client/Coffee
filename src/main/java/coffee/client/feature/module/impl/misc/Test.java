/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.PathFinder;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class Test extends Module {

    PathFinder pf;

    public Test() {
        super("Test", "Testing stuff with the client, can be ignored", ModuleType.MISC);
    }

    @Override
    public void enable() {
        new Thread(() -> {
            pf = new PathFinder(client.player.getBlockPos(), new BlockPos(100, 100, 100));
            pf.find();
        }).start();
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
        if (pf == null || pf.startEntry == null) {
            return;
        }
        PathFinder.Entry entry = pf.startEntry;
        while (entry != null && entry.next != null) {
            Renderer.R3D.renderLine(matrices, Color.GREEN, Vec3d.of(entry.pos).add(.5, .5, .5), Vec3d.of(entry.next.pos).add(.5, .5, .5));
            entry = entry.next;
        }

    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void tick() {
    }
}
