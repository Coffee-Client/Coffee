/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
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
