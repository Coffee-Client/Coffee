/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.event.impl;

import coffee.client.helper.event.Event;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MouseEvent extends Event {
    int button;
    Type type;

    public enum Type {
        LIFT, CLICK;

        public static Type of(int id) {
            Preconditions.checkElementIndex(id, 2);
            return Type.values()[id];
        }
    }
}
