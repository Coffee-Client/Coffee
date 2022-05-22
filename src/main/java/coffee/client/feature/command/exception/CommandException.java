/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.exception;

public class CommandException extends Exception {
    final String potentialFix;

    public CommandException(String cause, String potentialFix) {
        super(cause);
        this.potentialFix = potentialFix;
    }

    public String getPotentialFix() {
        return potentialFix;
    }
}
