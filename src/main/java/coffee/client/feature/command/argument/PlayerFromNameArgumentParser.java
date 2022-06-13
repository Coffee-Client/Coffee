/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.argument;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

@SuppressWarnings("ClassCanBeRecord")
public class PlayerFromNameArgumentParser implements ArgumentParser<PlayerEntity> {
    final boolean ignoreCase;

    public PlayerFromNameArgumentParser(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public PlayerEntity parse(String argument) throws CommandException {
        if (CoffeeMain.client.world == null) {
            throw new CommandException("World is not loaded", "Join a world or server");
        }
        for (AbstractClientPlayerEntity player : CoffeeMain.client.world.getPlayers()) {
            if (ignoreCase) {
                if (player.getGameProfile().getName().equalsIgnoreCase(argument)) {
                    return player;
                }
            } else {
                if (player.getGameProfile().getName().equals(argument)) {
                    return player;
                }
            }
        }
        throw new CommandException("Invalid argument \"" + argument + "\": Player not found", "Provide the name of an existing player");
    }
}
