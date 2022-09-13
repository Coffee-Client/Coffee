/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SelfDestruct extends Command {
    static final List<Module> modulesToReenable = new ArrayList<>();
    private static final AtomicBoolean selfDestruct = new AtomicBoolean(false);
    static String reset = "";

    public SelfDestruct() {
        super("SelfDestruct", "Hides the client entirely", "selfdestruct", "sd");
    }

    public static boolean shouldSelfDestruct() {
        return selfDestruct.get();
    }

    public static boolean handleMessage(String msg) {
        if (msg.equals(reset)) {
            selfDestruct.set(false);
            for (Module module : modulesToReenable) {
                module.setEnabled(true);
            }
            modulesToReenable.clear();
            reset = "";
            return true;
        }
        return false;
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, "<reset phrase>"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide message");
        modulesToReenable.clear();
        for (Module module : ModuleRegistry.getModules()) {
            if (module.isEnabled()) {
                module.setEnabled(false);
                modulesToReenable.add(module);
            }
        }
        selfDestruct.set(true);
        reset = String.join(" ", args);
    }
}
