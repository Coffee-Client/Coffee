/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.event.events;

import coffee.client.helper.event.events.base.Event;
import net.minecraft.client.util.math.MatrixStack;

public class RenderEvent extends Event {

    final MatrixStack stack;

    public RenderEvent(MatrixStack stack) {
        this.stack = stack;
    }

    public MatrixStack getStack() {
        return stack;
    }
}
