/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper.nbt;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

import java.util.UUID;

public class NbtProperty extends NbtElement {
    final String name;
    final Object val;

    public NbtProperty(String name, Object value) {
        this.name = name;
        this.val = value;
    }

    public NbtProperty(Object value) {
        this("", value);
    }

    @Override
    public String toString() {
        return "NbtProperty{" + "name='" + name + '\'' + ", val=" + val + '}';
    }

    @Override
    public void serialize(NbtCompound compound) {
        if (val instanceof UUID nu) {
            compound.putUuid(name, nu);
        } else if (val instanceof Boolean b) {
            compound.putBoolean(name, b);
        } else compound.put(name, get());
    }

    @Override
    public net.minecraft.nbt.NbtElement get() {
        if (val instanceof String s) {
            return NbtString.of(s);
        } else if (val instanceof Integer i) {
            return NbtInt.of(i);
        } else if (val instanceof Long l) {
            return NbtLong.of(l);
        } else if (val instanceof Double d) {
            return NbtDouble.of(d);
        } else if (val instanceof Float f) {
            return NbtFloat.of(f);
        } else if (val instanceof Byte b) {
            return NbtByte.of(b);
        } else if (val instanceof Short s) {
            return NbtShort.of(s);
        } else return null; // no nbt representation of it
    }
}
