/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.config.ListSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class Test extends Module {

    ListSetting<Real> r = this.config.create(new ListSetting.Builder<>(Real.A, Real.B).name("cock").description("suck").get());

    @Override
    public void enable() {
        Utils.Logging.message(r.getConfigSave());
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
