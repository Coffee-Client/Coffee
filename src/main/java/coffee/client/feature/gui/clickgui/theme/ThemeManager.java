/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.theme;

import coffee.client.feature.gui.clickgui.theme.impl.Custom;
import coffee.client.feature.gui.clickgui.theme.impl.Midnight;
import coffee.client.feature.gui.clickgui.theme.impl.Ocean;
import coffee.client.feature.module.ModuleRegistry;

public class ThemeManager {
    static final coffee.client.feature.module.impl.render.Theme t = ModuleRegistry.getByClass(coffee.client.feature.module.impl.render.Theme.class);
    static final Theme custom = new Custom();
    static final Theme shadow = new Midnight();
    static final Theme bestThemeEver = new Ocean();

    public static Theme getMainTheme() {
        return switch (t.modeSetting.getValue()) {
            case Ocean -> bestThemeEver;
            case Custom -> custom;
            case Midnight -> shadow;
        };
    }
}
