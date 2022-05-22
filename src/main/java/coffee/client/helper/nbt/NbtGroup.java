/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
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
