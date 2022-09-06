/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin.network;

import coffee.client.feature.gui.screen.ProxyManagerScreen;
import coffee.client.helper.network.proxy.Socks4ProxyHandler;
import coffee.client.helper.network.proxy.Socks5ProxyHandler;
import io.netty.channel.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(targets = "net/minecraft/network/ClientConnection$1")
public class ClientConnectionMixin1 {

    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("HEAD"))
    public void coffee_applyProxy(Channel channel, CallbackInfo ci) {
        ProxyManagerScreen.Proxy currentProxy = ProxyManagerScreen.currentProxy;
        if (currentProxy != null) {
            if (currentProxy.socks4()) {
                channel.pipeline().addFirst(new Socks4ProxyHandler(new InetSocketAddress(currentProxy.address(), currentProxy.port()), currentProxy.user()));
            } else {
                channel.pipeline()
                    .addFirst(new Socks5ProxyHandler(new InetSocketAddress(currentProxy.address(), currentProxy.port()), currentProxy.user(), currentProxy.pass()));
            }
        }
    }
}
