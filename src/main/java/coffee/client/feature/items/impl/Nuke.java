/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.items.impl;

import coffee.client.feature.items.Item;
import coffee.client.feature.items.Option;
import coffee.client.helper.nbt.NbtGroup;
import coffee.client.helper.nbt.NbtList;
import coffee.client.helper.nbt.NbtObject;
import coffee.client.helper.nbt.NbtProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Nuke extends Item {
    final Option<Integer> o = new Option<>("tntFuse", 120, Integer.class);

    public Nuke() {
        super("Nuke", "Nukes the area");
    }

    @Override
    public ItemStack generate() {
        ItemStack spawn = new ItemStack(Items.SPAWNER);
        NbtGroup blt = new NbtGroup(new NbtObject("BlockEntityTag",
            new NbtProperty("MinSpawnDelay", 1),
            new NbtProperty("MaxSpawnDelay", 1),
            new NbtProperty("SpawnRange", 100),
            new NbtProperty("SpawnCount", 50),
            new NbtProperty("MaxNearbyEntities", 32766),
            new NbtObject("SpawnData",
                new NbtObject("entity", new NbtProperty("id", "minecraft:tnt"), new NbtProperty("HasVisualFire", true), new NbtProperty("Fuse", o.getValue()))),
            new NbtList("SpawnPotentials",
                new NbtObject("",
                    new NbtProperty("weight", 1),
                    new NbtObject("data",
                        new NbtObject("entity",
                            new NbtProperty("id", "minecraft:tnt"),
                            new NbtProperty("HasVisualFire", true),
                            new NbtProperty("Fuse", o.getValue()),
                            new NbtProperty("NoGravity", true),
                            new NbtList("Motion", new NbtProperty(0d), new NbtProperty(2d), new NbtProperty(0d))))),
                new NbtObject("",
                    new NbtProperty("weight", 1),
                    new NbtObject("data",
                        new NbtObject("entity",
                            new NbtProperty("id", "minecraft:tnt"),
                            new NbtProperty("HasVisualFire", true),
                            new NbtProperty("Fuse", o.getValue()),
                            new NbtProperty("NoGravity", true),
                            new NbtList("Motion", new NbtProperty(0d), new NbtProperty(-2d), new NbtProperty(0d))))))));
        spawn.setNbt(blt.toCompound());
        return spawn;
    }
}
