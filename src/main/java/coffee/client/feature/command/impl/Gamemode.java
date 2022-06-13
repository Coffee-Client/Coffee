/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.world.GameMode;

import java.util.Arrays;

public class Gamemode extends Command {

    public Gamemode() {
        super("Gamemode", "Switch gamemodes client side", "gamemode", "gm", "gmode");
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("survival", "creative", "adventure");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
                new PossibleArgument(ArgumentType.STRING, Arrays.stream(GameMode.values()).map(GameMode::getName).toList().toArray(String[]::new)));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        if (CoffeeMain.client.interactionManager == null) {
            return;
        }
        validateArgumentsLength(args, 1, "Provide gamemode");
        GameMode gm = GameMode.byName(args[0], null);
        if (gm == null) {
            throw new CommandException("Invalid gamemode", "Specify a valid gamemode");
        }
        CoffeeMain.client.interactionManager.setGameMode(gm);
    }
}
