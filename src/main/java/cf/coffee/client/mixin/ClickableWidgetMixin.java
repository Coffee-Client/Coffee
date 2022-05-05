/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.gui.DoesMSAA;
import cf.coffee.client.feature.gui.FastTickable;
import cf.coffee.client.feature.gui.clickgui.theme.ThemeManager;
import cf.coffee.client.helper.font.FontRenderers;
import cf.coffee.client.helper.render.Renderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin implements DoesMSAA, FastTickable {
    final Color c = new Color(30, 30, 30);
    final Color c1 = new Color(15, 15, 15);
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    protected int width;
    @Shadow
    protected int height;
    double anim = 0;

    private static void renderRoundedQuadOutline(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double rad, double samples) {

        int color = c.getRGB();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        drawRoundOutline(matrix, g, h, k, f, fromX, fromY, toX, toY, rad, samples);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void drawRoundOutline(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double rad, double samples) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        double toX1 = toX - rad;
        double toY1 = toY - rad;
        double fromX1 = fromX + rad;
        double fromY1 = fromY + rad;
        double[][] map = new double[][] { new double[] { toX1, toY1, rad, samples }, new double[] { toX1, fromY1, rad, samples }, new double[] { fromX1, fromY1, rad, samples }, new double[] { fromX1, toY1, rad, samples }/*, new double[]{toX1, toY1, rad, samples}*/ };
        for (int i = 0; i < map.length; i++) {
            double[] current = map[i];
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / current[3])) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * current[2]);
                float cos = (float) (Math.cos(rad1) * current[2]);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
                bufferBuilder.vertex(matrix, (float) current[0] + sin + (float) Math.sin(rad1), (float) current[1] + cos + (float) Math.cos(rad1), 0.0F).color(cr, cg, cb, ca).next();
            }
        }
        double[] current = map[0];
        float rad1 = (float) Math.toRadians(360);
        float sin = (float) (Math.sin(rad1) * current[2]);
        float cos = (float) (Math.cos(rad1) * current[2]);
        bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex(matrix, (float) current[0] + sin + (float) Math.sin(rad1), (float) current[1] + cos + (float) Math.cos(rad1), 0.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    @Shadow
    public abstract boolean isHovered();

    @Shadow
    public abstract Text getMessage();

    @Override
    public void onFastTick() {
        double delta = 0.03;
        if (!isHovered()) delta *= -1;
        anim += delta;
        anim = MathHelper.clamp(anim, 0, 1);
    }

    @Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    void p(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (((Object) this) instanceof TextFieldWidget) return;
        ci.cancel();

        Renderer.R2D.renderRoundedQuad(matrices, Renderer.Util.lerp(c1.brighter(), c1, anim), x, y, x + width, y + height, 5, 15);

        if (((Object) this) instanceof SliderWidget wid) {
            double indicatorWidth = 1.5;
            double indicatorHeight = height;
            double sliderValue = ((SliderWidgetAccessor) wid).getValue();
            double indicatorYOffset = 0;
            double indicatorXOffset = MathHelper.lerp(sliderValue, indicatorWidth / 2d, width - indicatorWidth / 2d);
            if (indicatorXOffset < 5) {
                double vDelta = indicatorXOffset / 5d * 90;
                double cos = Math.cos(Math.toRadians(vDelta + 90)) * 5;
                indicatorHeight -= cos + 5;
                indicatorYOffset += cos + 5;
            }
            if (indicatorXOffset > width - 5) {
                double vDelta = (width - indicatorXOffset) / 5d * 90;
                double cos = Math.cos(Math.toRadians(vDelta + 90)) * 5;
                indicatorHeight -= cos + 5;
                indicatorYOffset += cos + 5;
            }
            Renderer.R2D.renderRoundedQuad(matrices, ThemeManager.getMainTheme().getAccent(), MathHelper.lerp(sliderValue, x, x + width - indicatorWidth), y + indicatorYOffset, MathHelper.lerp(sliderValue, x, x + width - indicatorWidth) + indicatorWidth, y + indicatorHeight, indicatorWidth / 2d, 10);
        }
        renderRoundedQuadOutline(matrices, Renderer.Util.lerp(c.brighter(), c, anim), x, y, x + width, y + height, 5, 15);
        FontRenderers.getRenderer().drawCenteredString(matrices, getMessage().getString(), x + width / 2d, y + height / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d, 1f, 1f, 1f, 1f);
    }
}
