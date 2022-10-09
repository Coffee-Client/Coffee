/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.event;

import lombok.Getter;
import lombok.Setter;

public class Event {
    @Setter
    @Getter
    boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }
}
