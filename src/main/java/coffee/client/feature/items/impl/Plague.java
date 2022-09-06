/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
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

public class Plague extends Item {
    final Option<Integer> duration = new Option<>("durationSeconds", 60, Integer.class);
    final Option<Float> spread = new Option<>("spreadRadius", 0.3f, Float.class);

    public Plague() {
        super("Plague", "the doctor");
    }

    @Override
    public ItemStack generate() {

        ItemStack spawn = new ItemStack(Items.SKELETON_SPAWN_EGG);
        NbtGroup ng = new NbtGroup(new NbtList("Enchantments", new NbtObject("", new NbtProperty("id", "minecraft:protection"), new NbtProperty("lvl", (short) 1))),
            new NbtObject("EntityTag",
                new NbtProperty("Duration", duration.getValue() * 20),
                new NbtList("Effects", new NbtObject("", new NbtProperty("Amplifier", (byte) 125), new NbtProperty("Duration", 2000), new NbtProperty("Id", (byte) 6))),
                new NbtProperty("Particle", "barrier"),
                new NbtProperty("Radius", 1.0f),
                new NbtProperty("RadiusPerTick", spread.getValue()),
                new NbtProperty("ReapplicationDelay", 0),
                new NbtProperty("id", "minecraft:area_effect_cloud")),
            new NbtProperty("HideFlags", 63));
                /*
        * {
    Enchantments: [
        {
            id: "minecraft:protection",
            lvl: 1s
        }
    ],
    EntityTag: {
        Duration: 1999999980,
        Effects: [
            {
                Amplifier: 125b,
                Duration: 2000,
                Id: 6b
            }
        ],
        Particle: "barrier",
        Radius: 1.0f,
        RadiusPerTick: 0.03f,
        ReapplicationDelay: 0,
        id: "minecraft:area_effect_cloud"
    },
    HideFlags: 63,
    display: {
        Lore: [
            '{"extra":[{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"color":"gray","text":"Summons a growth"}],"text":""}'
        ],
        Name: '{"extra":[{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"color":"dark_gray","text":"Plague"}],"text":""}'
    }
}
        * */
        spawn.setNbt(ng.toCompound());
        return spawn;
    }
}
