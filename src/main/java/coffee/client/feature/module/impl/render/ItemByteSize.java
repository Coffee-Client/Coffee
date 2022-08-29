/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.LoreQueryEvent;
import coffee.client.helper.util.ByteCounter;
import net.minecraft.client.util.math.MatrixStack;

import java.text.StringCharacterIterator;

public class ItemByteSize extends Module {
    public ItemByteSize() {
        super("ItemByteSize", "Shows the size of an item in bytes on the tooltip", ModuleType.RENDER);
        Events.registerEventHandler(EventType.LORE_QUERY, event -> {
            if (!this.isEnabled()) {
                return;
            }

            LoreQueryEvent e = (LoreQueryEvent) event;
            ByteCounter inst = ByteCounter.instance();
            inst.reset();
            boolean error = false;
            try {
                e.getSource().getOrCreateNbt().write(inst);
            } catch (Exception ignored) {
                error = true;
            }
            long count = inst.getSize();
            String fmt;
            if (error) {
                fmt = "§cError";
            } else {
                fmt = humanReadableByteCountBin(count);
            }
            e.addClientLore("Size: " + fmt);
        }, 0);
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        StringCharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }
}
