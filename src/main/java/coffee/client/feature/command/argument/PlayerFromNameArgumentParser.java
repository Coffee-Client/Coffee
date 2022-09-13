/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.argument;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerFromNameArgumentParser implements ArgumentParser<PlayerEntity> {
    final boolean ignoreCase;

    public PlayerFromNameArgumentParser(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public PlayerEntity parse(String argument) throws CommandException {
        if (CoffeeMain.client.world == null) {
            throw new CommandException("World is not loaded");
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
        throw new CommandException("Invalid argument \"" + argument + "\": Player not found");
    }
}
