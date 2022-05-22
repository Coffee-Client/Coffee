/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper;

import lombok.Getter;

public enum GameTexture {
    TEXTURE_ICON(new Texture("tex/icon"), "https://github.com/Coffee-Client/Resources/raw/master/icon.png"),
    TEXTURE_BACKGROUND(new Texture("tex/background"), "https://github.com/Coffee-Client/Resources/raw/master/background.png"),

    NOTIF_ERROR(new Texture("notif/error"), "https://github.com/Coffee-Client/Resources/raw/master/error.png"),
    NOTIF_INFO(new Texture("notif/info"), "https://github.com/Coffee-Client/Resources/raw/master/info.png"),
    NOTIF_SUCCESS(new Texture("notif/success"), "https://github.com/Coffee-Client/Resources/raw/master/success.png"),
    NOTIF_WARNING(new Texture("notif/warning"), "https://github.com/Coffee-Client/Resources/raw/master/warning.png"),

    ICONS_RENDER(new Texture("icons/render"), "https://github.com/Coffee-Client/Resources/raw/master/render.png"),
    ICONS_ADDON_PROVIDED(new Texture("icons/item"), "https://github.com/Coffee-Client/Resources/raw/master/addons.png"),
    ICONS_MOVE(new Texture("icons/move"), "https://github.com/Coffee-Client/Resources/raw/master/movement.png"),
    ICONS_MISC(new Texture("icons/misc"), "https://github.com/Coffee-Client/Resources/raw/master/misc.png"),
    ICONS_WORLD(new Texture("icons/world"), "https://github.com/Coffee-Client/Resources/raw/master/world.png"),
    ICONS_EXPLOIT(new Texture("icons/exploit"), "https://github.com/Coffee-Client/Resources/raw/master/exploit.png"),
    ICONS_COMBAT(new Texture("icons/combat"), "https://github.com/Coffee-Client/Resources/raw/master/combat.png"),

    ACTION_RUNCOMMAND(new Texture("actions/runCommand"), "https://github.com/Coffee-Client/Resources/raw/master/command.png"),
    ACTION_TOGGLEMODULE(new Texture("actions/toggleModule"), "https://github.com/Coffee-Client/Resources/raw/master/toggle.png");
    @Getter
    final String downloadUrl;
    @Getter
    final Texture where;

    GameTexture(Texture where, String downloadUrl) {
        this.where = where;
        this.downloadUrl = downloadUrl;
    }
}
