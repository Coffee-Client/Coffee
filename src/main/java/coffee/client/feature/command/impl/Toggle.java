/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;

public class Toggle extends Command {

    public Toggle() {
        super("Toggle", "Toggles a module", "toggle", "t");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
                new PossibleArgument(ArgumentType.STRING, ModuleRegistry.getModules().stream().map(Module::getName).toList().toArray(String[]::new))
        );
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide module name");
        Module m = ModuleRegistry.getByName(String.join(" ", args));
        if (m == null) {
            throw new CommandException("Module not found", "Specify a module name that exists");
        } else {
            m.toggle();
        }
    }
}
