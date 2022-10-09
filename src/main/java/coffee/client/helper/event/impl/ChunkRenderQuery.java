/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.event.impl;

import coffee.client.helper.event.Event;
import lombok.Getter;

public class ChunkRenderQuery extends Event {
    @Getter
    boolean shouldRender = false;
    @Getter
    boolean modified = false;

    public void setShouldRender(boolean t) {
        this.shouldRender = t;
        this.modified = true;
    }
}
