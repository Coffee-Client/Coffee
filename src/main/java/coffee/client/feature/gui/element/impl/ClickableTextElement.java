/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.element.impl;

import coffee.client.feature.gui.element.Element;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ClickableTextElement extends Element {
    FontAdapter fa;
    String content;
    Runnable clicked;
    int color;

    public ClickableTextElement(double x, double y, String content, FontAdapter fa, Runnable onClick, int textColor) {
        super(x, y, fa.getStringWidth(content), fa.getFontHeight());
        this.fa = fa;
        this.content = content;
        this.clicked = onClick;
        this.color = textColor;
    }

    @Override
    public void tickAnimations() {

    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        fa.drawString(stack, this.content, getPositionX(), getPositionY(), this.color);
        if (inBounds(mouseX, mouseY)) {
            Renderer.R2D.renderQuad(stack, Color.WHITE, getPositionX(), getPositionY() + getHeight(), getPositionX() + getWidth(), getPositionY() + getHeight() + .5);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0 && inBounds(x, y)) {
            clicked.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        return false;
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return false;
    }
}
