/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;

@Getter
public class NoSlow extends Module {
    @Setting(name = "Using items", description = "Prevents slowing down from using items")
    boolean eating = true;

    @Setting(name = "Cobwebs", description = "Prevents slowing down from cobwebs")
    boolean cobwebs = true;

    public NoSlow() {
        super("NoSlow", "Prevents slowing down from eating, cobwebs, etc", ModuleType.MOVEMENT);
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
