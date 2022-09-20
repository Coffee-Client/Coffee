/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */
package coffee.client.helper.network.handler.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;

/**
 * An socks init response.
 *
 * @see SocksInitRequest
 * @see SocksInitResponseDecoder
 */
public final class SocksInitResponse extends SocksResponse {
    private final SocksAuthScheme authScheme;

    public SocksInitResponse(SocksAuthScheme authScheme) {
        super(SocksResponseType.INIT);
        this.authScheme = ObjectUtil.checkNotNull(authScheme, "authScheme");
    }

    /**
     * Returns the {@link SocksAuthScheme} of this {@link SocksInitResponse}
     *
     * @return The {@link SocksAuthScheme} of this {@link SocksInitResponse}
     */
    public SocksAuthScheme authScheme() {
        return authScheme;
    }

    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte(protocolVersion().byteValue());
        byteBuf.writeByte(authScheme.byteValue());
    }
}
