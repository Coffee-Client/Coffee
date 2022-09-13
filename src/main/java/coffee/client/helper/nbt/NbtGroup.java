/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.nbt;

import net.minecraft.nbt.NbtCompound;

import java.util.Arrays;

public record NbtGroup(NbtElement... elements) {

    @Override
    public String toString() {
        return "NbtGroup{" + "elements=" + Arrays.toString(elements) + '}';
    }

    public NbtCompound toCompound() {
        NbtCompound nc = new NbtCompound();
        for (NbtElement element : elements) {
            element.serialize(nc);
        }
        return nc;
    }
}
