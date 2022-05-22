/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class AntiPacketKick extends Module {

    public AntiPacketKick() {
        super("AntiPacketKick", "Prevents a client disconnect caused by an internal exception", ModuleType.MISC);
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
