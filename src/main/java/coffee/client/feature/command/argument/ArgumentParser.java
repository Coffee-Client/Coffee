/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.argument;

import coffee.client.feature.command.exception.CommandException;

public interface ArgumentParser<T> {
    T parse(String argument) throws CommandException;
}
