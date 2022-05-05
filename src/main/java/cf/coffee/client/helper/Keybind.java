/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper;

import cf.coffee.client.CoffeeMain;
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
