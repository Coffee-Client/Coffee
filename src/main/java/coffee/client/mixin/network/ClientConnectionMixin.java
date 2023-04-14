/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.network;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.misc.AntiPacketKick;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.PacketEvent;
import coffee.client.helper.util.Utils;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void coffee_handlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        PacketEvent.Received pe = new PacketEvent.Received(packet);
        EventSystem.manager.send(pe);
        if (pe.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    public void coffee_preventThrow(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        ex.printStackTrace();
        if (ModuleRegistry.getByClass(AntiPacketKick.class).isEnabled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"))
    void coffee_replacePacket(ClientConnection instance, Packet<?> packet, PacketCallbacks callbacks) {
        if (!Utils.sendPackets) {
            return;
        }
        PacketEvent.Sent pe = new PacketEvent.Sent(packet);
        EventSystem.manager.send(pe);
        if (!pe.isCancelled()) {
            instance.send(pe.getPacket(), callbacks); // send either replaced or modified packet
        }
    }

}
