/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.gui.HasSpecialCursor;
import cf.coffee.client.helper.render.Cursor;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.Color;
import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractParentElement {
    private static final Color c = new Color(10, 10, 10);
    @Shadow
    public int height;

    @Shadow
    public int width;

    @Shadow
    @Final
    private List<Element> children;

    @Shadow
    protected abstract void insertText(String text, boolean override);


    @Override
    public List<? extends Element> children() { // have to do this because java will shit itself when i dont overwrite this
        return this.children;
    }

    void shadow_handleCursor(double x, double y) {
        long c = Cursor.STANDARD;
        for (Element child : this.children) {
            if (child instanceof HasSpecialCursor specialCursor) {
                if (specialCursor.shouldApplyCustomCursor()) c = specialCursor.getCursor();
            }
        }
        Cursor.setGlfwCursor(c);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        shadow_handleCursor(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }
}
