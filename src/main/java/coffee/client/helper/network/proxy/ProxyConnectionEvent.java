/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.network.proxy;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.SocketAddress;

public final class ProxyConnectionEvent {

    private final String protocol;
    private final String authScheme;
    private final SocketAddress proxyAddress;
    private final SocketAddress destinationAddress;
    private String strVal;

    /**
     * Creates a new event that indicates a successful connection attempt to the destination address.
     */
    public ProxyConnectionEvent(String protocol, String authScheme, SocketAddress proxyAddress, SocketAddress destinationAddress) {
        this.protocol = ObjectUtil.checkNotNull(protocol, "protocol");
        this.authScheme = ObjectUtil.checkNotNull(authScheme, "authScheme");
        this.proxyAddress = ObjectUtil.checkNotNull(proxyAddress, "proxyAddress");
        this.destinationAddress = ObjectUtil.checkNotNull(destinationAddress, "destinationAddress");
    }

    /**
     * Returns the name of the proxy protocol in use.
     */
    public String protocol() {
        return protocol;
    }

    /**
     * Returns the name of the authentication scheme in use.
     */
    public String authScheme() {
        return authScheme;
    }

    /**
     * Returns the address of the proxy server.
     */
    @SuppressWarnings("unchecked")
    public <T extends SocketAddress> T proxyAddress() {
        return (T) proxyAddress;
    }

    /**
     * Returns the address of the destination.
     */
    @SuppressWarnings("unchecked")
    public <T extends SocketAddress> T destinationAddress() {
        return (T) destinationAddress;
    }

    @Override
    public String toString() {
        if (strVal != null) {
            return strVal;
        }

        String buf = StringUtil.simpleClassName(this) + '(' + protocol + ", " + authScheme + ", " + proxyAddress + " => " + destinationAddress + ')';

        return strVal = buf;
    }
}
