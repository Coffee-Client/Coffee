/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomPayloadC2SPacket.class)
public interface ICustomPayloadC2SPacketAccessor {

    @Accessor("channel")
    Identifier getChannel();

    @Accessor("data")
    PacketByteBuf getData();

    @Mutable
    @Accessor("data")
    void setData(PacketByteBuf newValue);
}
