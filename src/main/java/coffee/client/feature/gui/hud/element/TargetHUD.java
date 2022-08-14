/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.hud.element;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.TargetHud;
import net.minecraft.client.util.math.MatrixStack;

public class TargetHUD extends HudElement {

    public TargetHUD() {
        super("Target HUD", CoffeeMain.client.getWindow().getScaledWidth() / 2f + 10,
                CoffeeMain.client.getWindow().getScaledHeight() / 2f + 10, TargetHud.modalWidth, TargetHud.modalHeight);
    }

    @Override
    public void renderIntern(MatrixStack stack) {
        ModuleRegistry.getByClass(TargetHud.class).draw(stack);
    }
}
