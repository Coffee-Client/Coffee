/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.IntegerArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.entity.Entity;

public class EVclip extends Command {
    public EVclip() {
        super("EVclip", "VClip with an entity", "evc", "evclip", "entityVclip");
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("1", "2", "69");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.NUMBER, "<amount>"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide height");

        if (!CoffeeMain.client.player.hasVehicle()) {
            error("You're not riding an entity");
            return;
        }

        int up = new IntegerArgumentParser().parse(args[0]);

        Entity vehicle = CoffeeMain.client.player.getVehicle();
        vehicle.updatePosition(vehicle.getX(), vehicle.getY() + up, vehicle.getZ());
        message("Teleported entity");
    }
}
