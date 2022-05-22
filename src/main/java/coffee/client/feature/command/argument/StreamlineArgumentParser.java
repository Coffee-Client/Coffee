/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.argument;

import coffee.client.feature.command.exception.CommandException;
import net.minecraft.entity.player.PlayerEntity;

public class StreamlineArgumentParser {
    final String[] args;
    int index = 0;

    public StreamlineArgumentParser(String[] args) {
        this.args = args;
    }

    public String consumeString() throws CommandException {
        if (index >= args.length) throw new CommandException("Not enough arguments", null);
        String el = args[index];
        index++;
        return el;
    }

    public int consumeInt() throws CommandException {
        return new IntegerArgumentParser().parse(consumeString());
    }

    public double consumeDouble() throws CommandException {
        return new DoubleArgumentParser().parse(consumeString());
    }

    public PlayerEntity consumePlayerEntityFromName(boolean ignoreCase) throws CommandException {
        return new PlayerFromNameArgumentParser(ignoreCase).parse(consumeString());
    }

    public PlayerEntity consumePlayerEntityFromUuid() throws CommandException {
        return new PlayerFromUuidArgumentParser().parse(consumeString());
    }
}
