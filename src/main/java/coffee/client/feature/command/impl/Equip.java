/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.screen.slot.SlotActionType;

public class Equip extends Command {
    public Equip() {
        super("Equip", "Equips items", "equip");
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("head", "legs", "feet");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, "head", "chest", "legs", "feet"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide slot");

        switch (args[0].toLowerCase()) {
            case "head" -> {
                // 39 HEAD - 36 FEET
                CoffeeMain.client.interactionManager.clickSlot(
                        CoffeeMain.client.player.currentScreenHandler.syncId,
                        36 + CoffeeMain.client.player.getInventory().selectedSlot,
                        39,
                        SlotActionType.SWAP,
                        CoffeeMain.client.player
                );
                message("Equipped item on head");
            }
            case "chest" -> {
                CoffeeMain.client.interactionManager.clickSlot(
                        CoffeeMain.client.player.currentScreenHandler.syncId,
                        36 + CoffeeMain.client.player.getInventory().selectedSlot,
                        39,
                        SlotActionType.SWAP,
                        CoffeeMain.client.player
                );
                message("Equipped item on chest");
            }
            case "legs" -> {
                CoffeeMain.client.interactionManager.clickSlot(
                        CoffeeMain.client.player.currentScreenHandler.syncId,
                        36 + CoffeeMain.client.player.getInventory().selectedSlot,
                        39,
                        SlotActionType.SWAP,
                        CoffeeMain.client.player
                );
                message("Equipped item on legs");
            }
            case "feet" -> {
                CoffeeMain.client.interactionManager.clickSlot(
                        CoffeeMain.client.player.currentScreenHandler.syncId,
                        36 + CoffeeMain.client.player.getInventory().selectedSlot,
                        39,
                        SlotActionType.SWAP,
                        CoffeeMain.client.player
                );
                message("Equipped item on feet");
            }
            default -> error("Incorrect slot, slots are chest, legs, feet, and head");
        }
    }
}
