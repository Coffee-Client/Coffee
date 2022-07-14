/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.coloring;

import java.util.function.Supplier;

public record PossibleArgument(ArgumentType argType, Supplier<String[]> suggestionSupplier) {
    public PossibleArgument(ArgumentType type, String... suggestions) {
        this(type, () -> suggestions);
    }
}
