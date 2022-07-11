/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.theme;

import java.awt.Color;

public interface Theme {
    String getName();

    Color getAccent();

    Color getModule();

    Color getConfig();

    Color getActive();

    Color getInactive();
}
