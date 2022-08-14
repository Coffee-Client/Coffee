/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command;

import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.helper.util.Utils;
import net.minecraft.client.MinecraftClient;

public abstract class Command extends Utils.Logging {

    public final MinecraftClient client = MinecraftClient.getInstance();
    private final String name;
    private final String description;
    private final String[] aliases;

    public Command(String n, String d, String... a) {
        if (!n.equals(this.getClass().getSimpleName())) {
            new Thread(() -> {
                Utils.sleep(1000);
                System.exit(1);
            }).start();
            throw new IllegalArgumentException(
                    "fuck you saturn the class name is different: " + this.getClass().getSimpleName() + " vs " + n);
        }
        String first = String.valueOf(d.charAt(0));
        if (first.equals(first.toLowerCase())) {
            new Thread(() -> {
                Utils.sleep(1000);
                System.exit(1);
            }).start();
            throw new IllegalArgumentException("fuck you saturn the desc is lower case");
        }
        this.name = n;
        this.description = d;
        this.aliases = a;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }

    public abstract void onExecute(String[] args) throws CommandException;

    public ExamplesEntry getExampleArguments() {
        return null;
    }

    protected void validateArgumentsLength(String[] args, int requiredLength, String message) throws CommandException {
        if (args.length < requiredLength) {
            throw new CommandException("Invalid number of arguments: " + requiredLength + " arguments required", message);
        }
    }

    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return new PossibleArgument(null);
    }

    public record ExamplesEntry(String... examples) {
    }
}
