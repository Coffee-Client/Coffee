/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.event.events;

import coffee.client.helper.event.events.base.NonCancellableEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class LoreQueryEvent extends NonCancellableEvent {

    final ItemStack source;
    final List<Text> lore;

    public LoreQueryEvent(ItemStack stack, List<Text> currentLore) {
        this.source = stack;
        this.lore = currentLore;
    }

    public ItemStack getSource() {
        return source;
    }

    public List<Text> getLore() {
        return lore;
    }

    public void addLore(String v) {
        lore.add(Text.of(v));
    }

    public void addClientLore(String v) {
        addLore("§7" + v + "§r");
    }
}
