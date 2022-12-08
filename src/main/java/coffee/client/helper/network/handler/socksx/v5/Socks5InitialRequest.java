/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */
package coffee.client.helper.network.handler.socksx.v5;

import java.util.List;

/**
 * An initial SOCKS5 authentication method selection request, as defined in
 * <a href="https://tools.ietf.org/html/rfc1928#section-3">the section 3, RFC1928</a>.
 */
public interface Socks5InitialRequest extends Socks5Message {
    /**
     * Returns the list of desired authentication methods.
     */
    List<Socks5AuthMethod> authMethods();
}
