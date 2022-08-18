/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.element.impl;

import coffee.client.feature.gui.element.Element;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.textures.SpritesheetTextureSet;
import coffee.client.helper.render.textures.Texture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.Color;

public class TexturedButtonElement extends Element {
    static Color highlight = new Color(200, 200, 200, 60);
    Runnable onPress;
    IconRenderer renderer;
    Color textColor;
    public TexturedButtonElement(Color color, double x, double y, double w, double h, Runnable a, IconRenderer renderer) {
        super(x, y, w, h);
        this.onPress = a;
        this.textColor = color;
        this.renderer = renderer;
    }

    @Override
    public void tickAnimations() {

    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        if (inBounds(mouseX, mouseY)) {
            Renderer.R2D.renderRoundedQuad(stack, highlight, getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + getHeight(), 2, 5);
        }
        float r = textColor.getRed() / 255f;
        float g = textColor.getGreen() / 255f;
        float b = textColor.getBlue() / 255f;
        float a = textColor.getAlpha() / 255f;
        RenderSystem.setShaderColor(r, g, b, a);
        renderer.render(stack, getPositionX() + 2, getPositionY() + 2, getWidth() - 4, getHeight() - 4);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0 && inBounds(x, y)) {
            onPress.run();
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

    public interface IconRenderer {
        static IconRenderer fromSpritesheet(SpritesheetTextureSet spritesheetTextureSet, String name) {
            return (stack, x, y, width, height) -> spritesheetTextureSet.bindAndDraw(stack, x, y, width, height, name);
        }

        static IconRenderer fromTexture(Texture texture) {
            return ((stack, x, y, width1, height1) -> {
                texture.bind();
                Renderer.R2D.renderTexture(stack, x, y, width1, height1, 0, 0, width1, height1, width1, height1);
            });
        }

        static IconRenderer fromIdentifier(Identifier ident) {
            return (stack, x, y, width1, height1) -> {
                RenderSystem.setShaderTexture(0, ident);
                Renderer.R2D.renderTexture(stack, x, y, width1, height1, 0, 0, width1, height1, width1, height1);
            };
        }

        void render(MatrixStack stack, double x, double y, double width, double height);
    }
}
