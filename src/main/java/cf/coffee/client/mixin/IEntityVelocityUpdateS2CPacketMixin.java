/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface IEntityVelocityUpdateS2CPacketMixin {

    @Mutable
    @Accessor("velocityX")
    void setVelocityX(int velocityX);

    @Mutable
    @Accessor("velocityY")
    void setVelocityY(int velocityY);

    @Mutable
    @Accessor("velocityZ")
    void setVelocityZ(int velocityZ);
}
