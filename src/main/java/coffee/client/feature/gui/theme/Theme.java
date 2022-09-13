/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
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
