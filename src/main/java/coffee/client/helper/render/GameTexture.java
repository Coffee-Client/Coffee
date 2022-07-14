/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render;

import coffee.client.helper.util.Utils;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;

public enum GameTexture {
    TEXTURE_ICON(new Texture("tex/icon"), "res:///assets/coffee/icon.png"), // custom protocol, handled below
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
    final Texture where;

    @Getter
    final String downloadUrl;
    @Getter
    final Rectangle dimensions = new Rectangle(0, 0, 0, 0);
    @Getter
    boolean alreadyInitialized = false;
    BufferedImage registerLater = null;

    GameTexture(Texture where, String url) {
        this.where = where;
        this.downloadUrl = url;
        URI c = URI.create(url);
        if (c.getScheme().equals("res")) {
            this.alreadyInitialized = true;
            String p = c.getPath();
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            try (InputStream is = GameTexture.class.getClassLoader().getResourceAsStream(p)) {
                if (is == null) {
                    throw new IllegalArgumentException("Resource " + c.getPath() + " not found");
                }
                BufferedImage bi = ImageIO.read(is);
                getDimensions().setX(0);
                getDimensions().setY(0);
                getDimensions().setX1(bi.getWidth());
                getDimensions().setY1(bi.getHeight());
                registerLater = bi;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void postInit() {
        for (GameTexture value : values()) {
            if (value.registerLater != null) {
                Utils.registerBufferedImageTexture(value.where, value.registerLater);
                value.registerLater = null;
            }
        }
    }

}
