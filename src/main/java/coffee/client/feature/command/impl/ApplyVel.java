/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.StreamlineArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.exception.CommandException;

public class ApplyVel extends Command {

    public ApplyVel() {
        super("ApplyVel", "Apply velocity to your player", "velocity", "vel", "applyVel", "yeet");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return switch (index) {
            case 0 -> new PossibleArgument(ArgumentType.NUMBER, "(x velocity)");
            case 1 -> new PossibleArgument(ArgumentType.NUMBER, "(y velocity)");
            case 2 -> new PossibleArgument(ArgumentType.NUMBER, "(z velocity)");
            default -> super.getSuggestionsWithType(index, args);
        };
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 3, "Provide X, Y and Z velocity");

        StreamlineArgumentParser dap = new StreamlineArgumentParser(args);
        double vx = dap.consumeDouble();
        double vy = dap.consumeDouble();
        double vz = dap.consumeDouble();

        CoffeeMain.client.player.addVelocity(vx, vy, vz);
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("1 2 3", "0 2 0");
    }
}
