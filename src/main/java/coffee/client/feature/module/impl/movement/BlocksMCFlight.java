/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class BlocksMCFlight extends Module {
    final List<AnimatedCircle> circles = new ArrayList<>();
    int jumpTimeout = 0;
    double yStart = 0;

    public BlocksMCFlight() {
        super("BlocksMCFlight", "Bypasses the blocksmc anticheat and flies", ModuleType.MOVEMENT);
    }

    @Override
    public void tick() {
        jumpTimeout--;
        if (jumpTimeout < 0) {
            jumpTimeout = 0;
        }
        if (CoffeeMain.client.player.getPos().y < yStart && jumpTimeout == 0) {
            CoffeeMain.client.player.jump();
            AnimatedCircle ac = new AnimatedCircle();
            ac.spawnPos = CoffeeMain.client.player.getPos();
            circles.add(ac);
            jumpTimeout = 5;
        }
        yStart -= 0.02;
    }

    @Override
    public void enable() {
        yStart = CoffeeMain.client.player.getPos().y;
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

        Vec3d ppos = Utils.getInterpolatedEntityPosition(CoffeeMain.client.player);
        Vec3d renderPos = new Vec3d(ppos.x, yStart, ppos.z);
        Renderer.R3D.renderOutline(renderPos.subtract(1, 0, 1), new Vec3d(2, 0, 2), Color.RED, matrices);
        circles.removeIf(animatedCircle -> animatedCircle.animProg > 1);
        for (AnimatedCircle circle : circles) {
            circle.render(matrices);
        }
    }

    @Override
    public void onFastTick() {
        for (AnimatedCircle circle : circles) {
            circle.animProg += 0.005;
        }
        super.onFastTick();
    }

    @Override
    public void onHudRender() {

    }

    static class AnimatedCircle {
        final Color a = new Color(200, 200, 200);
        final Color b = new Color(200, 200, 200, 0);
        double animProg = 0;
        Vec3d spawnPos;

        public void render(MatrixStack stack) {
            double progI = animProg * 2;
            double expandProg = progI / 2d; // 0-2 of 0-2 as 0-1
            double colorProg = MathHelper.clamp(progI - 1, 0, 1); // 1-2 of 0-2 as 0-1
            Color color = Renderer.Util.lerp(b, a, colorProg);
            double width = expandProg * 5;
            Renderer.R3D.renderCircleOutline(stack, color, spawnPos, width, 0.03, 50);
        }
    }
}
