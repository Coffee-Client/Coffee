/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.argument;

import coffee.client.feature.command.exception.CommandException;

public interface ArgumentParser<T> {
    T parse(String argument) throws CommandException;
}
