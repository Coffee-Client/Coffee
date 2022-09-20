/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.argument;

import coffee.client.feature.command.exception.CommandException;

public class IntegerArgumentParser implements ArgumentParser<Integer> {
    @Override
    public Integer parse(String argument) throws CommandException {
        try {
            return Integer.parseInt(argument);
        } catch (Exception e) {
            throw new CommandException("Invalid argument \"" + argument + "\": Expected a number");
        }
    }
}
