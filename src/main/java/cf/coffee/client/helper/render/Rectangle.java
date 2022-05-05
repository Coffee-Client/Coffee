/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Rectangle {
    @Getter
    @Setter
    private double x, y, x1, y1;

    public boolean contains(double x, double y) {
        return x >= this.x && x <= this.x1 && y >= this.y && y <= this.y1;
    }
}
