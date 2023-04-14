/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.hud.HudRenderer;
import coffee.client.feature.gui.screen.base.ClientScreen;

/**
 * Placeholder class with no elements, used for instanceof in {@link HudRenderer#render()}
 */
public class HudEditorScreen extends ClientScreen {
    @Override
    public boolean shouldPause() {
        return false;
    }
}
