/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.clickgui.element;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.impl.ConfigUtils;
import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.element.Element;
import coffee.client.feature.gui.element.impl.ButtonElement;
import coffee.client.feature.gui.element.impl.FlexLayoutElement;
import coffee.client.feature.gui.element.impl.TextFieldElement;
import coffee.client.feature.gui.notifications.hudNotif.HudNotification;
import coffee.client.helper.config.ConfigInputFile;
import coffee.client.helper.config.ConfigOutputStream;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.textures.Texture;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConfigsDisplay extends Element {
    static final double maxHeight = 300;
    private static final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
    public static ConfigsDisplay instance;
    final FlexLayoutElement layout;
    final FontAdapter titleRenderer = FontRenderers.getRenderer();
    boolean held = false;

    public ConfigsDisplay(double x, double y, double width) {
        super(x, y, width, 0);
        layout = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.DOWN, x + 2, y, width - 4, maxHeight, 2);
        reinit();
        double nh = Math.min(layout.getActualHeight(), maxHeight);
        if (nh != 0) {
            nh += 2;
        }
        setHeight(nh);
        instance = this;
    }

    public void reinit() {
        List<Element> the = new ArrayList<>();
        TextFieldElement el = new TextFieldElement(0, 0, width - 4 - 16 - 2, 16, "Name", 2);
        el.setContentFilter(s -> {
            for (char c : s.toCharArray()) {
                for (char illegalCharacter : ILLEGAL_CHARACTERS) {
                    if (illegalCharacter == c) {
                        return false;
                    }
                }
            }
            return true;
        });
        ButtonElement addButton = new ButtonElement(ButtonElement.STANDARD, 0, 0, 16, 16, "+", () -> {
            try (FileOutputStream fos = new FileOutputStream(new File(ConfigUtils.CONFIG_STORAGE, el.get().hashCode() + ".cconf"));
                ConfigOutputStream cos = new ConfigOutputStream(fos, el.get())) {
                cos.write();
                reinit();
            } catch (Exception e) {
                HudNotification.create("Failed to save config. Check logs for more info", 5000, HudNotification.Type.ERROR);
                e.printStackTrace();
            }
        }, 2);
        addButton.setEnabled(false);
        el.setChangeListener(() -> addButton.setEnabled(!el.get().isEmpty()));
        FlexLayoutElement fe = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.RIGHT, 0, 0, 2, el, addButton);
        the.add(fe);
        int clientVersion = CoffeeMain.getClientVersion();
        for (ConfigInputFile configFile : ConfigUtils.getConfigFiles()) {
            SavedConfigDisplay scd = new SavedConfigDisplay(getPositionX(), getPositionY(), width - 4, configFile.getFile(), configFile.getName(), configFile.getVersion() != clientVersion, this);
            the.add(scd);
        }
        layout.setElements(the);
    }

    @Override
    public void tickAnimations() {
        layout.tickAnimations();
    }

    double headerHeight() {
        return Math.round(titleRenderer.getFontHeight() + 5 * 2);
    }

    @Override
    public double getHeight() {
        return Math.round(headerHeight() + super.getHeight());
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        double nh = Math.min(layout.getActualHeight(), maxHeight);
        if (nh != 0) {
            nh += 2;
        }
        setHeight(nh);
        layout.updateScroller();
        Renderer.R2D.renderRoundedQuadWithShadow(stack, new Color(20, 20, 20), getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + getHeight(), 3, 10);
        double iconPad = 4;
        double iconDims = headerHeight() - iconPad * 2;
        Texture.MODULE_TYPES.bindAndDraw(stack, getPositionX() + iconPad, getPositionY() + iconPad, iconDims, iconDims, "configs.png");

        titleRenderer.drawString(stack, "Configs", (float) (getPositionX() + iconDims + iconPad * 2), (float) (getPositionY() + headerHeight() / 2d - Math.round(titleRenderer.getFontHeight()) / 2d), 1f, 1f, 1f, 1f);
        layout.setPositionX(getPositionX() + 2);
        layout.setPositionY(getPositionY() + headerHeight());
        layout.render(stack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (new Rectangle(getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + headerHeight()).contains(x, y)) {
            held = true;
            ClickGUI.instance().removeChild(this);
            ClickGUI.instance().addChild(0, this); // add to back of queue
            return true;
        }
        return layout.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (held) {
            held = false;
            return true;
        }
        return layout.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        if (held) {
            setPositionX(getPositionX() + xDelta);
            setPositionY(getPositionY() + yDelta);
            return true;
        }
        return layout.mouseDragged(x, y, xDelta, yDelta, button);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return layout.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return layout.keyPressed(keyCode, mods);
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return layout.keyReleased(keyCode, mods);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return layout.mouseScrolled(x, y, amount);
    }
}
