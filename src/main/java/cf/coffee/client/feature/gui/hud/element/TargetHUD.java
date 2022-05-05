/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.hud.element;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.render.TargetHud;
import net.minecraft.client.util.math.MatrixStack;

public class TargetHUD extends HudElement {

    public TargetHUD() {
        super("Target HUD", CoffeeMain.client.getWindow().getScaledWidth() / 2f + 10, CoffeeMain.client.getWindow().getScaledHeight() / 2f + 10, TargetHud.modalWidth, TargetHud.modalHeight);
    }

    @Override
    public void renderIntern(MatrixStack stack) {
        ModuleRegistry.getByClass(TargetHud.class).draw(stack);
    }
}
