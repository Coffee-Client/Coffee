/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.examples;

import coffee.client.feature.command.Command;

public class ExampleServer {
    public static Command.ExamplesEntry getPlayerNames() {
        return new Command.ExamplesEntry("Notch", "Herobrine", "Player123");
    }
}
