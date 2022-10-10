/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.mixinUtil.ChatHudDuck;
import net.minecraft.text.Text;

public class Test extends Command {
    public Test() {
        super("Test", "REAL", "test");
    }

    @Override
    public void onExecute(String[] args) throws CommandException {

    }
}
