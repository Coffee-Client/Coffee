/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.panels.elements;

import cf.coffee.client.feature.gui.clickgui.element.Element;
import cf.coffee.client.helper.font.FontRenderers;
import cf.coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class PanelButton extends Element {
    static final double h = FontRenderers.getRenderer().getFontHeight() + 2;
    final Runnable code;
    final String title;


    public PanelButton(double x, double y, double width, String title, Runnable code) {
        super(x, y, width, h);
        this.code = code;
        this.title = title;
    }

    @Override
    public double getHeight() {
        return h;
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        if (inBounds(x, y)) {
            code.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        return false;
    }

    @Override
    public boolean released() {
        return false;
    }

    //    long lastUpdate = System.currentTimeMillis();
    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        Renderer.R2D.renderRoundedQuad(matrices, inBounds(mouseX, mouseY) ? new Color(25, 25, 25) : new Color(40, 40, 40), x, y, x + width, y + h, 5, 20);
        FontRenderers.getRenderer().drawCenteredString(matrices, title, x + width / 2d, y + h / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d, 1f, 1f, 1f, 1f);
    }

    @Override
    public void tickAnim() {

    }


    @Override
    public boolean charTyped(char c, int mods) {
        return false;
    }
}
