/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.command.impl;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.command.Command;
import cf.coffee.client.feature.command.coloring.ArgumentType;
import cf.coffee.client.feature.command.coloring.PossibleArgument;
import cf.coffee.client.feature.command.coloring.StaticArgumentServer;
import cf.coffee.client.feature.command.exception.CommandException;

import java.util.Objects;

public class Say extends Command {

    public Say() {
        super("Say", "Says something in chat (use when scripting or to say the prefix in chat)", "say", "tell");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, "(message)"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide message");
        Objects.requireNonNull(CoffeeMain.client.player).sendChatMessage(String.join(" ", args));
    }
}
