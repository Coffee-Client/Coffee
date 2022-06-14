/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element.impl.config;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.gui.clickgui.theme.Theme;
import coffee.client.feature.gui.clickgui.theme.ThemeManager;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

import java.awt.Color;

public class BooleanSettingEditor extends ConfigBase<BooleanSetting> {
    double animProgress = 0;

    public BooleanSettingEditor(double x, double y, double width, BooleanSetting configValue) {
        super(x, y, width, FontRenderers.getRenderer().getFontHeight() + 2, configValue);
    }

    @Override
    public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        return false;
    }

    @Override
    public boolean released() {
        return false;
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        //        System.out.println(x+", "+y+", "+button);
        if (inBounds(x, y) && button == 0) {
            //            System.out.println("clicked");
            configValue.setValue(!configValue.getValue());
            return true;
        }
        return false;
    }


    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        Theme theme = ThemeManager.getMainTheme();
        double smoothAnimProgress = Transitions.easeOutExpo(animProgress);

        double dimensionsWeCanUse = 8;
        Renderer.R2D.renderRoundedQuad(matrices, Renderer.Util.lerp(theme.getInactive(), theme.getActive(), 1 - smoothAnimProgress), x + width - dimensionsWeCanUse - 1,
                y + height / 2d - dimensionsWeCanUse / 2d, x + width - 1, y + height / 2d + dimensionsWeCanUse / 2d, 2, 20);
        matrices.push();

        float rotateDeg = 45;
        double radians = Math.toRadians(rotateDeg);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double totalWidth = 2 * cos;
        double hookHeight = 6 * cos;
        double extraHeight = 2 * sin;
        double totalHeight = Math.max(hookHeight, extraHeight);

        matrices.translate(x + width - 1 - dimensionsWeCanUse / 2d - totalWidth / 2d, y + height / 2d + totalHeight / 2d, 0);
        matrices.multiply(new Quaternion(0, 0, rotateDeg, true));

        renderHook(matrices, Color.WHITE, 0, 0, 3 * smoothAnimProgress, 5 * smoothAnimProgress, 0.75);
        matrices.pop();
        FontRenderers.getRenderer()
                .drawString(matrices, configValue.getName(), x, y + height / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d,
                        0xFFFFFF);
    }

    void renderHook(MatrixStack matrices, Color color, double x, double y, double hookWidth, double extendHeight, double thickness) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        Renderer.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        /*
         *      4 -- 3
         *      |    |
         *      |    |
         * 6 -- 5    |
         * |         |
         * 1 ------- 2 - origin
         * */

        double[][] verts = new double[][] { new double[] { 0, 0 }, new double[] { 0, -extendHeight }, new double[] { -thickness, -extendHeight },
                new double[] { -thickness, 0 },

                new double[] { 0, 0 }, new double[] { 0, -thickness }, new double[] { -hookWidth, -thickness }, new double[] { -hookWidth, 0 } };
        for (double[] vert : verts) {
            double xOffset = vert[0];
            double yOffset = vert[1];
            bufferBuilder.vertex(matrix, (float) (x + xOffset), (float) (y + yOffset), 0)
                    .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                    .next();
        }

        BufferRenderer.drawWithShader(bufferBuilder.end());
        Renderer.endRender();
    }

    @Override
    public void tickAnim() {
        double a = 0.04;
        if (!configValue.getValue()) {
            a *= -1;
        }
        animProgress += a;
        animProgress = MathHelper.clamp(animProgress, 0, 1);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return false;
    }
}
