/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.notifications.hudNotif;

import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.textures.Texture;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.awt.Color;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HudNotification {
    final String content;
    final long duration;
    final Type type;
    final long creation = System.currentTimeMillis();
    final double texDim = 16;
    final double pad = 2;
    final long fadeTime = 1200;

    public static HudNotification create(String content, long duration, Type type) {
        HudNotification n = new HudNotification(content, duration, type);
        HudNotificationRenderer.instance.addNotification(n);
        return n;
    }

    public long getRemainingLifeTime() {
        return Math.max(0, creation + duration - System.currentTimeMillis());
    }

    public boolean isDead() {
        return getRemainingLifeTime() == 0;
    }

    public double getHeight() {
        return pad + Math.max(FontRenderers.getRenderer().getFontHeight(), texDim) + pad;
    }

    public double easeInOutBack(double x) {
        double c1 = 1.70158;
        double c2 = c1 * 1.525;

        return x < 0.5 ? Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2) / 2 : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;

    }

    double getAnimProg() {
        long remainingLife = getRemainingLifeTime();
        long timeExpired = duration - remainingLife;
        double animAccordingToTime = (double) Math.min(timeExpired, fadeTime) / fadeTime;
        double animAccordingToRemaining = (double) Math.min(remainingLife, fadeTime) / fadeTime;
        return Math.min(animAccordingToRemaining, animAccordingToTime) * 1.1;
    }

    public void render(MatrixStack stack, double x, double y) {
        FontAdapter fa = FontRenderers.getRenderer();
        double anim = getAnimProg();
        double moveAnim = easeInOutBack(MathHelper.clamp(anim, 0, 0.5) * 2);
        double expandAnim = easeInOutBack(MathHelper.clamp(anim, 0.1, 1.1) - .1);

        double notifWidthWithText = Math.max(pad + fa.getStringWidth(content) + pad + texDim + pad, 100);
        double notifWidthWithoutText = pad + texDim + pad;
        double notifWidth = MathHelper.lerp(Math.max(expandAnim, 0), notifWidthWithoutText, notifWidthWithText);
        double notifHeight = getHeight();
        double rootX = x - notifWidth;
        double rootY = MathHelper.lerp(moveAnim, -notifHeight, y);
        Renderer.R2D.renderRoundedQuadWithShadow(stack, new Color(10, 10, 20), rootX, rootY, rootX + notifWidth, rootY + notifHeight, 3, 20);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        stack.push();
        stack.translate(rootX + notifWidth - pad - texDim + texDim / 2d, rootY + pad + texDim / 2d, 0);
        stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (expandAnim * 360f)));
        Texture.NOTIFICATION_TYPES.bindAndDraw(stack, -texDim / 2d, -texDim / 2d, texDim, texDim, type.i);
        stack.pop();
        ClipStack.globalInstance.addWindow(stack, new Rectangle(rootX + pad, rootY, rootX + notifWidth - pad - texDim - pad, rootY + notifHeight));
        fa.drawString(stack, content, rootX + pad, rootY + notifHeight / 2d - fa.getFontHeight() / 2d, 0xFFFFFF);
        ClipStack.globalInstance.popWindow();
    }

    public enum Type {
        SUCCESS("success.png", new Color(58, 223, 118)),
        INFO("info.png", new Color(39, 186, 253)),
        WARNING("warning.png", new Color(255, 189, 17)),
        ERROR("error.png", new Color(254, 92, 92));
        final Color c;
        final String i;

        Type(String icon, Color color) {
            this.i = icon;
            this.c = color;
        }

        public String getI() {
            return i;
        }

        public Color getC() {
            return c;
        }
    }
}
