/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.element.impl.ButtonElement;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.manager.ShaderManager;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class NotificationScreen extends AAScreen {
    final Notification.Type icon;
    final String title;
    final String[] contentSplit;
    final int maxWidth = 200;
    final int pad = 5;
    final Screen parent;
    ButtonElement ok;
    boolean closing = false;
    double anim = 0;

    public NotificationScreen(Notification.Type icon, String title, String content, Screen parent) {
        this.title = title;
        this.icon = icon;
        contentSplit = Utils.splitLinesToWidth(content, maxWidth - pad * 2, FontRenderers.getRenderer());
        this.parent = parent;
    }

    @Override
    protected void initInternal() {
        closing = false;
        anim = 0;
        this.ok = new ButtonElement(ButtonElement.STANDARD, 0, 0, 0, 20, "Close", this::close);
        addChild(this.ok);
    }

    @Override
    public void onFastTick() {
        double delta = 0.04;
        if (closing) {
            delta *= -1;
        }
        anim += delta;
        anim = MathHelper.clamp(anim, 0, 1);
        super.onFastTick();
    }

    @Override
    public void close() {
        closing = true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double anim = Transitions.easeOutExpo(this.anim);
        if (anim == 0 && closing) {
            client.setScreen(parent);
            return;
        }
        if (parent != null) {
            parent.render(matrices, mouseX, mouseY, delta);
        }
        ShaderManager.BLUR.getEffect().setUniformValue("progress", (float) anim);
        ShaderManager.BLUR.render(delta);
        matrices.push();
        matrices.translate(this.width / 2d * (1 - anim), this.height / 2d * (1 - anim), 0);
        matrices.scale((float) anim, (float) anim, 1);
        super.render(matrices, mouseX, mouseY, delta);
        matrices.pop();
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        //        double anim = Transitions.easeOutExpo(this.anim);
        double width = 0;
        double headheight = FontRenderers.getRenderer().getFontHeight();
        double height = pad + headheight + pad + FontRenderers.getRenderer().getFontHeight() * contentSplit.length + pad + 20 + pad;
        for (String s : contentSplit) {
            width = Math.max(FontRenderers.getRenderer().getStringWidth(s), width);
        }
        width += pad * 2;
        //        width = Math.max(width, 100);
        double startX = this.width / 2d - width / 2d;
        double startY = this.height / 2d - height / 2d;
        Renderer.R2D.renderRoundedQuadWithShadow(stack, new Color(20, 20, 20), startX, startY, startX + width, startY + height, 5, 10);
        double texDims = 12;
        RenderSystem.setShaderTexture(0, icon.getI());
        Color c = icon.getC();
        RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f);
        Renderer.R2D.renderTexture(stack, startX + pad, startY + pad + (headheight) / 2d - texDims / 2d, texDims, texDims, 0, 0, texDims, texDims, texDims, texDims);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        FontRenderers.getRenderer().drawString(stack, title, startX + pad + texDims + pad, startY + pad + (headheight) / 2d - FontRenderers.getRenderer().getFontHeight() / 2d, 0xCCCCCC);
        double yOffset = 0;
        for (String s : contentSplit) {
            FontRenderers.getRenderer().drawString(stack, s, startX + pad, startY + FontRenderers.getRenderer().getFontHeight() + pad * 2 + yOffset, 0xFFFFFF);
            yOffset += FontRenderers.getRenderer().getFontHeight();
        }
        this.ok.setPositionX(startX + pad);
        this.ok.setPositionY(startY + FontRenderers.getRenderer().getFontHeight() + pad * 2 + FontRenderers.getRenderer().getFontHeight() * contentSplit.length + pad);
        this.ok.setWidth(width - pad * 2);

        super.renderInternal(stack, mouseX, mouseY, delta);
    }
}
