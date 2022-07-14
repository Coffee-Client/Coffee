/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.gui.screen.base.ClientScreen;
import coffee.client.feature.gui.widget.RoundButton;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class NotificationScreen extends ClientScreen implements FastTickable {
    final String t;
    final Notification.Type ty;
    final Screen parent;
    final Color bg = new Color(20, 20, 20, 100);
    boolean closing = false;
    double anim = 0;

    public NotificationScreen(Screen parent, String text, Notification.Type type) {
        this.ty = type;
        this.t = text;
        this.parent = parent;
    }

    @Override
    public void onFastTick() {
        double d = 0.05;
        if (closing) {
            d *= -1;
        }
        anim += d;
        anim = MathHelper.clamp(anim, 0, 1);
    }

    @Override
    protected void init() {
        double height = 5 + 32 + 5 + FontRenderers.getRenderer().getMarginHeight() + 5 + 20 + 5;
        double w = Math.max(120, FontRenderers.getRenderer().getStringWidth(t));
        RoundButton rb = new RoundButton(RoundButton.STANDARD,
                width / 2d - w / 2d,
                this.height / 2d - height / 2d + height - 5 - 20,
                w,
                20,
                "Close",
                this::close
        );
        addDrawableChild(rb);
    }

    @Override
    public void close() {
        closing = true;
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        double x = anim;
        double anim = x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2;
        if (parent != null) {
            parent.render(stack, mouseX, mouseY, delta);
        }
        Renderer.R2D.renderQuad(stack, Renderer.Util.modify(bg, -1, -1, -1, (int) (160 * anim)), 0, 0, width, height);
        stack.push();
        stack.translate(width / 2d * (1 - anim), height / 2d * (1 - anim), 10);
        stack.scale((float) anim, (float) anim, 1);

        double height = 5 + 32 + 5 + FontRenderers.getRenderer().getMarginHeight() + 5 + 20 + 5;
        double w = Math.max(120, FontRenderers.getRenderer().getStringWidth(t)) + 10;
        Renderer.R2D.renderRoundedQuad(stack,
                new Color(20, 20, 20, 255),
                (width - w) / 2d,
                (this.height - height) / 2d,
                (width - w) / 2d + w,
                (this.height - height) / 2d + height,
                5,
                20
        );
        Color p = ty.getC();
        RenderSystem.setShaderColor(p.getRed() / 255f, p.getGreen() / 255f, p.getBlue() / 255f, p.getAlpha() / 255f);
        RenderSystem.setShaderTexture(0, ty.getI());
        Renderer.R2D.renderTexture(stack, width / 2d - 32 / 2d, this.height / 2d - height / 2d + 5, 32, 32, 0, 0, 32, 32, 32, 32);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        FontRenderers.getRenderer()
                .drawCenteredString(stack,
                        t,
                        width / 2d,
                        this.height / 2d + height / 2d - 5 - 20 - 5 - FontRenderers.getRenderer().getMarginHeight(),
                        0xFFFFFF
                );
        if (closing && anim == 0) {
            client.setScreen(parent);
        }

        super.renderInternal(stack, mouseX, mouseY, delta);
        stack.pop();
    }
}
