/*
 * Copyright (c) 2023 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.font.adapter.impl;

import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.font.renderer.ColoredTextSegment;
import coffee.client.helper.render.AlphaOverride;
import lombok.Getter;
import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

public class RendererFontAdapter implements FontAdapter {
    @Getter
    final FontRenderer fontRenderer;

    final float si;

    public RendererFontAdapter(Font fnt, float si) {
        this.fontRenderer = new FontRenderer(new Font[] { fnt }, si);
        this.si = si;
    }

    public float getSize() {
        return si;
    }

    @Override
    public void drawString(MatrixStack matrices, String text, float x, float y, int color) {
        int color1 = color;
        if ((color1 & 0xfc000000) == 0) {
            color1 |= 0xff000000;
        }
        float a = (float) (color1 >> 24 & 255) / 255.0F;
        float r = (float) (color1 >> 16 & 255) / 255.0F;
        float g = (float) (color1 >> 8 & 255) / 255.0F;
        float b = (float) (color1 & 255) / 255.0F;
        drawString(matrices, text, x, y, r, g, b, a);
    }

    @Override
    public void drawString(MatrixStack matrices, String text, double x, double y, int color) {
        drawString(matrices, text, (float) x, (float) y, color);
    }

    public void drawString(MatrixStack stack, ColoredTextSegment cts, float x, float y) {
        float v = x;
        ArrayList<ColoredTextSegment> ctsC = new ArrayList<>();
        ctsC.add(cts);
        while (!ctsC.isEmpty()) {
            ColoredTextSegment poll = ctsC.get(0);
            ctsC.remove(0);
            ctsC.addAll(0, Arrays.asList(poll.children()));
            String text = poll.text();
            if (text.isEmpty()) {
                continue;
            }

            drawString(stack, text, v, y, poll.r(), poll.g(), poll.b(), poll.a());
            v += getStringWidth(text);
        }
    }

    @Override
    public void drawString(MatrixStack matrices, String text, float x, float y, float r, float g, float b, float a) {
        float v = AlphaOverride.compute((int) (a * 255)) / 255;
        fontRenderer.drawString(matrices, text, x, y, r, g, b, v);
    }

    @Override
    public void drawCenteredString(MatrixStack matrices, String text, double x, double y, int color) {
        int color1 = color;
        if ((color1 & 0xfc000000) == 0) {
            color1 |= 0xff000000;
        }
        float a = (float) (color1 >> 24 & 255) / 255.0F;
        float r = (float) (color1 >> 16 & 255) / 255.0F;
        float g = (float) (color1 >> 8 & 255) / 255.0F;
        float b = (float) (color1 & 255) / 255.0F;
        drawCenteredString(matrices, text, x, y, r, g, b, a);
    }

    @Override
    public void drawCenteredString(MatrixStack matrices, String text, double x, double y, float r, float g, float b, float a) {
        float v = AlphaOverride.compute((int) (a * 255)) / 255;
        fontRenderer.drawCenteredString(matrices, text, (float) x, (float) y, r, g, b, v);
    }

    @Override
    public float getStringWidth(String text) {
        return fontRenderer.getStringWidth(text);
    }

    @Override
    public float getFontHeight() {
        return fontRenderer.getStringHeight("abcdefg123"); // we just need to trust it here
    }

    @Override
    public float getFontHeight(String text) {
        return getFontHeight();
    }

    @Override
    public float getMarginHeight() {
        return getFontHeight();
    }

    @Override
    public void drawString(MatrixStack matrices, String s, float x, float y, int color, boolean dropShadow) {
        drawString(matrices, s, x, y, color);
    }

    @Override
    public void drawString(MatrixStack matrices, String s, float x, float y, float r, float g, float b, float a, boolean dropShadow) {
        drawString(matrices, s, x, y, r, g, b, a);
    }

    @Override
    public String trimStringToWidth(String in, double width) {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            if (getStringWidth(sb.toString() + c) >= width) {
                return sb.toString();
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public String trimStringToWidth(String in, double width, boolean reverse) {
        return trimStringToWidth(in, width);
    }
}
