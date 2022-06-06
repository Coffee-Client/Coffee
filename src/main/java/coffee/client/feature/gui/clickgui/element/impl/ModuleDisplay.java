/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element.impl;

import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.clickgui.element.Element;
import coffee.client.feature.gui.clickgui.theme.Theme;
import coffee.client.feature.gui.clickgui.theme.ThemeManager;
import coffee.client.feature.module.Module;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class ModuleDisplay extends Element {
    @Getter
    final Module module;
    final ConfigDisplay cd;
    @Getter
    @Setter
    boolean extended = false;
    double extendAnim = 0;
    long hoverStart = System.currentTimeMillis();
    boolean hoveredBefore = false;

    public ModuleDisplay(double x, double y, Module module) {
        super(x, y, 100, 15);
        this.module = module;
        this.cd = new ConfigDisplay(x, y, module.config);
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        if (inBounds(x, y)) {
            if (button == 0) {
                module.setEnabled(!module.isEnabled()); // left click
            } else if (button == 1) {
                extended = !extended;
            } else {
                return false;
            }
            return true;
        } else {
            return extended && cd.clicked(x, y, button);
        }
    }

    @Override
    public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        return extended && cd.dragged(x, y, deltaX, deltaY, button);
    }

    @Override
    public boolean released() {
        return extended && cd.released();
    }

    double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;

    }

    @Override
    public double getHeight() {
        return super.getHeight() + cd.getHeight() * easeInOutCubic(extendAnim);
    }

    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        return extended && cd.keyPressed(keycode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {

        Theme theme = ThemeManager.getMainTheme();
        boolean hovered = inBounds(mouseX, mouseY);
        if (!hoveredBefore && hovered) {
            hoverStart = System.currentTimeMillis();
        }
        if (hoverStart + 500 < System.currentTimeMillis() && hovered) {
            ClickGUI.instance()
                    .renderDescription(Utils.Mouse.getMouseX(), Utils.Mouse.getMouseY() + 10, module.getDescription());
        }
        hoveredBefore = hovered;
        Renderer.R2D.renderQuad(matrices,
                hovered ? theme.getModule().darker() : theme.getModule(),
                x,
                y,
                x + width,
                y + height);
        FontRenderers.getRenderer()
                .drawCenteredString(matrices,
                        module.getName(),
                        x + width / 2d,
                        y + height / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d,
                        0xFFFFFF);
        if (module.isEnabled()) {
            double wid = 1.5;
            Renderer.R2D.renderRoundedQuad(matrices,
                    theme.getAccent(),
                    x + 1,
                    y + 1,
                    x + 1 + wid,
                    y + height - 1,
                    wid / 2d,
                    6);
        }
        cd.setX(this.x);
        cd.setY(this.y + height);
        ClipStack.globalInstance.addWindow(matrices, new Rectangle(x, y, x + width, y + getHeight()));
        if (extendAnim > 0) {
            cd.render(matrices, mouseX, mouseY, getHeight() - super.getHeight());
        }
        ClipStack.globalInstance.popWindow();
    }

    @Override
    public void tickAnim() {
        double a = 0.04;
        if (!extended) {
            a *= -1;
        }
        extendAnim += a;
        extendAnim = MathHelper.clamp(extendAnim, 0, 1);
        cd.tickAnim();
    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        if (extendAnim > 0) {
            if (cd.scroll(mouseX, mouseY, amount)) {
                return true;
            }
        }
        return super.scroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return extended && cd.charTyped(c, mods);
    }
}
