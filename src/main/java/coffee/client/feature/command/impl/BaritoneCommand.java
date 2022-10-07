/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import baritone.api.BaritoneAPI;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.exception.CommandException;

import java.util.stream.Stream;

public class BaritoneCommand extends Command {
    public BaritoneCommand() {
        super("Baritone", "Runs a baritone command", "baritone", "b");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        if (index == 0) {
            return new PossibleArgument(ArgumentType.STRING, "<Baritone command>");
        }
        Stream<String> stringStream = BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().tabComplete(String.join(" ", args));
        String[] strings = stringStream.toArray(String[]::new);
        return new PossibleArgument(ArgumentType.STRING, strings.length == 0 ? new String[] { "<Baritone argument>" } : strings);
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        boolean executedAnything = BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(String.join(" ", args));
        if (!executedAnything) {
            error("Command not found");
        }
    }
}
