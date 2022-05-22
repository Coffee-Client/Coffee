/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtString;

public class Author extends Command {
    public Author() {
        super("Author", "Sets the author of a book", "author", "setAuthor");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return new PossibleArgument(ArgumentType.STRING, "(new author)");
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide author username");

        if (!CoffeeMain.client.interactionManager.hasCreativeInventory()) {
            error("You must be in creative mode to do this!");
            return;
        }

        ItemStack heldItem = CoffeeMain.client.player.getInventory().getMainHandStack();

        if (!heldItem.isOf(Items.WRITTEN_BOOK)) {
            error("You must hold a written book");
            return;
        }
        String author = String.join(" ", args);
        heldItem.setSubNbt("author", NbtString.of(author));
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("Notch", "Newton", "Your fucking father");
    }
}
