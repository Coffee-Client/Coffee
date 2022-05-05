/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.screen;

import cf.coffee.client.feature.gui.FastTickable;
import cf.coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class TestScreen extends ClientScreen implements FastTickable {

    @Override
    public void onFastTick() {
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        Renderer.R2D.renderQuad(stack, Color.WHITE, 0, 0, width, height);
        Renderer.R2D.renderRoundedQuad(stack, new Color(200, 200, 200), 50, 50, 150, 150, 10, 20);
        Renderer.R2D.renderRoundedShadow(stack, new Color(0, 0, 0, 100), 50, 50, 150, 150, 10, 20, -5);


        super.renderInternal(stack, mouseX, mouseY, delta);
    }

}
