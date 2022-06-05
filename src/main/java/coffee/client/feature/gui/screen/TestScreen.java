/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.element.impl.ButtonGroup;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class TestScreen extends AAScreen {

    @Override
    public void onFastTick() {
        super.onFastTick();

    }


    @Override
    protected void initInternal() {
        ButtonGroup bg = new ButtonGroup(5, 5, 200, 20, ButtonGroup.LayoutDirection.RIGHT, new ButtonGroup.ButtonEntry("abc", () -> {
            System.out.println("abc");
        }), new ButtonGroup.ButtonEntry("def", () -> {
            System.out.println("def");
        }), new ButtonGroup.ButtonEntry("increid", () -> {
            System.out.println("inc");
        }));
        addChild(bg);
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        Renderer.R2D.renderQuad(stack, Color.WHITE, 0, 0, width, height);

        super.renderInternal(stack, mouseX, mouseY, delta);
    }

}
