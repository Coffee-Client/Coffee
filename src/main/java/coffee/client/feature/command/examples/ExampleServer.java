/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.examples;

import coffee.client.feature.command.Command;

public class ExampleServer {
    public static Command.ExamplesEntry getPlayerNames() {
        return new Command.ExamplesEntry("Notch", "Herobrine", "Player123");
    }
}
