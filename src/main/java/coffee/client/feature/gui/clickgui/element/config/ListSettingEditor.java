/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.clickgui.element.config;

import coffee.client.feature.config.ListSetting;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ListSettingEditor extends SettingEditor<ListSetting<?>> {

    static final FontAdapter fa = FontRenderers.getCustomSize(14);
    final double headerPad = 4;
    final List<Enum<?>> ese = new ArrayList<>();
    double expandProg = 0;
    boolean expanded = false;

    public ListSettingEditor(double x, double y, double width, ListSetting<?> confValue) {
        super(x, y, width, 0, confValue);

        this.ese.addAll(confValue.getAllValues());
        setHeight(fa.getFontHeight() + headerPad);
    }

    double headerHeight() {
        return fa.getFontHeight() + 4;
    }

    @Override
    public double getHeight() {
        return headerHeight() + (ese.size() * (fa.getFontHeight() + 2)) * Transitions.easeOutExpo(expandProg);
    }

    @Override
    public void tickAnimations() {
        double d = 0.02;
        if (!expanded) {
            d *= -1;
        }
        expandProg += d;
        expandProg = MathHelper.clamp(expandProg, 0, 1);
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        super.render(stack, mouseX, mouseY);
        Renderer.R2D.renderRoundedQuad(stack, new Color(30, 30, 30), getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + getHeight(), 2, 8);
        fa.drawString(stack, configValue.name, getPositionX() + headerPad / 2d, getPositionY() + headerHeight() / 2d - fa.getFontHeight() / 2d, 0xFFFFFF);
        float delta = (float) Transitions.easeOutExpo(expandProg);
        double rD = 6 + delta * 2;
        Renderer.R2D.renderCheckmark(stack,
            Color.WHITE,
            getPositionX() + getWidth() - headerPad / 2d - rD / 2d,
            getPositionY() + headerHeight() / 2d - delta * 1.5,
            5,
            5,
            .5f,
            MathHelper.lerp(delta, 45, -45));
        if (expandProg != 0) {

            ClipStack.globalInstance.addWindow(stack,
                new Rectangle(getPositionX(), getPositionY() + headerHeight() - 1, getPositionX() + getWidth(), getPositionY() + getHeight()));

            double offsetY = 1;
            double indicatorWid = fa.getFontHeight();
            for (Enum<?> enumSettingEntry : ese) {
                Renderer.R2D.renderRoundedQuad(stack,
                    new Color(40, 40, 40),
                    getPositionX() + 1,
                    getPositionY() + headerHeight() + offsetY,
                    getPositionX() + 1 + indicatorWid,
                    getPositionY() + headerHeight() + offsetY + fa.getFontHeight(),
                    2,
                    5);
                if (configValue.getValue().getChecked().contains(enumSettingEntry)) {
                    Renderer.R2D.renderCheckmark(stack,
                        new Color(9, 162, 104),
                        getPositionX() + 1 + indicatorWid / 2,
                        getPositionY() + headerHeight() + offsetY + fa.getFontHeight() / 2d - .5,
                        3,
                        5,
                        .5f,
                        -45);
                }
                fa.drawString(stack, enumSettingEntry.name(), getPositionX() + indicatorWid + 4, getPositionY() + headerHeight() + offsetY, 0xFFFFFF);
                offsetY += fa.getFontHeight() + 2;
            }

            ClipStack.globalInstance.popWindow();
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean mouseClicked(double x, double y, int button) {
        if (new Rectangle(getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + headerHeight()).contains(x, y)) {
            expanded = !expanded;
            return true;
        } else if (new Rectangle(getPositionX(), getPositionY() + headerHeight(), getPositionX() + getWidth(), getPositionY() + getHeight()).contains(x, y) && expanded) {
            double offsetY = 0;
            for (Enum<?> enumSettingEntry : ese) {
                double startY = getPositionY() + headerHeight() + offsetY;
                double endY = getPositionY() + headerHeight() + offsetY + fa.getFontHeight() + 2;
                if (startY <= y && endY > y) {
                    // NEVER DO THIS, EVER
                    List value = configValue.getValue().getChecked();
                    if (value.contains(enumSettingEntry)) {
                        value.remove(enumSettingEntry);
                    } else {
                        value.add(enumSettingEntry);
                    }
                    configValue.getValue().setChecked(value);
                    break; // found our value, cancel
                }
                offsetY += fa.getFontHeight() + 2;
            }
            return true;
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
}
