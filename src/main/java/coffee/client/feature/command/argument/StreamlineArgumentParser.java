/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
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
        return consumeString(null);
    }

    public String consumeString(String defaultValue) throws CommandException {
        if (index >= args.length) {
            if (defaultValue != null) {
                return defaultValue;
            }
            throw new CommandException("Not enough arguments");
        }
        return args[index++];
    }

    public boolean isEmpty() {
        return index >= args.length;
    }

    public int consumeInt(Integer defaultValue) throws CommandException {
        return new IntegerArgumentParser().parse(consumeString(defaultValue == null ? null : defaultValue.toString()));
    }

    public int consumeInt() throws CommandException {
        return consumeInt(null);
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
