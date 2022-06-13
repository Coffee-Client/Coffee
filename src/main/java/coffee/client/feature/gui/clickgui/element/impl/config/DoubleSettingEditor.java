/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element.impl.config;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.gui.clickgui.theme.Theme;
import coffee.client.feature.gui.clickgui.theme.ThemeManager;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class DoubleSettingEditor extends ConfigBase<DoubleSetting> {

    boolean clicked = false;

    public DoubleSettingEditor(double x, double y, double width, DoubleSetting configValue) {
        super(x, y, width, FontRenderers.getRenderer().getMarginHeight() + 10, configValue);
    }

    void handleClick(double x) {
        double translated = x - this.x;
        double perIn = MathHelper.clamp(translated / width, 0, 1);
        configValue.setValue(
                Utils.Math.roundToDecimal(perIn * (configValue.getMax() - configValue.getMin()) + configValue.getMin(), configValue.getPrecision()));
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        if (inBounds(x, y)) {
            clicked = true;
            if (button == 0) {
                handleClick(x);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        if (clicked) {
            handleClick(x);
        }
        return false;
    }

    @Override
    public boolean released() {
        clicked = false;
        return false;
    }

    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        return false;
    }

    double getPer() {
        return MathHelper.clamp((configValue.getValue() - configValue.getMin()) / (configValue.getMax() - configValue.getMin()), 0, 1);
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        Theme theme = ThemeManager.getMainTheme();
        FontRenderers.getRenderer().drawString(matrices, configValue.name, x, y, 0xFFFFFF);
        String t = configValue.getValue().toString();
        FontRenderers.getRenderer().drawString(matrices, t, x + width - FontRenderers.getRenderer().getStringWidth(t), y, 0xFFFFFF);
        double h = y + FontRenderers.getRenderer().getMarginHeight() + .5; // 9 px left
        Renderer.R2D.renderQuad(matrices, theme.getInactive(), x, h + 9 / 2d - .5, x + width, h + 9 / 2d);
        Renderer.R2D.renderQuad(matrices, theme.getActive(), x, h + 9 / 2d - .5, x + width * getPer(), h + 9 / 2d);
        //        Renderer.R2D.fill(matrices, theme.getAccent(), x + width * getPer() - .5, h + .5, x + width * getPer() + .5, h + 8.5);
        Renderer.R2D.renderCircle(matrices, theme.getAccent(), x + width * getPer(), h + 9 / 2d, 2, 10);
    }

    @Override
    public void tickAnim() {

    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        if (inBounds(mouseX, mouseY)) {
            double delta = 1 / (double) (configValue.getPrecision() + 1);
            delta *= amount;
            configValue.setValue(MathHelper.clamp(Utils.Math.roundToDecimal(configValue.getValue() + delta, configValue.getPrecision()), configValue.getMin(),
                    configValue.getMax()));
            return true;
        }
        return super.scroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return false;
    }
}
