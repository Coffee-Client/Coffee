/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.gui.clickgui.element.config;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class DoubleSettingEditor extends SettingEditor<DoubleSetting> {
    boolean clicked = false;

    public DoubleSettingEditor(double x, double y, double width, DoubleSetting confValue) {
        super(x, y, width, 0, confValue);
        FontAdapter sex = FontRenderers.getCustomSize(14);
        setHeight(sex.getFontHeight() + 4);
    }

    @Override
    public void tickAnimations() {

    }

    void handleClick(double x) {
        double translated = x - (this.getPositionX());
        double perIn = MathHelper.clamp(translated / (width), 0, 1);
        configValue.setValue(Utils.Math.roundToDecimal(perIn * (configValue.getMax() - configValue.getMin()) + configValue.getMin(), configValue.getPrecision()));
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY) {
        super.render(matrices, mouseX, mouseY);
        double actualX = getPositionX();
        double actualWidth = getWidth();
        FontAdapter real = FontRenderers.getCustomSize(14);
        real.drawString(matrices, configValue.name, actualX, getPositionY(), 0xFFFFFF);
        String t = configValue.getValue().toString();
        real.drawString(matrices, t, actualX + actualWidth - real.getStringWidth(t), getPositionY(), 0xFFFFFF);
        double remainingH = getPositionY() + real.getFontHeight() + 2;
        Renderer.R2D.renderRoundedQuad(matrices, new Color(50, 50, 50), actualX, remainingH, actualX + actualWidth, remainingH + 1.5, 1.5 / 2, 5);
        Renderer.R2D.renderRoundedQuad(matrices,
            new Color(9, 162, 104),
            actualX,
            remainingH,
            actualX + Math.max(actualWidth * getPer(), 1.5),
            remainingH + 1.5,
            1.5 / 2,
            5);
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

    double getPer() {
        return MathHelper.clamp((configValue.getValue() - configValue.getMin()) / (configValue.getMax() - configValue.getMin()), 0, 1);
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
}
