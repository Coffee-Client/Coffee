/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.items.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.items.Item;
import coffee.client.feature.items.Option;
import coffee.client.helper.nbt.NbtArray;
import coffee.client.helper.nbt.NbtGroup;
import coffee.client.helper.nbt.NbtList;
import coffee.client.helper.nbt.NbtObject;
import coffee.client.helper.nbt.NbtProperty;
import coffee.client.helper.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class InfiniSculk extends Item {
    final Option<Integer> strength = new Option<>("charge", 300, Integer.class);

    public InfiniSculk() {
        super("InfiniSculk", "Makes a sculk catalyst that infects a very big region");
    }

    @Override
    public ItemStack generate() {
        HitResult bhr = CoffeeMain.client.player.raycast(200, 0f, false);
        if (!(bhr instanceof BlockHitResult bhr1)) {
            Utils.Logging.error("Look at a block to infect first");
            return null;
        }
        BlockPos origin = bhr1.getBlockPos();
        BlockState bs = CoffeeMain.client.world.getBlockState(origin);
        if (!bs.getMaterial().blocksMovement()) {
            Utils.Logging.error("Start block has to be solid");
            return null;
        }
        ItemStack stack = new ItemStack(Items.SCULK_CATALYST);
        NbtObject catalyst = new NbtObject("",
            new NbtProperty("charge", strength.getValue()),
            new NbtProperty("decay_delay", 1),
            new NbtList("facings"),
            NbtArray.create("pos", origin.getX(), origin.getY(), origin.getZ()),
            new NbtProperty("update_delay", 1));
        NbtObject[] l = new NbtObject[32];
        Arrays.fill(l, catalyst);
        NbtGroup root = new NbtGroup(new NbtObject("BlockEntityTag", new NbtList("cursors", l), new NbtProperty("id", "minecraft:sculk_catalyst")));
        stack.setNbt(root.toCompound());
        return stack;
    }
}
