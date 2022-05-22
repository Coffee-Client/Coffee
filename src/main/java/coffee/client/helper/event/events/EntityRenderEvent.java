/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.event.events;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class EntityRenderEvent extends RenderEvent {

    final Entity target;

    public EntityRenderEvent(MatrixStack stack, Entity e) {
        super(stack);
        this.target = e;
    }

    public Entity getEntity() {
        return target;
    }
}
