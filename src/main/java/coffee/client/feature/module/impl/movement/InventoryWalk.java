/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.config.annotation.VisibilitySpecifier;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class InventoryWalk extends Module {
    @Setting(name = "Mouse in inventory", description = "Uses mouse movements while in an inventory to look around")
    public boolean mouseInInventory = true;
    @Setting(name = "Mouse speed", description = "How fast to turn with mouse movements", min = 0, max = 40, precision = 1)
    double mouseSpeed = 10;

    @VisibilitySpecifier("Mouse speed")
    boolean showMouseSpeed() {
        return mouseInInventory;
    }

    public float getMSpeed() {
        return (float) (mouseSpeed - 39f);
    }

    public InventoryWalk() {
        super("InventoryWalk", "Allows you to walk with an open inventory", ModuleType.MOVEMENT);
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
