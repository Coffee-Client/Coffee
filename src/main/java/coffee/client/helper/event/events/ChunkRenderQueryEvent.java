/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.event.events;

import coffee.client.helper.event.events.base.NonCancellableEvent;

public class ChunkRenderQueryEvent extends NonCancellableEvent {
    private boolean shouldRender = false;
    private boolean wasModified = false;

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
        this.wasModified = true;
    }

    public boolean wasModified() {
        return wasModified;
    }

    public boolean shouldRender() {
        return shouldRender;
    }
}
