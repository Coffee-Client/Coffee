/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class TestScreen extends AAScreen {
    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        float s = (System.currentTimeMillis() % 5000) / 5000f;

        Renderer.R2D.renderCheckmark(stack, Color.WHITE, 100, 100, 5, 15, 1, s * 360);
    }
}
