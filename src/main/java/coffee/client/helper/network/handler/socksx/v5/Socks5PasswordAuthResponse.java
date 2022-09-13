/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */
package coffee.client.helper.network.handler.socksx.v5;

/**
 * A SOCKS5 subnegotiation response for username-password authentication, as defined in
 * <a href="https://tools.ietf.org/html/rfc1929#section-2">the section 2, RFC1929</a>.
 */
public interface Socks5PasswordAuthResponse extends Socks5Message {
    /**
     * Returns the status of this response.
     */
    Socks5PasswordAuthStatus status();
}
