/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.render;

import cf.coffee.client.feature.gui.screen.SpotLightScreen;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.feature.module.NoNotificationDefault;
import net.minecraft.client.util.math.MatrixStack;

@NoNotificationDefault
public class Spotlight extends Module {
    public Spotlight() {
        super("Spotlight", "Opens the spotlight menu", ModuleType.RENDER);
    }

    @Override
    public void tick() {
        client.setScreen(new SpotLightScreen());
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
