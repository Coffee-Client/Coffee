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
        //        float[] checkmarkDimensions = Renderer.R2D.getCheckmarkDimensions(5, 15, s*360);
        //        float wid = checkmarkDimensions[4];
        //        float hei = checkmarkDimensions[5];
        //        float centerX = checkmarkDimensions[0]+wid/2;
        //        float centerY = checkmarkDimensions[1]+hei/2;

        //        Renderer.R2D.renderQuad(stack, Color.RED, 100+checkmarkDimensions[0], 100+checkmarkDimensions[1], 100+checkmarkDimensions[2], 100+checkmarkDimensions[3]);
        //        Renderer.R2D.renderQuad(stack, Color.BLUE, 100+centerX,100+centerY,101+centerX,101+centerY);
        Renderer.R2D.renderCheckmark(stack, Color.WHITE, 100, 100, 5, 15, 1, s * 360);
    }
}
