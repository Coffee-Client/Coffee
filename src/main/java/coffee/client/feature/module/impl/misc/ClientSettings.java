/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class ClientSettings extends Module {
//    @Getter
    //    final StringSetting prefix = this.config.create(new StringSetting.Builder(".").name("Prefix").description("The prefix to use for commands").get());
    @Setting(name = "Prefix", description = "The prefix to use for commands")
    public String prefix = ".";

    public enum ToggleMode {
        Notification, Chat
    }

    @Setting(name = "Toggle style", description = "How the module toggle notifications should look")
    public ToggleMode toggleStyle = ToggleMode.Notification;

    public ClientSettings() {
        super("ClientSettings", "Configuration for the client", ModuleType.MISC);
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {
        setEnabled(false);
        Notification.create(5000, "ClientSettings", Notification.Type.INFO, "No need to enable this");
    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }
}
