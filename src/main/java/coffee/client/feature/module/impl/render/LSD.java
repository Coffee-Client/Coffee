/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.base.NonCancellableEvent;
import coffee.client.helper.manager.ShaderManager;
import net.minecraft.client.util.math.MatrixStack;

public class LSD extends Module {

    long startTime = System.currentTimeMillis();

    @Setting(name = "Intensity", description = "How intense the distorting effect is", min = 0, max = 6, precision = 1)
    double intensity = 1;

    public LSD() {
        super("LSD", "Holy shit", ModuleType.RENDER);
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {
        this.startTime = System.currentTimeMillis();
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

    @EventListener(EventType.HUD_RENDER_NOMSAA)
    void hudRenderRaw(NonCancellableEvent e) {
        float s = (System.currentTimeMillis() % 5000) / 5000f;
        float time = (System.currentTimeMillis() - startTime) / 5000f;
        ShaderManager.LSD.getEffect().setUniformValue("strength", 1f);
        ShaderManager.LSD.getEffect().setUniformValue("time", time);
        ShaderManager.LSD.getEffect().setUniformValue("seed", s);
        ShaderManager.LSD.render(client.getTickDelta());
    }
}
