/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.coloring;

public class StaticArgumentServer {
    public static PossibleArgument serveFromStatic(int index, PossibleArgument... types) {
        if (index >= types.length) {
            return new PossibleArgument(null);
        }
        return types[index];
    }
}
