/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper.manager;

import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleRegistry;

/**
 * The keybind manager
 */
public class KeybindingManager {

    /**
     * Update a single keybind via keyboard event
     *
     * @param kc     The key which was changed
     * @param action The action performed (0 = release, 1 = pressed, 2 = repeat pressed when holding)
     */
    public static void updateSingle(int kc, int action) {
        if (kc == -1) {
            return; // JESSE WE FUCKED UP
        }
        if (action == 1) { // key pressed
            for (Module module : ModuleRegistry.getModules()) {
                if (module.keybind.getValue() == kc) {
                    module.toggle();
                }
            }
        }
    }

}
