/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.nbt;

import net.minecraft.nbt.NbtCompound;

import java.util.Arrays;

public class NbtList extends NbtElement {
    final String name;
    final NbtElement[] children;

    public NbtList(String name, NbtElement... children) {
        this.name = name;
        this.children = children;
    }

    @Override
    public String toString() {
        return "NbtArray{" + "name='" + name + '\'' + ", children=" + Arrays.toString(children) + '}';
    }

    @Override
    public void serialize(NbtCompound compound) {
        compound.put(name, get());
    }

    @Override
    public net.minecraft.nbt.NbtElement get() {
        net.minecraft.nbt.NbtList s = new net.minecraft.nbt.NbtList();
        for (NbtElement child : children) {
            s.add(child.get());
        }
        return s;
    }
}
