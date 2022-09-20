/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.mixin.IGameProfileMixin;
import net.minecraft.client.network.AbstractClientPlayerEntity;

import java.util.Arrays;
import java.util.Objects;

public class FakeNick extends Command {
    public FakeNick() {
        super("FakeNick", "Fakes an entity name for a certain player", "fakenick", "fn");
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("Airpipe Coffee developer", "Notch Herobrine");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
            new PossibleArgument(ArgumentType.PLAYER,
                () -> Objects.requireNonNull(CoffeeMain.client.world)
                    .getPlayers()
                    .stream()
                    .map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getGameProfile().getName())
                    .toList()
                    .toArray(String[]::new)),
            new PossibleArgument(ArgumentType.STRING, "Adolf", "Fred", "Mark"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 2, "Player name and new name is required");
        String pname = args[0];
        String newName = String.join("_", Arrays.copyOfRange(args, 1, args.length)).replaceAll("&", "§");
        for (AbstractClientPlayerEntity player : CoffeeMain.client.world.getPlayers()) {
            if (player.getGameProfile().getName().equals(pname)) {
                success("Renamed " + player.getGameProfile().getName());
                ((IGameProfileMixin) player.getGameProfile()).coffee_setName(newName);
                return;
            }
        }
        error("No players called \"" + pname + "\" found");
    }
}
