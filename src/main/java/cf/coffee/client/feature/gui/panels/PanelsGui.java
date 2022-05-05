/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.panels;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.gui.FastTickable;
import cf.coffee.client.feature.gui.panels.elements.PanelFrame;
import cf.coffee.client.feature.gui.screen.ClientScreen;
import net.minecraft.client.util.math.MatrixStack;

public class PanelsGui extends ClientScreen implements FastTickable {
    final PanelFrame[] renders;

    public PanelsGui(PanelFrame[] renders) {
        this.renders = renders;
    }

    @Override
    protected void init() {

    }

    @Override
    public void onFastTick() {
        for (PanelFrame render : renders) {
            render.onFastTick();
        }
    }

    @Override
    public void close() {
        CoffeeMain.client.setScreen(null);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {

        for (PanelFrame pf : renders) {
            pf.scroll(mouseX, mouseY, amount);
        }
        return true;
    }


    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        for (PanelFrame pf : renders) {
            pf.render(stack, mouseX, mouseY, delta);
        }
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (PanelFrame pf : renders) {
            pf.clicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (PanelFrame pf : renders) {
            pf.released();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (PanelFrame pf : renders) {
            pf.dragged(mouseX, mouseY, deltaX, deltaY, button);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (PanelFrame pf : renders) {
            pf.keyPressed(keyCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (PanelFrame pf : renders) {
            pf.charTyped(chr, modifiers);
        }
        return false;
    }
}
