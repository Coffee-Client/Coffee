/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render.textures;

import coffee.client.helper.render.Rectangle;
import coffee.client.helper.util.Utils;

public interface Texture {
    SpritesheetTextureSet MODULE_TYPES = SpritesheetTextureSet.fromJson("https://raw.githubusercontent.com/Coffee-Client/Resources/master/ss_module_types.png",
        Utils.loadFromResources("sprite/module_types.json"));
    SpritesheetTextureSet NOTIFICATION_TYPES = SpritesheetTextureSet.fromJson("https://raw.githubusercontent.com/Coffee-Client/Resources/master/ss_notifications.png",
        Utils.loadFromResources("sprite/notifications.json"));
    SpritesheetTextureSet ACTION_TYPES = SpritesheetTextureSet.fromJson("https://raw.githubusercontent.com/Coffee-Client/Resources/master/ss_actions.png",
        Utils.loadFromResources("sprite/actions.json"));
    DirectTexture BACKGROUND = new DirectTexture("https://raw.githubusercontent.com/Coffee-Client/Resources/master/background.png");
    ResourceTexture ICON = new ResourceTexture("assets/coffee/icon.png");

    void load() throws Throwable;

    void bind();

    Rectangle getBounds();

    default boolean alreadyInitialized() {
        return false;
    }
}
