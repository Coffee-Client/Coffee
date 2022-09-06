/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.clickgui.element;

import coffee.client.feature.config.*;
import coffee.client.feature.gui.clickgui.element.config.*;
import coffee.client.feature.gui.element.Element;
import coffee.client.feature.gui.element.impl.FlexLayoutElement;
import coffee.client.feature.gui.element.impl.TextElement;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigDisplay extends Element {
    @SuppressWarnings("rawtypes")
    static final Map<Class<? extends SettingBase>, Class<? extends Element>> settingTypeMappings = Util.make(new HashMap<>(), classClassHashMap -> {
        classClassHashMap.put(BooleanSetting.class, BooleanSettingEditor.class);
        classClassHashMap.put(EnumSetting.class, EnumSettingEditor.class);
        classClassHashMap.put(DoubleSetting.class, DoubleSettingEditor.class);
        classClassHashMap.put(StringSetting.class, StringSettingEditor.class);
        classClassHashMap.put(RangeSetting.class, RangeSettingEditor.class);
    });
    final double leftPadding = 6;
    boolean expanded = false;
    double progress = 0;
    FlexLayoutElement fle;

    public ConfigDisplay(double x, double y, double width, ModuleConfig mconf) {
        super(x, y, width, 0);
        List<Element> daConfigs = new ArrayList<>();
        for (SettingBase<?> setting : mconf.getSettings()) {
            if (setting.getName().equals("Keybind")) {
                continue;
            }
            Class<? extends Element> elementToCreate = settingTypeMappings.get(setting.getClass());
            if (elementToCreate == null) {
                continue;
            }
            try {
                Constructor<? extends Element> constructor = elementToCreate.getConstructor(
                    // double x, double y, double width, ? extends SettingBase<?> confValue
                    double.class, double.class, double.class, setting.getClass());
                Element e = constructor.newInstance(0, 0, width - leftPadding, setting);
                daConfigs.add(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        FontAdapter fnt = FontRenderers.getCustomSize(14);
        TextElement nothing = new TextElement(fnt,
            "Nothing to see here",
            new Color(0.7f, 0.7f, 0.7f),
            true,
            getPositionX(),
            getPositionY(),
            getWidth(),
            fnt.getFontHeight() + 2);
        if (daConfigs.isEmpty()) {
            daConfigs.add(nothing);
        }
        fle = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.DOWN, x + leftPadding, y, width - leftPadding, -1, 5, daConfigs.toArray(Element[]::new));
        this.setHeight(fle.getHeight());
    }

    @Override
    public void tickAnimations() {
        double progDelta = 0.02;
        if (!expanded) {
            progDelta *= -1;
        }
        progress += progDelta;
        progress = MathHelper.clamp(progress, 0, 1);
        fle.tickAnimations();
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        fle.updateScroller();
        fle.setHeight(fle.getActualHeight());
        setHeight(fle.getActualHeight());
        double height1 = getHeight();
        if (height1 > 1.5) {
            Renderer.R2D.renderRoundedQuad(stack,
                new Color(9, 162, 104),
                getPositionX() + 2,
                getPositionY(),
                getPositionX() + 3.5,
                getPositionY() + height1,
                1.5 / 2d,
                2);
        }
        fle.setPositionX(getPositionX() + leftPadding);
        fle.setPositionY(getPositionY());
        fle.setHeightMulMatrix(new Vec2f(1, (float) Transitions.easeOutExpo(progress)));
        fle.render(stack, mouseX, mouseY);
    }

    @Override
    public double getHeight() {
        return super.getHeight() * Transitions.easeOutExpo(progress);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return fle.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return fle.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        return fle.mouseDragged(x, y, xDelta, yDelta, button);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return fle.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return fle.keyPressed(keyCode, mods);
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return fle.keyReleased(keyCode, mods);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return false; // we don't need this shit
    }
}
