/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.command.impl;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.command.Command;
import cf.coffee.client.feature.command.argument.PlayerFromNameArgumentParser;
import cf.coffee.client.feature.command.coloring.ArgumentType;
import cf.coffee.client.feature.command.coloring.PossibleArgument;
import cf.coffee.client.feature.command.exception.CommandException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Objects;

public class FakeItem extends Command {
    public FakeItem() {
        super("FakeItem", "Fakes a person holding a specific item", "fakeItem");
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("Notch hand", "Herobrine custom:compass", "Player123 custom:bat_spawn_egg {EntityTag:{id: \"minecraft: cow\"}}");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return switch (index) {
            case 0 ->
                    new PossibleArgument(ArgumentType.STRING, Objects.requireNonNull(CoffeeMain.client.world).getPlayers().stream().map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getGameProfile().getName()).toList().toArray(String[]::new));
            case 1 -> new PossibleArgument(ArgumentType.STRING, "hand", "custom:(item id) [item nbt]");
            case 2 -> {
                if (args[1].toLowerCase().startsWith("custom:")) {
                    yield new PossibleArgument(ArgumentType.STRING, "(item nbt)");
                }
                yield super.getSuggestionsWithType(index, args);
            }
            default -> super.getSuggestionsWithType(index, args);
        };
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 2, "Provide player and source");
        PlayerEntity le = new PlayerFromNameArgumentParser(true).parse(args[0]);
        if (args[1].equalsIgnoreCase("hand")) {
            ItemStack main = Objects.requireNonNull(CoffeeMain.client.player).getMainHandStack().copy();
            if (main.isEmpty()) {
                error("You're not holding anything");
                return;
            }
            le.equipStack(EquipmentSlot.MAINHAND, main);
        } else if (args[1].toLowerCase().startsWith("custom:")) {
            String id = args[1].substring("custom:".length());
            Identifier idParsed = Identifier.tryParse(id);
            if (idParsed == null) {
                error("Invalid item");
                return;
            }

            if (!Registry.ITEM.containsId(idParsed)) {
                error("Item not found");
                return;
            }
            Item item = Registry.ITEM.get(idParsed);
            ItemStack stack = new ItemStack(item);
            if (args.length > 2) { // we got additional nbt
                try {
                    stack.setNbt(StringNbtReader.parse(String.join(" ", Arrays.copyOfRange(args, 2, args.length))));
                } catch (CommandSyntaxException e) {
                    error("Invalid NBT: " + e.getContext());
                    return;
                }
            }
            le.equipStack(EquipmentSlot.MAINHAND, stack);
        }
        success("Faked item until manual update by the entity");
    }
}
