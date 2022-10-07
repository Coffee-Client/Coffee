/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.misc.ChestIndexer;
import coffee.client.helper.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Search extends Command {
    private static String[] ALL_FUCKING_ITEM_IDS;

    public Search() {
        super("Search", "Searches chests for a specified item. Requires ChestIndexer to be enabled.", "search", "searchItem", "findItem", "find");
    }

    private static String[] getAllFuckingItemIds() {
        if (ALL_FUCKING_ITEM_IDS == null) {
            ALL_FUCKING_ITEM_IDS = Registry.ITEM.getIds().stream().map(Identifier::toString).toArray(String[]::new);
        }
        return ALL_FUCKING_ITEM_IDS;
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, getAllFuckingItemIds()));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        ChestIndexer indexer = ModuleRegistry.getByClass(ChestIndexer.class);
        if (!indexer.isEnabled()) {
            throw new CommandException("ChestIndexer needs to be enabled");
        }
        //        indexer.wipeDistant();
        BlockPos origin = client.player.getBlockPos();
        Map<BlockPos, Int2ObjectMap<ItemStack>> stacks = new HashMap<>(indexer.stacks);
        int omitted = 0;
        for (BlockPos blockPos : new ArrayList<>(stacks.keySet())) {
            if (!blockPos.isWithinDistance(origin, 128)) {
                stacks.remove(blockPos);
                omitted++;
            }
        }
        if (args.length == 0) {
            if(stacks.isEmpty()) {
                error("No items?");
                return;
            }
            Object2IntMap<Item> itemAmounts = new Object2IntArrayMap<>();
            for (Int2ObjectMap<ItemStack> value : stacks.values()) {
                for (ItemStack itemStack : value.values()) {
                    Item item = itemStack.getItem();
                    itemAmounts.put(item, itemAmounts.getOrDefault(item, 0) + itemStack.getCount());
                }
            }

            message("You have:");
            for (Item item : itemAmounts.keySet().stream().sorted(Comparator.comparingInt(value -> itemAmounts.getOrDefault(value, 0))).toList()) {
                message(String.format("  %s: %s", item.getName().getString(), itemAmounts.getOrDefault(item, 0)));
            }
        } else {
            String search = args[0];
            Item searchFor = Utils.throwSilently(() -> Registry.ITEM.get(new Identifier(search)));
            if (searchFor == null) {
                throw new CommandException("Invalid search term");
            }
            Object2IntMap<BlockPos> foundAt = new Object2IntArrayMap<>();
            for (BlockPos k : stacks.keySet()) {
                Int2ObjectMap<ItemStack> value = stacks.get(k);
                for (ItemStack itemStack : value.values()) {
                    Item item = itemStack.getItem();
                    if (item.equals(searchFor)) {
                        foundAt.put(k, foundAt.getOrDefault(k, 0) + itemStack.getCount());
                    }
                }
            }
            if (foundAt.isEmpty()) {
                error(String.format("Didn't find item anywhere. %s",
                    omitted > 0 ? String.format("Note that %s container%s were omitted because of distance", omitted, omitted != 1 ? "s" : "") : ""));
            } else {
                message("Found " + searchFor.getName().getString() + " at:");
                for (BlockPos item : foundAt.keySet().stream().sorted(Comparator.comparingInt(value -> foundAt.getOrDefault(value, 0))).toList()) {
                    indexer.showResult(item);
                    message(String.format("  %s %s %s: %s times", item.getX(), item.getY(), item.getZ(), foundAt.getOrDefault(item, 0)));
                }
            }
        }
    }
}
