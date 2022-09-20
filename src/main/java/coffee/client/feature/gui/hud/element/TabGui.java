/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.hud.element;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import net.minecraft.client.util.math.MatrixStack;

public class TabGui extends HudElement {
    coffee.client.feature.module.impl.render.TabGui tgui;

    public TabGui() {
        super("Tab gui", 5, 100, 0, ModuleType.values().length * FontRenderers.getRenderer().getMarginHeight() + 4);
        double longest = 0;
        for (ModuleType value : ModuleType.values()) {
            longest = Math.max(FontRenderers.getRenderer().getStringWidth(value.getName()), longest);
        }
        longest = Math.ceil(longest + 1);
        width = 2 + 1.5 + 2 + longest + 3;
    }

    coffee.client.feature.module.impl.render.TabGui getTgui() {
        if (tgui == null) {
            tgui = ModuleRegistry.getByClass(coffee.client.feature.module.impl.render.TabGui.class);
        }
        return tgui;
    }

    @Override
    public void renderIntern(MatrixStack stack) {
        stack.push();
        getTgui().render(stack);
        stack.pop();
    }
}
