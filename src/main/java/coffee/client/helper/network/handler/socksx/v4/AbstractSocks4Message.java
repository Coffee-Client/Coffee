/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v4;


import coffee.client.helper.network.handler.socksx.AbstractSocksMessage;
import coffee.client.helper.network.handler.socksx.SocksVersion;

/**
 * An abstract {@link Socks4Message}.
 */
public abstract class AbstractSocks4Message extends AbstractSocksMessage implements Socks4Message {
    @Override
    public final SocksVersion version() {
        return SocksVersion.SOCKS4a;
    }
}
