/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.clickgui.element.config;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class BooleanSettingEditor extends SettingEditor<BooleanSetting> {
    static final FontAdapter theReal = FontRenderers.getCustomSize(14);
    double anim = 0;

    public BooleanSettingEditor(double x, double y, double width, BooleanSetting confValue) {
        super(x, y, width, theReal.getFontHeight(), confValue);
    }

    @Override
    public void tickAnimations() {
        double delta = 0.02;
        if (!configValue.getValue()) {
            delta *= -1;
        }
        anim += delta;
        anim = MathHelper.clamp(anim, 0, 1);
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        super.render(stack, mouseX, mouseY);
        theReal.drawString(stack, configValue.name, getPositionX(), getPositionY(), 0xFFFFFF);
        double bruhHeight = 3;
        double bruhWidth = 12;
        double ballDim = bruhHeight - 1;
        double ballXStart = getPositionX() + getWidth() - bruhWidth + ballDim + 1;
        double ballXEnd = getPositionX() + getWidth() - ballDim - 1;
        double d = Transitions.easeOutExpo(anim);
        double ballX = MathHelper.lerp(d, ballXStart, ballXEnd);
        Renderer.R2D.renderRoundedQuad(stack,
            Renderer.Util.lerp(new Color(50, 50, 50), new Color(2, 2, 2), d),
            getPositionX() + getWidth() - bruhWidth,
            getPositionY() + getHeight() / 2d - bruhHeight,
            getPositionX() + getWidth(),
            getPositionY() + getHeight() / 2d + bruhHeight,
            bruhHeight,
            15);
        Renderer.R2D.renderCircle(stack, new Color(9, 162, 104), ballX, getPositionY() + getHeight() / 2d, ballDim, 20);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (inBounds(x, y)) {
            configValue.setValue(!configValue.getValue());
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
}
