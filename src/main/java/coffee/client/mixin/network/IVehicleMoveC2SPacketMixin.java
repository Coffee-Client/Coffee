/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.network;

import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VehicleMoveC2SPacket.class)
public interface IVehicleMoveC2SPacketMixin {
    @Mutable
    @Accessor("x")
    void setX(double x);

    @Mutable
    @Accessor("y")
    void setY(double x);

    @Mutable
    @Accessor("z")
    void setZ(double x);
}
