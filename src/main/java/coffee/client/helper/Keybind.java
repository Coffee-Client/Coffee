/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper;

import coffee.client.CoffeeMain;
import net.minecraft.client.util.InputUtil;

public record Keybind(int keycode) {

    public boolean isPressed() {
        if (keycode < 0) {
            return false;
        }
        boolean isActuallyPressed = InputUtil.isKeyPressed(CoffeeMain.client.getWindow().getHandle(), keycode);
        return CoffeeMain.client.currentScreen == null && isActuallyPressed;
    }
}
