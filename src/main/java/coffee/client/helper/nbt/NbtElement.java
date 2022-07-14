/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.nbt;

import net.minecraft.nbt.NbtCompound;

public abstract class NbtElement {
    public abstract String toString();

    public abstract void serialize(NbtCompound compound);

    public abstract net.minecraft.nbt.NbtElement get();
}
