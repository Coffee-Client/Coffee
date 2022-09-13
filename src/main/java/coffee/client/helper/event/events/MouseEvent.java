/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.event.events;

import coffee.client.helper.event.events.base.Event;

public class MouseEvent extends Event {

    final int button;
    final int type;

    public MouseEvent(int button, int action) {
        this.button = button;
        type = action;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return type;
    }
}
