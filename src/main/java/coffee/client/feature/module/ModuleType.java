/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module;

import coffee.client.helper.GameTexture;
import coffee.client.helper.Texture;

public enum ModuleType {
    RENDER("Render", GameTexture.ICONS_RENDER.getWhere()),
    MOVEMENT("Movement", GameTexture.ICONS_MOVE.getWhere()),
    MISC("Miscellaneous", GameTexture.ICONS_MISC.getWhere()),
    WORLD("World", GameTexture.ICONS_WORLD.getWhere()),
    EXPLOIT("Exploit", GameTexture.ICONS_EXPLOIT.getWhere()),
    ADDON_PROVIDED("Addons", GameTexture.ICONS_ADDON_PROVIDED.getWhere()),
    COMBAT("Combat", GameTexture.ICONS_COMBAT.getWhere());


    final String name;
    final Texture tex;

    ModuleType(String n, Texture tex) {
        this.name = n;
        this.tex = tex;
    }

    public String getName() {
        return name;
    }

    public Texture getTex() {
        return tex;
    }
}
