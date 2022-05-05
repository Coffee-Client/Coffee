/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.clickgui.theme;

import java.awt.Color;

public interface Theme {
    String getName();

    Color getAccent();

    Color getHeader();

    Color getModule();

    Color getConfig();

    Color getActive();

    Color getInactive();
}
