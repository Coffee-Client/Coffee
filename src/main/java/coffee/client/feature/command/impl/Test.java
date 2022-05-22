/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;

public class Test extends Command {
    public Test() {
        super("Test", "REAL", "test");
    }

    @Override
    public void onExecute(String[] args) {

    }
}
