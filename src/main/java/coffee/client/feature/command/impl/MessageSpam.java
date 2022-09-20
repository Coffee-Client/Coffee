/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.IntegerArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;

import java.util.Arrays;

public class MessageSpam extends Command {
    public MessageSpam() {
        super("MessageSpam", "Sends a large amount of messages quickly", "spam", "spamMessages");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.NUMBER, "<amount>"), new PossibleArgument(ArgumentType.STRING, "<message>"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 2, "Provide amount and message");
        int amount = new IntegerArgumentParser().parse(args[0]);
        for (int i = 0; i < amount; i++) {
            CoffeeMain.client.player.sendChatMessage(String.join("", Arrays.copyOfRange(args, 1, args.length)), null);
        }
    }
}
