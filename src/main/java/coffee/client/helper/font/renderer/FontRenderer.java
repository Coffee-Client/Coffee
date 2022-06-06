/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.font.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;

import java.awt.Font;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;

public class FontRenderer {
    static final Map<Character, Integer> colorMap = Util.make(() -> {
        Map<Character, Integer> ci = new HashMap<>();
        ci.put('0', 0x000000);
        ci.put('1', 0x0000AA);
        ci.put('2', 0x00AA00);
        ci.put('3', 0x00AAAA);
        ci.put('4', 0xAA0000);
        ci.put('5', 0xAA00AA);
        ci.put('6', 0xFFAA00);
        ci.put('7', 0xAAAAAA);
        ci.put('8', 0x555555);
        ci.put('9', 0x5555FF);
        ci.put('A', 0x55FF55);
        ci.put('B', 0x55FFFF);
        ci.put('C', 0xFF5555);
        ci.put('D', 0xFF55FF);
        ci.put('E', 0xFFFF55);
        ci.put('F', 0xFFFFFF);
        return ci;
    });
    final Font f;
    final Map<Character, Glyph> glyphMap = new ConcurrentHashMap<>();
    final int size;
    final float cachedHeight;

    public FontRenderer(Font f, int size) {
        this.f = f;
        this.size = size;
        init();
        cachedHeight = (float) glyphMap.values()
                .stream()
                .max(Comparator.comparingDouble(value -> value.dimensions.getHeight()))
                .orElseThrow().dimensions.getHeight() * 0.25f;
    }

    public int getSize() {
        return size;
    }

    void init() {
        char[] chars = { 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 0x40,
                0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51,
                0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x60, 0x61, 0x62,
                0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72, 0x73,
                0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0xa2, 0xa3, 0xa7, 0xa9, 0xb0, 0xb2,
                0xb3, 0xb4, 0xb7, 0xb9, 0xbb, 0xbc, 0xbd, 0xbe, 0xc0, 0xc1, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xd2, 0xd3,
                0xd4, 0xd5, 0xd6, 0xd8, 0xd9, 0xda, 0xdb, 0xdc, 0xdf, 0xe0, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xf2, 0xf3,
                0xf4, 0xf5, 0xf6, 0xf8, 0xf9, 0xfa, 0xfb, 0xfc };
        for (char aChar : chars) {
            Glyph glyph = new Glyph(aChar, f);
            glyphMap.put(aChar, glyph);
        }
    }

    public void drawString(MatrixStack matrices, String s, float x, float y, float r, float g, float b, float a) {
        float r1 = r;
        float g1 = g;
        float b1 = b;
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(0.25F, 0.25F, 1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        boolean isInSelector = false;
        for (char c : s.toCharArray()) {
            if (isInSelector) {
                char upper = String.valueOf(c).toUpperCase().charAt(0);
                int color = colorMap.getOrDefault(upper, 0xFFFFFF);
                r1 = (float) (color >> 16 & 255) / 255.0F;
                g1 = (float) (color >> 8 & 255) / 255.0F;
                b1 = (float) (color & 255) / 255.0F;
                isInSelector = false;
                continue;
            }
            if (c == '§') {
                isInSelector = true;
                continue;
            }

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            double prevWidth = drawChar(bufferBuilder, matrix, c, r1, g1, b1, a);
            matrices.translate(prevWidth, 0, 0);
        }

        matrices.pop();
    }

    @SuppressWarnings("AssignmentToForLoopParameter")
    String stripControlCodes(String in) {
        char[] s = in.toCharArray();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            char current = s[i];
            if (current == '§') {
                i++;
                continue;
            }
            out.append(current);
        }
        return out.toString();
    }

    public float getStringWidth(String text) {
        float wid = 0;
        for (char c : stripControlCodes(text).toCharArray()) {
            Glyph g = glyphMap.get(c);
            if (g == null) {
                wid += 20;
            } else {
                wid += g.dimensions.getWidth();
            }
        }
        return wid * 0.25f;
    }

    public String trimStringToWidth(String t, float maxWidth) {
        StringBuilder sb = new StringBuilder();
        for (char c : t.toCharArray()) {
            if (getStringWidth(sb.toString() + c) >= maxWidth) {
                return sb.toString();
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public void drawCenteredString(MatrixStack matrices, String s, float x, float y, float r, float g, float b, float a) {
        drawString(matrices, s, x - getStringWidth(s) / 2f, y, r, g, b, a);
    }

    public float getFontHeight() {
        return cachedHeight;
    }

    private void drawMissing(BufferBuilder bufferBuilder, Matrix4f matrix, float width, float height) {
        float r = 1f;
        float g = 1f;
        float b = 1f;
        float a = 1f;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, 0, height, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, width, height, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, width, 0, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, 0, 0, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, 0, height, 0).color(r, g, b, a).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    }

    private double drawChar(BufferBuilder bufferBuilder, Matrix4f matrix, char c, float r, float g, float b, float a) {
        Glyph glyph = glyphMap.get(c);
        if (glyph == null) {
            double missingW = 20;
            drawMissing(bufferBuilder, matrix, (float) missingW, getFontHeight() * 4);
            return missingW;
        }
        RenderSystem.setShaderTexture(0, glyph.getImageTex());

        float height = (float) glyph.dimensions.getHeight();
        float width = (float) glyph.dimensions.getWidth();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix, 0, height, 0).texture(0, 1).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, width, height, 0).texture(1, 1).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, width, 0, 0).texture(1, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, 0, 0, 0).texture(0, 0).color(r, g, b, a).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        return width;
    }
}
