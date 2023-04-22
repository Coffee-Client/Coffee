/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
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
        List<String> moduleNames = ModuleRegistry.getModules().stream().map(Module::getName).toList();
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, moduleNames.toArray(new String[0])));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide module name");
        String moduleName = String.join(" ", args);
        Module module = ModuleRegistry.getByName(moduleName);
        if (module == null) {
            throw new CommandException("Module not found: " + moduleName);
        } else {
            module.toggle();
            String status = module.isEnabled() ? "enabled" : "disabled";
            String message = String.format("%s has been %s", module.getName(), status);
            sendMessageToPlayer(message);
        }
    }
    
    private void sendMessageToPlayer(String message) {
        MinecraftClient.getInstance().player.sendMessage(new LiteralText(message), false);
    }
}
