/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
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
