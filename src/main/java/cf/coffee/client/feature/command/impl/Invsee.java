/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.command.impl;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.command.Command;
import cf.coffee.client.feature.command.argument.PlayerFromNameArgumentParser;
import cf.coffee.client.feature.command.coloring.ArgumentType;
import cf.coffee.client.feature.command.coloring.PossibleArgument;
import cf.coffee.client.feature.command.coloring.StaticArgumentServer;
import cf.coffee.client.feature.command.exception.CommandException;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;

public class Invsee extends Command {

    public Invsee() {
        super("Invsee", "Shows you the inventory of another player", "invsee", "isee");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, Objects.requireNonNull(CoffeeMain.client.world).getPlayers().stream().map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getGameProfile().getName()).toList().toArray(String[]::new)));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide target username");
        PlayerEntity t = new PlayerFromNameArgumentParser(true).parse(args[0]);
        Utils.TickManager.runOnNextRender(() -> CoffeeMain.client.setScreen(new InventoryScreen(t)));
    }
}
