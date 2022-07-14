/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.text.Text;

import java.util.Objects;

public class ViewNbt extends Command {

    int i = 0;

    public ViewNbt() {
        super("ViewNbt", "Views the nbt data of the current item", "viewnbt", "shownbt");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, "(flags)"));
    }

    @Override
    public void onExecute(String[] args) {
        boolean formatted = false;
        boolean copy = false;
        boolean noColor = false;
        if (args.length == 0) {
            message("pro tip: use \"viewnbt help\" to show additional options");
        } else if (args[0].equalsIgnoreCase("help")) {
            message("Use flags like these to control what to do with the nbt:");
            message("  N - Nothing (to skip the help message)");
            message("  F - Formatted (to format the nbt in a nice way)");
            message("  C - Copy (to copy the nbt to clipboard)");
            message("  W - White (to show uncolored nbt)");
            message("Examples: \".viewnbt FC\" to view a formatted view of the nbt and to copy it to clipboard");
            message("\".viewnbt CW\" to copy the nbt and show it without colors");
            return;
        } else {
            for (char c : args[0].toLowerCase().toCharArray()) {
                switch (c) {
                    case 'n' -> {
                    }
                    case 'f' -> formatted = true;
                    case 'c' -> copy = true;
                    case 'w' -> noColor = true;
                    default -> {
                        error("Unknown option '" + c + "'.");
                        return;
                    }
                }
            }
        }
        if (Objects.requireNonNull(CoffeeMain.client.player).getInventory().getMainHandStack().isEmpty()) {
            error("you're not holding anything");
            return;
        }
        ItemStack stack = CoffeeMain.client.player.getInventory().getMainHandStack();
        NbtCompound c = stack.getNbt();
        if (!stack.hasNbt() || c == null) {
            error("stack has no data");
            return;
        }
        if (formatted) {
            parse(c, "(root)");
        } else {
            // I've to use .sendMessage because of monkey minecraft api
            if (noColor) {
                CoffeeMain.client.player.sendMessage(Text.of(c.asString()), false);
            } else {
                CoffeeMain.client.player.sendMessage(NbtHelper.toPrettyPrintedText(c), false);
            }
        }
        if (copy) {
            CoffeeMain.client.keyboard.setClipboard(c.asString());
            success("Copied nbt!");
        }
    }

    void parse(NbtElement ne, String componentName) {
        if (ne instanceof NbtByteArray || ne instanceof NbtCompound || ne instanceof NbtIntArray || ne instanceof NbtList || ne instanceof NbtLongArray) {
            message(" ".repeat(i) + (componentName == null ? "-" : componentName + ":"));
            i += 2;
            if (ne instanceof NbtByteArray array) {
                for (NbtByte nbtByte : array) {
                    parse(nbtByte, null);
                }
            } else if (ne instanceof NbtCompound compound) {
                for (String key : compound.getKeys()) {
                    NbtElement ne1 = compound.get(key);
                    parse(ne1, key);
                }
            } else if (ne instanceof NbtIntArray nbtIntArray) {
                for (NbtInt nbtInt : nbtIntArray) {
                    parse(nbtInt, null);
                }
            } else if (ne instanceof NbtList nbtList) {
                for (NbtElement nbtElement : nbtList) {
                    parse(nbtElement, null);
                }
            } else {
                NbtLongArray nbtLongArray = (NbtLongArray) ne;
                for (NbtLong nbtLong : nbtLongArray) {
                    parse(nbtLong, null);
                }
            }
            i -= 2;
        } else {
            message(" ".repeat(i) + (componentName == null ? "-" : componentName + ":") + " " + ne.toString().replaceAll("§", "&"));
        }
    }
}
