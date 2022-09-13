/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.argument;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class PlayerFromUuidArgumentParser implements ArgumentParser<PlayerEntity> {
    @Override
    public PlayerEntity parse(String argument) throws CommandException {
        if (CoffeeMain.client.world == null) {
            throw new CommandException("World is not loaded");
        }
        try {
            UUID u = UUID.fromString(argument);
            for (AbstractClientPlayerEntity player : CoffeeMain.client.world.getPlayers()) {
                if (player.getUuid().equals(u)) {
                    return player;
                }
            }
            throw new CommandException("Invalid argument \"" + argument + "\": Player not found");
        } catch (Exception e) {
            throw new CommandException("Invalid argument \"" + argument + "\": Expected an UUID");
        }
    }
}
