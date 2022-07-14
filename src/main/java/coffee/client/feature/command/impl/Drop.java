/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.helper.util.Utils;

public class Drop extends Command {

    public Drop() {
        super("Drop", "Drops all items in your inventory", "drop", "d", "throw");
    }

    @Override
    public void onExecute(String[] args) {
        for (int i = 0; i < 36; i++) {
            Utils.Inventory.drop(i);
        }
    }
}
