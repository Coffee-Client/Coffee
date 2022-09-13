/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */
package coffee.client.helper.network.handler.socksx.v5;

/**
 * An initial SOCKS5 authentication method selection request, as defined in
 * <a href="https://tools.ietf.org/html/rfc1928#section-3">the section 3, RFC1928</a>.
 */
public interface Socks5InitialResponse extends Socks5Message {

    /**
     * Returns the {@code METHOD} field of this response.
     */
    Socks5AuthMethod authMethod();
}
