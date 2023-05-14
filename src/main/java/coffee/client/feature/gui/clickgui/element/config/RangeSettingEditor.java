/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.clickgui.element.config;

import coffee.client.feature.config.RangeSetting;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class RangeSettingEditor extends SettingEditor<RangeSetting> {
    boolean clickedA = false, clickedB = false;

    public RangeSettingEditor(double x, double y, double width, RangeSetting confValue) {
        super(x, y, width, 0, confValue);
        FontAdapter sex = FontRenderers.getCustomSize(14);
        setHeight(sex.getFontHeight() + 4);
    }

    @Override
    public void tickAnimations() {

    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY) {
        super.render(matrices, mouseX, mouseY);
        double actualX = getPositionX();
        double actualWidth = getWidth();
        FontAdapter real = FontRenderers.getCustomSize(14);
        real.drawString(matrices, configValue.name, actualX, getPositionY(), 0xFFFFFF);
        String t = configValue.getValue().getMin() + " - " + configValue.getValue().getMax();
        real.drawString(matrices, t, actualX + actualWidth - real.getStringWidth(t), getPositionY(), 0xFFFFFF);
        double remainingH = getPositionY() + real.getFontHeight() + 2;
        Renderer.R2D.renderRoundedQuad(matrices, new Color(50, 50, 50), actualX, remainingH, actualX + actualWidth, remainingH + 1.5, 1.5 / 2, 5);
        double xA = getPerA() * actualWidth;
        double xB = getPerB() * actualWidth;
        double barWidth = xB - xA;
        barWidth = Math.max(barWidth, 1.5);
        Renderer.R2D.renderRoundedQuad(matrices, new Color(9, 162, 104), actualX + xA, remainingH, actualX + xA + barWidth, remainingH + 1.5, 1.5 / 2, 5);
    }

    void handleClickA(double translated) {
        double cumMin = Math.min(configValue.getMin(), configValue.getMin1());
        double cumMax = Math.max(configValue.getMax(), configValue.getMax1());
        double perIn = MathHelper.clamp(translated / (getWidth()), 0, 1);
        double v = MathHelper.lerp(perIn, cumMin, cumMax);
        double b = configValue.getValue().getMax();
        double p = Utils.Math.roundToDecimal(v, configValue.getPrecision());

        if (p > b) {
            b = p;
        }

        configValue.setValue(new RangeSetting.Range(p, b));
    }

    void handleClickB(double translated) {
        double cumMin = Math.min(configValue.getMin(), configValue.getMin1());
        double cumMax = Math.max(configValue.getMax(), configValue.getMax1());
        double perIn = MathHelper.clamp(translated / (getWidth()), 0, 1);
        double v = MathHelper.lerp(perIn, cumMin, cumMax);
        double a = configValue.getValue().getMin();
        double p = Utils.Math.roundToDecimal(v, configValue.getPrecision());
        if (p < a) {
            a = p;
        }
        configValue.setValue(new RangeSetting.Range(a, p));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (inBounds(x, y)) {
            if (button == 0) {
                double actualWidth = getWidth();
                double xA = getPerA() * actualWidth;
                double xB = getPerB() * actualWidth;
                double acceptableDelta = 6;
                double translated = x - getPositionX();
                if (Math.abs(xA - translated) < acceptableDelta) {
                    this.clickedA = true;
                    handleClickA(translated);
                } else if (Math.abs(xB - translated) < acceptableDelta) {
                    this.clickedB = true;
                    handleClickB(translated);
                }
            }
            return true;
        }
        return false;
    }

    double getPerA() {
        double cumMin = Math.min(configValue.getMin(), configValue.getMin1());
        double cumMax = Math.max(configValue.getMax(), configValue.getMax1());
        return MathHelper.clamp((configValue.getValue().getMin() - cumMin) / (cumMax - cumMin), 0, 1);
    }

    double getPerB() {
        double cumMin = Math.min(configValue.getMin(), configValue.getMin1());
        double cumMax = Math.max(configValue.getMax(), configValue.getMax1());
        return MathHelper.clamp((configValue.getValue().getMax() - cumMin) / (cumMax - cumMin), 0, 1);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (clickedA || clickedB) {
            clickedA = clickedB = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        if (clickedA || clickedB) {
            double t = x - getPositionX();
            if (clickedA) {
                handleClickA(t);
            } else {
                handleClickB(t);
            }
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
