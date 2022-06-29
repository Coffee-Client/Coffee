/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element.impl;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.ColorSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.config.ModuleConfig;
import coffee.client.feature.config.SettingBase;
import coffee.client.feature.config.StringSetting;
import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.clickgui.element.Element;
import coffee.client.feature.gui.clickgui.element.impl.config.BooleanSettingEditor;
import coffee.client.feature.gui.clickgui.element.impl.config.ColorSettingEditor;
import coffee.client.feature.gui.clickgui.element.impl.config.ConfigBase;
import coffee.client.feature.gui.clickgui.element.impl.config.DoubleSettingEditor;
import coffee.client.feature.gui.clickgui.element.impl.config.EnumSettingEditor;
import coffee.client.feature.gui.clickgui.element.impl.config.KeybindEditor;
import coffee.client.feature.gui.clickgui.element.impl.config.StringSettingEditor;
import coffee.client.feature.gui.clickgui.theme.Theme;
import coffee.client.feature.gui.clickgui.theme.ThemeManager;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigDisplay extends Element {
    final List<ConfigBase<?>> bases = new ArrayList<>();
    final double padding = 4;
    final double paddingRight = 0;
    long hoverStart = System.currentTimeMillis();
    boolean hoveredBefore = false;

    public ConfigDisplay(double x, double y, ModuleConfig mc) {
        super(x, y, 100, 0);
        for (SettingBase<?> setting : mc.getSettings()) {

            if (setting instanceof BooleanSetting set) {
                BooleanSettingEditor bse = new BooleanSettingEditor(0, 0, width - padding * 2 - paddingRight, set);
                bases.add(bse);
            } else if (setting instanceof DoubleSetting set) {
                if (set.getName().equalsIgnoreCase("keybind")) {
                    KeybindEditor ke = new KeybindEditor(0, 0, width - padding * 2 - paddingRight, set);
                    bases.add(ke);
                } else {
                    DoubleSettingEditor dse = new DoubleSettingEditor(0, 0, width - padding * 2 - paddingRight, set);
                    bases.add(dse);
                }
            } else if (setting instanceof EnumSetting<?> set) {
                EnumSettingEditor ese = new EnumSettingEditor(0, 0, width - padding * 2 - paddingRight, set);
                bases.add(ese);
            } else if (setting instanceof StringSetting set) {
                StringSettingEditor sse = new StringSettingEditor(0, 0, width - padding * 2 - paddingRight, set);
                bases.add(sse);
            } else if (setting instanceof ColorSetting set) {
                ColorSettingEditor cse = new ColorSettingEditor(0, 0, width - padding * 2 - paddingRight, set);
                bases.add(cse);
            }
        }
        this.height = bases.stream().map(Element::getHeight).reduce(Double::sum).orElse(0d);
    }

    public List<ConfigBase<?>> getBases() {
        return bases.stream().filter(configBase -> configBase.getConfigValue().shouldShow()).collect(Collectors.toList());
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        boolean returnTrue = false;
        for (ConfigBase<?> basis : getBases()) { // notify every string setting to optionally deselect the thing
            if (basis instanceof StringSettingEditor && basis.getConfigValue().shouldShow()) {
                if (basis.clicked(x, y, button)) {
                    returnTrue = true;
                }
            }
        }
        if (returnTrue) {
            return true;
        }
        for (ConfigBase<?> basis : getBases()) {
            if (!(basis instanceof StringSettingEditor) && basis.getConfigValue().shouldShow() && basis.clicked(x, y, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        for (ConfigBase<?> basis : getBases()) {
            if (basis.getConfigValue().shouldShow() && basis.dragged(x, y, deltaX, deltaY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean released() {
        for (ConfigBase<?> basis : bases) {
            if (basis.getConfigValue().shouldShow()) {
                basis.released();
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        for (ConfigBase<?> basis : getBases()) {
            if (basis.getConfigValue().shouldShow() && basis.keyPressed(keycode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getHeight() {
        this.height = 4 + getBases().stream().map(Element::getHeight).reduce(Double::sum).orElse(0d);
        return super.getHeight();
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        double yOffset = 2;
        Theme theme = ThemeManager.getMainTheme();
        double height = getHeight();
        Renderer.R2D.renderQuad(matrices, theme.getConfig(), x, this.y, x + width, this.y + height);
        //        Renderer.R2D.renderQuad(matrices, theme.getAccent(), x, this.y, x + 1, this.y + height);
        Renderer.R2D.renderQuadGradient(
                matrices,
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 100),
                this.x,
                this.y + this.getHeight() - 10,
                this.x + this.getWidth(),
                this.y + this.getHeight(),
                true
        );
        boolean hovered = inBounds(mouseX, mouseY);
        if (!hoveredBefore && hovered) {
            hoverStart = System.currentTimeMillis();
        }
        hoveredBefore = hovered;
        String renderingDesc = null;
        for (ConfigBase<?> basis : getBases()) {
            basis.setX(x + padding);
            basis.setY(this.y + yOffset);
            if (!basis.getConfigValue().shouldShow()) {
                continue;
            }
            if (y + scrollBeingUsed > basis.getY()) {
                basis.render(matrices, mouseX, mouseY, scrollBeingUsed);
                if (basis.inBounds(mouseX, mouseY) && renderingDesc == null) {
                    renderingDesc = basis.getConfigValue().description;
                }
            }
            yOffset += basis.getHeight();
        }
        if (hoverStart + 500 < System.currentTimeMillis() && hovered && renderingDesc != null) {
            ClickGUI.instance().renderDescription(Utils.Mouse.getMouseX(), Utils.Mouse.getMouseY() + 10, renderingDesc);
        }
    }

    @Override
    public void tickAnim() {
        for (ConfigBase<?> basis : bases) {
            basis.tickAnim();
        }

    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        for (ConfigBase<?> basis : bases) {
            if (basis.scroll(mouseX, mouseY, amount)) {
                return true;
            }
        }
        return super.scroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        for (ConfigBase<?> basis : getBases()) {
            if (basis.getConfigValue().shouldShow() && basis.charTyped(c, mods)) {
                return true;
            }
        }
        return false;
    }
}
