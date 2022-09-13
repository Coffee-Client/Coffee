/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.network.proxy;

import coffee.client.helper.network.handler.socksx.v5.DefaultSocks5CommandRequest;
import coffee.client.helper.network.handler.socksx.v5.DefaultSocks5InitialRequest;
import coffee.client.helper.network.handler.socksx.v5.DefaultSocks5PasswordAuthRequest;
import coffee.client.helper.network.handler.socksx.v5.Socks5AddressType;
import coffee.client.helper.network.handler.socksx.v5.Socks5AuthMethod;
import coffee.client.helper.network.handler.socksx.v5.Socks5ClientEncoder;
import coffee.client.helper.network.handler.socksx.v5.Socks5CommandResponse;
import coffee.client.helper.network.handler.socksx.v5.Socks5CommandResponseDecoder;
import coffee.client.helper.network.handler.socksx.v5.Socks5CommandStatus;
import coffee.client.helper.network.handler.socksx.v5.Socks5CommandType;
import coffee.client.helper.network.handler.socksx.v5.Socks5InitialRequest;
import coffee.client.helper.network.handler.socksx.v5.Socks5InitialResponse;
import coffee.client.helper.network.handler.socksx.v5.Socks5InitialResponseDecoder;
import coffee.client.helper.network.handler.socksx.v5.Socks5PasswordAuthResponse;
import coffee.client.helper.network.handler.socksx.v5.Socks5PasswordAuthResponseDecoder;
import coffee.client.helper.network.handler.socksx.v5.Socks5PasswordAuthStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.NetUtil;
import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;

public final class Socks5ProxyHandler extends ProxyHandler {

    private static final String PROTOCOL = "socks5";
    private static final String AUTH_PASSWORD = "password";

    private static final Socks5InitialRequest INIT_REQUEST_NO_AUTH = new DefaultSocks5InitialRequest(Collections.singletonList(Socks5AuthMethod.NO_AUTH));

    private static final Socks5InitialRequest INIT_REQUEST_PASSWORD = new DefaultSocks5InitialRequest(Arrays.asList(Socks5AuthMethod.NO_AUTH, Socks5AuthMethod.PASSWORD));

    private final String username;
    private final String password;

    private String decoderName;
    private String encoderName;

    public Socks5ProxyHandler(SocketAddress proxyAddress) {
        this(proxyAddress, null, null);
    }

    public Socks5ProxyHandler(SocketAddress proxyAddress, String username, String password) {
        super(proxyAddress);
        String username1 = username;
        String password1 = password;
        if (username1 != null && username1.isEmpty()) {
            username1 = null;
        }
        if (password1 != null && password1.isEmpty()) {
            password1 = null;
        }
        this.username = username1;
        this.password = password1;
    }

    @Override
    public String protocol() {
        return PROTOCOL;
    }

    @Override
    public String authScheme() {
        return socksAuthMethod() == Socks5AuthMethod.PASSWORD ? AUTH_PASSWORD : AUTH_NONE;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    @Override
    protected void addCodec(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        String name = ctx.name();

        Socks5InitialResponseDecoder decoder = new Socks5InitialResponseDecoder();
        p.addBefore(name, null, decoder);

        decoderName = p.context(decoder).name();
        encoderName = decoderName + ".encoder";

        p.addBefore(name, encoderName, Socks5ClientEncoder.DEFAULT);
    }

    @Override
    protected void removeEncoder(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(encoderName);
    }

    @Override
    protected void removeDecoder(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        if (p.context(decoderName) != null) {
            p.remove(decoderName);
        }
    }

    @Override
    protected Object newInitialMessage(ChannelHandlerContext ctx) {
        return socksAuthMethod() == Socks5AuthMethod.PASSWORD ? INIT_REQUEST_PASSWORD : INIT_REQUEST_NO_AUTH;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        if (response instanceof Socks5InitialResponse res) {
            Socks5AuthMethod authMethod = socksAuthMethod();

            if (res.authMethod() != Socks5AuthMethod.NO_AUTH && res.authMethod() != authMethod) {
                // Server did not allow unauthenticated access nor accept the requested authentication scheme.
                throw new ProxyConnectException(exceptionMessage("unexpected authMethod: " + res.authMethod()));
            }

            if (authMethod == Socks5AuthMethod.NO_AUTH) {
                sendConnectCommand(ctx);
            } else if (authMethod == Socks5AuthMethod.PASSWORD) {
                // In case of password authentication, send an authentication request.
                ctx.pipeline().replace(decoderName, decoderName, new Socks5PasswordAuthResponseDecoder());
                sendToProxyServer(new DefaultSocks5PasswordAuthRequest(username != null ? username : "", password != null ? password : ""));
            } else {
                // Should never reach here.
                throw new Error();
            }

            return false;
        }

        if (response instanceof Socks5PasswordAuthResponse res) {
            // Received an authentication response from the server.
            if (res.status() != Socks5PasswordAuthStatus.SUCCESS) {
                throw new ProxyConnectException(exceptionMessage("authStatus: " + res.status()));
            }

            sendConnectCommand(ctx);
            return false;
        }

        // This should be the last message from the server.
        Socks5CommandResponse res = (Socks5CommandResponse) response;
        if (res.status() != Socks5CommandStatus.SUCCESS) {
            throw new ProxyConnectException(exceptionMessage("status: " + res.status()));
        }

        return true;
    }

    private Socks5AuthMethod socksAuthMethod() {
        Socks5AuthMethod authMethod;
        if (username == null && password == null) {
            authMethod = Socks5AuthMethod.NO_AUTH;
        } else {
            authMethod = Socks5AuthMethod.PASSWORD;
        }
        return authMethod;
    }

    private void sendConnectCommand(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress raddr = destinationAddress();
        Socks5AddressType addrType;
        String rhost;
        if (raddr.isUnresolved()) {
            addrType = Socks5AddressType.DOMAIN;
            rhost = raddr.getHostString();
        } else {
            rhost = raddr.getAddress().getHostAddress();
            if (NetUtil.isValidIpV4Address(rhost)) {
                addrType = Socks5AddressType.IPv4;
            } else if (NetUtil.isValidIpV6Address(rhost)) {
                addrType = Socks5AddressType.IPv6;
            } else {
                throw new ProxyConnectException(exceptionMessage("unknown address type: " + StringUtil.simpleClassName(rhost)));
            }
        }

        ctx.pipeline().replace(decoderName, decoderName, new Socks5CommandResponseDecoder());
        sendToProxyServer(new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, addrType, rhost, raddr.getPort()));
    }
}
