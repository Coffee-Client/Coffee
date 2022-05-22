/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.event.events.base;

public class NonCancellableEvent extends Event {

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        throw new IllegalStateException("Event cannot be cancelled.");
    }
}
