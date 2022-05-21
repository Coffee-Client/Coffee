/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.command.impl;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.command.Command;
import cf.coffee.client.feature.command.coloring.ArgumentType;
import cf.coffee.client.feature.command.coloring.PossibleArgument;
import cf.coffee.client.feature.command.exception.CommandException;
import cf.coffee.client.feature.gui.screen.BindScreen;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.helper.util.Utils;

public class Bind extends Command {
    public Bind() {
        super("Bind", "Sets the keybind of a module", "bind");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        if (index == 0) return new PossibleArgument(ArgumentType.STRING, ModuleRegistry.getModules()
                .stream()
                .map(Module::getName)
                .toList()
                .toArray(String[]::new));
        return super.getSuggestionsWithType(index, args);
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("Flight", "NoFall", "ClickGui");
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide module name");
        String mn = args[0];
        Module module = ModuleRegistry.getByName(mn);
        if (module == null) {
            error("Module not found");
            return;
        }
        BindScreen bs = new BindScreen(module);
        Utils.TickManager.runInNTicks(5, () -> CoffeeMain.client.setScreen(bs));
    }
}
