/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.Keybind;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class Zoom extends Module {

    static long enabledTime = 0;
    final DoubleSetting finalFov = this.config.create(new DoubleSetting.Builder(30).name("FOV")
            .description("How far to zoom in")
            .min(1)
            .max(180)
            .precision(0)
            .get());
    final BooleanSetting hold = this.config.create(new BooleanSetting.Builder(true).name("Hold")
            .description("Disables the module when you unpress the keybind")
            .get());

    Keybind kb;
    double msens = 0.5d;

    public Zoom() {
        super("Zoom", "Imitates the spyglass with more options", ModuleType.RENDER);
        //        finalFov = (SliderValue) this.config.create("Fov", 30, 1, 180, 0).description("The FOV to zoom into");
    }

    static double easeOutBounce(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }

    public double getZoomValue(double vanilla) {
        long enabledFor = System.currentTimeMillis() - enabledTime;
        double prog = MathHelper.clamp(enabledFor / 100d, 0, 1);
        if (!Objects.requireNonNull(ModuleRegistry.getByClass(Zoom.class)).isEnabled()) {
            prog = Math.abs(1 - prog);
        }
        prog = easeOutBounce(prog);
        return Renderer.Util.lerp(vanilla, finalFov.getValue(), prog);
    }

    @Override
    public void tick() {
        if (kb == null) {
            return;
        }
        if (!kb.isPressed() && hold.getValue()) {
            this.setEnabled(false);
        }
    }

    @Override
    public void enable() {
        msens = client.options.getMouseSensitivity().getValue();
        client.options.getMouseSensitivity().setValue(msens * (finalFov.getValue() / client.options.getFov().getValue()));
        // retard the keybind thing is always an int shut the fuck up
        kb = new Keybind((int) (keybind.getValue() + 0));
        enabledTime = System.currentTimeMillis();
    }

    @Override
    public void disable() {
        enabledTime = System.currentTimeMillis();
        client.options.getMouseSensitivity().setValue(msens);
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
