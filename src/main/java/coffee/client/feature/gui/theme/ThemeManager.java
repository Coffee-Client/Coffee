/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.theme;

import coffee.client.feature.gui.theme.impl.Ocean;

public class ThemeManager {
    static final Theme bestThemeEver = new Ocean();

    public static Theme getMainTheme() {
        return bestThemeEver;
    }
}
