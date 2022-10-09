/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.event.impl;

import coffee.client.helper.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

@AllArgsConstructor
@Getter
public class LoreQueryEvent extends Event {
    ItemStack source;
    List<Text> existingLore;

    public void addLore(String v) {
        existingLore.add(Text.of(v));
    }

    public void addClientLore(String v) {
        addLore("§7" + v + "§r");
    }
}
