/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper;

import lombok.Getter;

public enum GameTexture {
    TEXTURE_ICON(new Texture("tex/icon"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/icon.png"),
    TEXTURE_BACKGROUND(new Texture("tex/background"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/background.jpg?inline=false"),
    TEXTURE_LOGO(new Texture("tex/logo"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/shadow_logo.png"),
    TEXTURE_ICON_FULL(new Texture("tex/iconFull"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/shadowLogoFull.png?inline=false"),

    NOTIF_ERROR(new Texture("notif/error"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/error.png"),
    NOTIF_INFO(new Texture("notif/info"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/info.png"),
    NOTIF_SUCCESS(new Texture("notif/success"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/success.png"),
    NOTIF_WARNING(new Texture("notif/warning"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/warning.png"),

    ICONS_RENDER(new Texture("icons/render"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/render.png"),
    ICONS_CRASH(new Texture("icons/crash"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/crash.png"),
    ICONS_GRIEF(new Texture("icons/grief"), "https://github.com/Saturn5Vfive/shadow-fs/blob/main/grief.png?raw=true"),
    ICONS_ADDON_PROVIDED(new Texture("icons/item"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/addons.png"),
    ICONS_MOVE(new Texture("icons/move"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/movement.png"),
    ICONS_MISC(new Texture("icons/misc"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/misc.png"),
    ICONS_WORLD(new Texture("icons/world"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/world.png"),
    ICONS_EXPLOIT(new Texture("icons/exploit"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/exploit.png"),
    ICONS_COMBAT(new Texture("icons/combat"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/combat.png"),

    ACTION_RUNCOMMAND(new Texture("actions/runCommand"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/command.png"),
    ACTION_TOGGLEMODULE(new Texture("actions/toggleModule"), "https://gitlab.com/0x151/coffee-fs/-/raw/main/toggle.png");
    @Getter
    final String downloadUrl;
    @Getter
    final Texture where;

    GameTexture(Texture where, String downloadUrl) {
        this.where = where;
        this.downloadUrl = downloadUrl;
    }
}
