/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.config.ListSetting;
import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class Test extends Module {

    @Setting(name = "REAL", description = "abc")
    ListSetting.FlagSet<Real> r = new ListSetting.FlagSet<>(Real.A, Real.B);

    @Override
    public void enable() {

    }

    public Test() {
        super("Test", "Testing stuff with the client, can be ignored", ModuleType.MISC);
    }

    public enum Real {
        A, B, C
    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    Entity target;

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
