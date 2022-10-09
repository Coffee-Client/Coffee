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
public class KeyboardEvent extends Event {
    int keycode;
    Type type;

    public enum Type {
        RELEASE, PRESS, REPEAT;

        public static Type of(int id) {
            Preconditions.checkElementIndex(id, 3);
            return Type.values()[id];
        }
    }
}
