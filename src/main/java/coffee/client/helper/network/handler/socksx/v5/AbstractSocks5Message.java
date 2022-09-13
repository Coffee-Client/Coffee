/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.network.handler.socksx.v5;

import coffee.client.helper.network.handler.socksx.AbstractSocksMessage;
import coffee.client.helper.network.handler.socksx.SocksVersion;

/**
 * An abstract {@link Socks5Message}.
 */
public abstract class AbstractSocks5Message extends AbstractSocksMessage implements Socks5Message {
    @Override
    public final SocksVersion version() {
        return SocksVersion.SOCKS5;
    }
}
