/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.clickgui.element;

import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.element.Element;
import coffee.client.feature.gui.element.impl.TexturedButtonElement;
import coffee.client.feature.gui.notifications.hudNotif.HudNotification;
import coffee.client.helper.config.ConfigInputFile;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.textures.Texture;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.io.File;

public class SavedConfigDisplay extends Element {
    ConfigsDisplay parent;
    File configPath;
    String configName;
    TexturedButtonElement delete;
    boolean warn;

    public SavedConfigDisplay(double x, double y, double width, File configPath, String name, boolean showWarning, ConfigsDisplay parent) {
        super(x, y, width, 20);
        this.parent = parent;
        this.configPath = configPath;
        this.configName = name;
        this.warn = showWarning;
        this.delete = new TexturedButtonElement(new Color(255, 70, 70), 0, 0, 16, 16, () -> {
            // SHUT THE FUCK UP
            //noinspection ResultOfMethodCallIgnored
            configPath.delete();
            parent.reinit();
        }, TexturedButtonElement.IconRenderer.fromSpritesheet(Texture.MODULE_TYPES, "delete.png"));
    }

    @Override
    public void tickAnimations() {

    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        boolean hovered = false;
        if (new Rectangle(getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + getHeight()).contains(mouseX, mouseY)) {
            int parentIndex = ClickGUI.instance().getIndex(parent); // get index of current element (index 0 = last in render queue)
            if (parentIndex >= 0) { // was the element found? (this should always be yes)
                boolean render = true;
                if (parentIndex > 0) { // are we *NOT* first in queue?
                    for (int i = parentIndex - 1; i >= 0; i--) { // go over all the elements before us
                        Element e = ClickGUI.instance().getChild(i);
                        if (e.inBounds(mouseX, mouseY)) { // is the mouse over said element before us? (will be above us in rendering)
                            render = false; // dont render our tooltip and selection
                            break;
                        }
                    }
                }
                if (render) { // should we render our tooltip and selection?
                    Renderer.R2D.renderRoundedQuad(
                        stack,
                        new Color(255, 255, 255, 30),
                        getPositionX(),
                        getPositionY(),
                        getPositionX() + getWidth(),
                        getPositionY() + getHeight(),
                        2,
                        10
                    );
                    hovered = true;
                }
            }
        }
        String renderableConfigName = Utils.capAtLength(configName, getWidth() - this.delete.getWidth() - 4, FontRenderers.getRenderer());
        FontRenderers.getRenderer()
                     .drawString(stack, renderableConfigName, getPositionX() + 2, getPositionY() + getHeight() / 2d - FontRenderers.getRenderer().getFontHeight() / 2d, 0xFFFFFF);
        this.delete.setPositionX(getPositionX() + getWidth() - this.delete.getWidth() - 2);
        this.delete.setPositionY(getPositionY() + 2);
        if (hovered) {
            this.delete.render(stack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (inBounds(x, y) && button == 0) {
            if (!this.delete.inBounds(x, y)) {
                try {
                    new ConfigInputFile(this.configPath).apply();
                } catch (Exception e) {
                    HudNotification.create("Failed to load config. Check logs for more info", 5000, HudNotification.Type.ERROR);
                    e.printStackTrace();
                }
                return true;
            } else {
                return this.delete.mouseClicked(x, y, button);
            }
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

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return false;
    }
}
