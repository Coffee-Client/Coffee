/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.exception;

public class CommandException extends Exception {
    public CommandException(String cause) {
        super(cause);
    }
}
