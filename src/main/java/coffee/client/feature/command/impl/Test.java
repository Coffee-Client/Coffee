/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.feature.gui.notifications.Notification;

public class Test extends Command {
    public Test() {
        super("Test", "REAL", "test");
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        for (Notification.Type value : Notification.Type.values()) {
            Notification.create(5000, "Holy shit", value, "cum", "cum,", "cum");
        }
    }
}
