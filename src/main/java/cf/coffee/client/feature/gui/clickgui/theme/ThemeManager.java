/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.clickgui.theme;

import cf.coffee.client.feature.gui.clickgui.theme.impl.Custom;
import cf.coffee.client.feature.gui.clickgui.theme.impl.Midnight;
import cf.coffee.client.feature.gui.clickgui.theme.impl.Ocean;
import cf.coffee.client.feature.module.ModuleRegistry;

public class ThemeManager {
    static final cf.coffee.client.feature.module.impl.render.Theme t = ModuleRegistry.getByClass(cf.coffee.client.feature.module.impl.render.Theme.class);
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
