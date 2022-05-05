/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.command.impl;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.command.Command;
import cf.coffee.client.feature.command.coloring.ArgumentType;
import cf.coffee.client.feature.command.coloring.PossibleArgument;
import cf.coffee.client.feature.command.coloring.StaticArgumentServer;
import cf.coffee.client.feature.command.exception.CommandException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public class Inject extends Command {
    public Inject() {
        super("Inject", "Injects a chunk of nbt into the target item", "inject", "inj", "addNbt");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, "(nbt)"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide NBT");
        ItemStack is = CoffeeMain.client.player.getMainHandStack();

        String nString = String.join(" ", args);
        NbtCompound old = is.getOrCreateNbt();
        try {
            NbtCompound ncNew = StringNbtReader.parse(nString);
            old.copyFrom(ncNew);
            success("Item modified");
        } catch (Exception e) {
            error(e.getMessage());
        }
    }
}
