/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.hud.element;

import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.font.FontRenderers;
import net.minecraft.client.util.math.MatrixStack;

public class TabGui extends HudElement {
    cf.coffee.client.feature.module.impl.render.TabGui tgui;

    public TabGui() {
        super("Tab gui", 5, 100, 0, ModuleType.values().length * FontRenderers.getRenderer().getMarginHeight() + 4);
        double longest = 0;
        for (ModuleType value : ModuleType.values()) {
            longest = Math.max(FontRenderers.getRenderer().getStringWidth(value.getName()), longest);
        }
        longest = Math.ceil(longest + 1);
        width = 2 + 1.5 + 2 + longest + 3;
    }

    cf.coffee.client.feature.module.impl.render.TabGui getTgui() {
        if (tgui == null) tgui = ModuleRegistry.getByClass(cf.coffee.client.feature.module.impl.render.TabGui.class);
        return tgui;
    }

    @Override
    public void renderIntern(MatrixStack stack) {
        stack.push();
        getTgui().render(stack);
        stack.pop();
    }
}
