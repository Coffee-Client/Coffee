/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.screen;

import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.feature.gui.HasSpecialCursor;
import coffee.client.helper.render.Cursor;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractParentElement {

    @Shadow
    @Final
    private List<Element> children;


    @Override
    public List<? extends Element> children() { // have to do this because java will shit itself when i dont overwrite this
        return this.children;
    }

    void handleCursor() {
        long c = Cursor.STANDARD;
        if (!SelfDestruct.shouldSelfDestruct()) {
            for (Element child : this.children) {
                if (child instanceof HasSpecialCursor specialCursor) {
                    if (specialCursor.shouldApplyCustomCursor()) {
                        c = specialCursor.getCursor();
                    }
                }
            }
        }
        Cursor.setGlfwCursor(c);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        handleCursor();
        super.mouseMoved(mouseX, mouseY);
    }
}
