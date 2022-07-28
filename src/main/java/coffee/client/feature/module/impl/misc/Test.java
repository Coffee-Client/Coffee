/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class Test extends Module {

    public Test() {
        super("Test", "Testing stuff with the client, can be ignored", ModuleType.MISC);
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
        Renderer.R3D.renderCircleOutline(matrices, Color.WHITE, CoffeeMain.client.player.getPos(), 5, 0.2, 16);
    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void tick() {

    }
}
