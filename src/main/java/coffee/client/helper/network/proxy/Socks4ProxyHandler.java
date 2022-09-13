/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.network.proxy;

import coffee.client.helper.network.handler.socksx.v4.DefaultSocks4CommandRequest;
import coffee.client.helper.network.handler.socksx.v4.Socks4ClientDecoder;
import coffee.client.helper.network.handler.socksx.v4.Socks4ClientEncoder;
import coffee.client.helper.network.handler.socksx.v4.Socks4CommandResponse;
import coffee.client.helper.network.handler.socksx.v4.Socks4CommandStatus;
import coffee.client.helper.network.handler.socksx.v4.Socks4CommandType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class Socks4ProxyHandler extends ProxyHandler {

    private static final String PROTOCOL = "socks4";
    private static final String AUTH_USERNAME = "username";

    private final String username;

    private String decoderName;
    private String encoderName;

    public Socks4ProxyHandler(SocketAddress proxyAddress) {
        this(proxyAddress, null);
    }

    public Socks4ProxyHandler(SocketAddress proxyAddress, String username) {
        super(proxyAddress);
        String username1 = username;
        if (username1 != null && username1.isEmpty()) {
            username1 = null;
        }
        this.username = username1;
    }

    @Override
    public String protocol() {
        return PROTOCOL;
    }

    @Override
    public String authScheme() {
        return username != null ? AUTH_USERNAME : AUTH_NONE;
    }

    public String username() {
        return username;
    }

    @Override
    protected void addCodec(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        String name = ctx.name();

        Socks4ClientDecoder decoder = new Socks4ClientDecoder();
        p.addBefore(name, null, decoder);

        decoderName = p.context(decoder).name();
        encoderName = decoderName + ".encoder";

        p.addBefore(name, encoderName, Socks4ClientEncoder.INSTANCE);
    }

    @Override
    protected void removeEncoder(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.remove(encoderName);
    }

    @Override
    protected void removeDecoder(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.remove(decoderName);
    }

    @Override
    protected Object newInitialMessage(ChannelHandlerContext ctx) {
        InetSocketAddress raddr = destinationAddress();
        String rhost;
        if (raddr.isUnresolved()) {
            rhost = raddr.getHostString();
        } else {
            rhost = raddr.getAddress().getHostAddress();
        }
        return new DefaultSocks4CommandRequest(Socks4CommandType.CONNECT, rhost, raddr.getPort(), username != null ? username : "");
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        final Socks4CommandResponse res = (Socks4CommandResponse) response;
        final Socks4CommandStatus status = res.status();
        if (status == Socks4CommandStatus.SUCCESS) {
            return true;
        }

        throw new ProxyConnectException(exceptionMessage("status: " + status));
    }
}
