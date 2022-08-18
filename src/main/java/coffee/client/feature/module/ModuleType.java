/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module;

public enum ModuleType {
    RENDER("Render", "render.png"),
    MOVEMENT("Movement", "movement.png"),
    MISC("Miscellaneous", "misc.png"),
    WORLD("World", "world.png"),
    EXPLOIT("Exploit", "exploit.png"),
    ADDON_PROVIDED("Addons", "addons.png"),
    COMBAT("Combat", "combat.png");


    final String name;
    final String tex;

    ModuleType(String n, String tex) {
        this.name = n;
        this.tex = tex;
    }

    public String getName() {
        return name;
    }

    public String getTex() {
        return tex;
    }
}
