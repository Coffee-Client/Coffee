/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.gui.screen.SpotlightScreen;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.feature.module.NoNotificationDefault;
import net.minecraft.client.util.math.MatrixStack;

@NoNotificationDefault
public class Spotlight extends Module {
    public Spotlight() {
        super("Spotlight", "Opens the spotlight menu", ModuleType.RENDER);
    }

    @Override
    public void tick() {
        client.setScreen(new SpotlightScreen());
        setEnabled(false);
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
