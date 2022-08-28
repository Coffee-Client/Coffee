/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.element.impl;

import coffee.client.feature.gui.element.Element;
import coffee.client.helper.render.Renderer;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import java.awt.Color;
import java.util.function.Consumer;

public class ColorEditorElement extends Element {
    final HueSlider slider;
    final Color color;
    double x, y;
    boolean clicked = false;
    @Setter
    Consumer<Color> onChange;

    public ColorEditorElement(double x, double y, double width, double height, Color initialColor) {
        super(x, y, width, height);
        this.color = initialColor;
        float[] floats = Color.RGBtoHSB(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), null);
        this.slider = new HueSlider(x, y, width, 6, floats[0]);
    }

    @Override
    public void tickAnimations() {

    }

    void renderColorWheel(MatrixStack matrices, double x, double y, double width, double height, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        Renderer.setupRender();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(1f, 1f, 1f, 1f).next();
        bufferBuilder.vertex(matrix, (float) (x + width), (float) y, 0.0F).color(g, h, k, 1f).next();
        bufferBuilder.vertex(matrix, (float) (x + width), (float) (y + height), 0.0F).color(g, h, k, 1f).next();
        bufferBuilder.vertex(matrix, (float) x, (float) (y + height), 0.0F).color(1f, 1f, 1f, 1f).next();

        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(0f, 0f, 0f, 0f).next();
        bufferBuilder.vertex(matrix, (float) (x + width), (float) y, 0.0F).color(0f, 0f, 0f, 0f).next();
        bufferBuilder.vertex(matrix, (float) (x + width), (float) (y + height), 0.0F).color(0f, 0f, 0f, 1f).next();
        bufferBuilder.vertex(matrix, (float) x, (float) (y + height), 0.0F).color(0f, 0f, 0f, 1f).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        Renderer.endRender();
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        renderColorWheel(stack, getPositionX(), getPositionY(), getWidth(), getHeight() - slider.getHeight() - 1, Color.HSBtoRGB((float) slider.value, 1f, 1f));
        Color value = getValue();
        Color inverted = new Color(255 - value.getRed(), 255 - value.getGreen(), 255 - value.getBlue());
        double totalHeight = (getHeight() - slider.getHeight() - 1);
        Renderer.R2D.renderCircle(stack, inverted, getPositionX() + MathHelper.clamp(x * getWidth(), 1, getWidth() - 1), getPositionY() + MathHelper.clamp(y * totalHeight, 1, totalHeight - 1), 1, 16);
        slider.setPositionX(getPositionX() + 1);
        slider.setWidth(getWidth() - 2);
        slider.setPositionY(getPositionY() + getHeight() - slider.getHeight());
        slider.render(stack, mouseX, mouseY);
    }

    void handleClick(double x, double y) {
        double translated = x - (this.getPositionX());
        double translatedY = y - this.getPositionY();
        double perX = MathHelper.clamp(translated / (width), 0, 1);
        double perY = MathHelper.clamp(translatedY / (height - slider.getHeight() - 1), 0, 1);
        this.x = perX;
        this.y = perY;
        if (onChange != null) {
            onChange.accept(getValue());
        }
    }

    public Color getValue() {
        return Color.getHSBColor((float) slider.value, (float) x, (float) (1 - y));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (inBounds(x, y) && y < getPositionY() + getHeight() - slider.getHeight() - 1 && button == 0) {
            clicked = true;
            handleClick(x, y);
            return true;
        } else {
            boolean a = slider.mouseClicked(x, y, button);
            if (a && onChange != null) {
                onChange.accept(getValue());
            }
            return a;
        }
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (clicked) {
            clicked = false;
            return true;
        }
        boolean a = slider.mouseReleased(x, y, button);
        if (a && onChange != null) {
            onChange.accept(getValue());
        }
        return a;
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        if (clicked) {
            handleClick(x, y);
            return true;
        }
        boolean a = slider.mouseDragged(x, y, xDelta, yDelta, button);
        if (a && onChange != null) {
            onChange.accept(getValue());
        }
        return a;
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

    static class HueSlider extends Element {
        double value;
        boolean clicked;

        public HueSlider(double x, double y, double width, double height, double initial) {
            super(x, y, width, height);
            this.value = initial;
        }

        @Override
        public void tickAnimations() {

        }

        @Override
        public void render(MatrixStack stack, double mouseX, double mouseY) {
            double actualX = getPositionX();
            double actualWidth = getWidth();
            Renderer.R2D.renderRoundedQuad(stack, new Color(50, 50, 50), actualX, getPositionY() + getHeight() / 2d - 1.5 / 2d, actualX + actualWidth, getPositionY() + getHeight() / 2d + 1.5 / 2d, 1.5 / 2, 5);
            Renderer.R2D.renderRoundedQuad(stack, Color.getHSBColor((float) value, 1f, 1f), actualX, getPositionY() + getHeight() / 2d - 1.5 / 2d, actualX + Math.max(actualWidth * value, 1.5), getPositionY() + getHeight() / 2d + 1.5 / 2d, 1.5 / 2, 5);
            Renderer.R2D.renderRoundedQuad(stack, Color.WHITE, actualX + Math.max(actualWidth * value, 1.5) - 1.5 / 2, getPositionY(), actualX + Math.max(actualWidth * value, 1.5) + 1.5 / 2, getPositionY() + getHeight(), 1.5 / 2, 5);
        }

        void handleClick(double x) {
            double translated = x - (this.getPositionX());
            this.value = MathHelper.clamp(translated / (width), 0, 1);

        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (inBounds(x, y)) {
                clicked = true;
                if (button == 0) {
                    handleClick(x);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            if (clicked) {
                clicked = false;
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
            if (clicked) {
                handleClick(x);
                return true;
            }
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
}
