/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.items;


import coffee.client.feature.items.impl.Backdoor;
import coffee.client.feature.items.impl.Fireball;
import coffee.client.feature.items.impl.InfiniSculk;
import coffee.client.feature.items.impl.InfiniteEntity;
import coffee.client.feature.items.impl.Nuke;
import coffee.client.feature.items.impl.Plague;
import coffee.client.feature.items.impl.Poof;

import java.util.ArrayList;
import java.util.List;

public class ItemRegistry {
    public static final ItemRegistry instance = new ItemRegistry();
    final List<Item> items = new ArrayList<>();

    private ItemRegistry() {
        init();
    }

    void init() {
        items.clear();
        items.add(new Nuke());
        items.add(new Plague());
        items.add(new Poof());
        items.add(new Backdoor());
        items.add(new Fireball());
        items.add(new InfiniteEntity());
        items.add(new InfiniSculk());
    }

    public List<Item> getItems() {
        return items;
    }
}
