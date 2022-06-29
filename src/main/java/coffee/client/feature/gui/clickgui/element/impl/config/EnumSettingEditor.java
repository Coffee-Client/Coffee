/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element.impl.config;

import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.gui.clickgui.theme.Theme;
import coffee.client.feature.gui.clickgui.theme.ThemeManager;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class EnumSettingEditor extends ConfigBase<EnumSetting<?>> {
    static final Color idk2 = new Color(0x5F060909, true);
    final List<EnumSelectorClickable<?>> values = new ArrayList<>();

    public EnumSettingEditor(double x, double y, double width, EnumSetting<?> configValue) {
        super(x, y, width, 0, configValue);
        double h = FontRenderers.getRenderer().getFontHeight() + 3;
        for (Enum<?> value : configValue.getValues()) {
            EnumSelectorClickable<?> a = new EnumSelectorClickable<>(this, 0, 0, width - 2, FontRenderers.getRenderer().getMarginHeight() + 2, value);
            values.add(a);
            h += a.height;
        }
        this.height = h + 3;
    }

    <T extends Enum<?>> int getColor(T value) {
        Theme theme = ThemeManager.getMainTheme();
        return configValue.getValue().equals(value) ? theme.getActive().getRGB() : theme.getInactive().getRGB();
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        if (inBounds(x, y)) {
            for (EnumSelectorClickable<?> value : values) {
                if (value.inBounds(x, y)) {
                    configValue.accept(value.value.name());
                }
            }
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

    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        double pad = 0;
        FontRenderers.getRenderer().drawString(matrices, configValue.name, x, y + 1, 0xFFFFFF);
        double yOffset = FontRenderers.getRenderer().getMarginHeight() + 2;
        //        Renderer.R2D.fill(matrices, new Color(0, 0, 0, 30), x, y + yOffset, x + width, y + height);
        Renderer.R2D.renderRoundedQuad(matrices, idk2, x, y + yOffset, x + width, y + height - pad, 5, 5);
        yOffset += 1;
        for (EnumSelectorClickable<?> value : values) {
            value.x = x + 1;
            value.y = this.y + yOffset;
            value.width = this.width - 2;
            value.render(matrices);
            yOffset += value.height;
        }
        this.height = yOffset + pad;
    }

    @Override
    public void tickAnim() {

    }

    @Override
    public boolean charTyped(char c, int mods) {
        return false;
    }

    static class EnumSelectorClickable<T extends Enum<?>> {
        final EnumSettingEditor instance;
        final double height;
        final T value;
        double x;
        double y;
        double width;

        public EnumSelectorClickable(EnumSettingEditor instance, double x, double y, double width, double height, T value) {
            this.instance = instance;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.value = value;
        }

        void render(MatrixStack stack) {
            FontRenderers.getRenderer()
                    .drawCenteredString(stack,
                            value.name(),
                            x + width / 2d,
                            y + height / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d,
                            instance.getColor(value)
                    );
        }

        boolean inBounds(double cx, double cy) {
            return cx >= x && cx < x + width && cy >= y && cy < y + height;
        }
    }
}
