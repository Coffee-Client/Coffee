/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

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


    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void tick() {
    }
}
