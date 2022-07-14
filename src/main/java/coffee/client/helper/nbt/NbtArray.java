/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.nbt;

import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLongArray;

import java.util.Arrays;
import java.util.List;

public class NbtArray<T> extends NbtElement {
    final String name;
    final T[] elements;

    @SafeVarargs
    private NbtArray(String name, T... elements) {
        this.name = name;
        this.elements = elements;
    }

    public static NbtArray<Integer> create(String name, Integer... elements) {
        return new NbtArray<>(name, elements);
    }

    public static NbtArray<Long> create(String name, Long... elements) {
        return new NbtArray<>(name, elements);
    }

    public static NbtArray<Byte> create(String name, Byte... elements) {
        return new NbtArray<>(name, elements);
    }

    @Override
    public net.minecraft.nbt.NbtElement get() {
        if (elements instanceof Integer[] val) {
            return new NbtIntArray(List.of(val));
        } else if (elements instanceof Long[] val) {
            return new NbtLongArray(List.of(val));
        } else if (elements instanceof Byte[] val) {
            return new NbtByteArray(List.of(val));
        }
        throw new IllegalArgumentException("Type invalid");
    }

    @Override
    public void serialize(NbtCompound compound) {
        compound.put(name, get());
    }

    @Override
    public String toString() {
        return "NbtArray{" + "name='" + name + '\'' + ", elements=" + Arrays.toString(elements) + '}';
    }
}
