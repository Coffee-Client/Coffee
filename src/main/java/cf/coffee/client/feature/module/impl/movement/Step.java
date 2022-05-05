/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.movement;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.config.DoubleSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Objects;

public class Step extends Module {

    //    final SliderValue height = (SliderValue) this.config.create("Step height", 3, 1, 50, 0).description("How high to step");
    final DoubleSetting height = this.config.create(new DoubleSetting.Builder(3).name("Height").description("How high to step").min(1).max(50).precision(0).get());

    public Step() {
        super("Step", "Allows you to step up full blocks", ModuleType.MOVEMENT);
    }

    @Override
    public void tick() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        CoffeeMain.client.player.stepHeight = (float) (height.getValue() + 0);
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        Objects.requireNonNull(client.player).stepHeight = 0.6f;
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
