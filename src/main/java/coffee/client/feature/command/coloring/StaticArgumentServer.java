/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
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
