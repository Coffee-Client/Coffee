/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.clickgui.element.config;

import coffee.client.feature.config.StringSetting;
import coffee.client.feature.gui.element.impl.TextFieldElement;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import net.minecraft.client.util.math.MatrixStack;

public class StringSettingEditor extends SettingEditor<StringSetting> {
    static final FontAdapter fa = FontRenderers.getCustomSize(14);
    final TextFieldElement rtfw;

    public StringSettingEditor(double x, double y, double width, StringSetting confValue) {
        super(x, y, width, fa.getFontHeight() + 20, confValue);
        FontAdapter def = FontRenderers.getRenderer();
        this.rtfw = new TextFieldElement(x, y + fa.getFontHeight(), width, def.getFontHeight() + 2, "");
        this.rtfw.set(confValue.getValue());
        this.rtfw.setChangeListener(() -> this.configValue.setValue(this.rtfw.get()));
        this.setHeight(fa.getFontHeight() + this.rtfw.getHeight());
    }

    @Override
    public void tickAnimations() {
        this.rtfw.tickAnimations();
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        super.render(stack, mouseX, mouseY);
        fa.drawString(stack, configValue.name, getPositionX(), getPositionY(), 0xFFFFFF);
        this.rtfw.setPositionX(getPositionX());
        this.rtfw.setPositionY(getPositionY() + fa.getFontHeight());
        this.rtfw.render(stack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return this.rtfw.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return rtfw.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        return rtfw.mouseDragged(x, y, xDelta, yDelta, button);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return rtfw.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return rtfw.keyPressed(keyCode, mods);
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return rtfw.keyReleased(keyCode, mods);
    }
}
