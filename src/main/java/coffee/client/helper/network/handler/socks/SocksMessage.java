/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */
package coffee.client.helper.network.handler.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;

/**
 * An abstract class that defines a SocksMessage, providing common properties for
 * {@link SocksRequest} and {@link SocksResponse}.
 *
 * @see SocksRequest
 * @see SocksResponse
 */

public abstract class SocksMessage {
    private final SocksMessageType type;
    private final SocksProtocolVersion protocolVersion = SocksProtocolVersion.SOCKS5;

    protected SocksMessage(SocksMessageType type) {
        this.type = ObjectUtil.checkNotNull(type, "type");
    }

    /**
     * Returns the {@link SocksMessageType} of this {@link SocksMessage}
     *
     * @return The {@link SocksMessageType} of this {@link SocksMessage}
     */
    public SocksMessageType type() {
        return type;
    }

    /**
     * Returns the {@link SocksProtocolVersion} of this {@link SocksMessage}
     *
     * @return The {@link SocksProtocolVersion} of this {@link SocksMessage}
     */
    public SocksProtocolVersion protocolVersion() {
        return protocolVersion;
    }

    /**
     * @deprecated Do not use; this method was intended for an internal use only.
     */
    @Deprecated
    public abstract void encodeAsByteBuf(ByteBuf byteBuf);
}
