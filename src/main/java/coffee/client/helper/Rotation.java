/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;

@AllArgsConstructor
@Setter
@Getter
public class Rotation implements Cloneable {
    private float pitch, yaw;

    public Rotation normalize() {
        this.pitch = MathHelper.wrapDegrees(this.pitch);
        this.yaw = MathHelper.wrapDegrees(this.yaw);
        return this;
    }

    @Override
    public Rotation clone() {
        try {
            return (Rotation) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
