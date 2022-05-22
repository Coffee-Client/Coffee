/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.config.SettingBase;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;

import java.util.Arrays;
import java.util.Objects;

public class Config extends Command {

    public Config() {
        super("Config", "Changes configuration of a module", "config", "conf");
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("NoFall", "Flight Mode", "Flight Bypass-vanilla-AC true");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return switch (index) {
            case 0 -> new PossibleArgument(ArgumentType.STRING, ModuleRegistry.getModules()
                    .stream()
                    .map(mod -> mod.getName().replaceAll(" ", "-"))
                    .toList()
                    .toArray(String[]::new));
            case 1 -> {
                if (ModuleRegistry.getByName(args[0]) != null) {
                    yield new PossibleArgument(ArgumentType.STRING, Objects.requireNonNull(ModuleRegistry.getByName(args[0].replaceAll("-", " "))).config.getSettings()
                            .stream()
                            .map(SettingBase::getName)
                            .toList()
                            .toArray(String[]::new));
                } else yield super.getSuggestionsWithType(index, args);
            }
            case 2 -> new PossibleArgument(ArgumentType.STRING, "(New value)");
            default -> super.getSuggestionsWithType(index, args);
        };
    }

    @Override
    public void onExecute(String[] args) {
        if (args.length == 0) {
            message("Syntax: .config (module) <key> <value>");
            message("For a module or key with spaces, use - as a separator");
            message("Example: \".config block-spammer times-per-tick 11\" to set the \"times per tick\" property to 11");
            return;
        }
        Module target = ModuleRegistry.getByName(args[0].replaceAll("-", " "));
        if (target == null) {
            error("Module not found");
            return;
        }
        if (args.length == 1) {
            for (SettingBase<?> dynamicValue : target.config.getSettings()) {
                message(dynamicValue.getName() + " = " + dynamicValue.getValue().toString());
            }
        } else if (args.length == 2) {
            SettingBase<?> val = target.config.get(args[1].replaceAll("-", " "));
            if (val == null) {
                error("Key not found");
                return;
            }
            message(val.getName() + " = " + val.getValue().toString());
        } else if (args.length == 3) {
            SettingBase<?> val = target.config.get(args[1].replaceAll("-", " "));
            if (val == null) {
                error("Key not found");
                return;
            }
            val.accept(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
        }
    }
}
