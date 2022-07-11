/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec2f;

@AllArgsConstructor
public class Rectangle {
    @Getter
    @Setter
    private double x, y, x1, y1;

    public boolean contains(double x, double y) {
        return x >= this.x && x < this.x1 && y >= this.y && y < this.y1;
    }

    public double getWidth() {
        return x1 - x;
    }

    public double getHeight() {
        return y1 - y;
    }

    public Rectangle multiplyWidthHeight(Vec2f multiplier) {
        double w = getWidth() * multiplier.x;
        double h = getHeight() * multiplier.y;
        setX1(getX() + w);
        setY1(getY() + h);
        return this;
    }
}
