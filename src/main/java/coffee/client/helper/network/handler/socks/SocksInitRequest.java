/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */
package coffee.client.helper.network.handler.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;

import java.util.Collections;
import java.util.List;

/**
 * An socks init request.
 *
 * @see SocksInitResponse
 * @see SocksInitRequestDecoder
 */
public final class SocksInitRequest extends SocksRequest {
    private final List<SocksAuthScheme> authSchemes;

    public SocksInitRequest(List<SocksAuthScheme> authSchemes) {
        super(SocksRequestType.INIT);
        this.authSchemes = ObjectUtil.checkNotNull(authSchemes, "authSchemes");
    }

    /**
     * Returns the List<{@link SocksAuthScheme}> of this {@link SocksInitRequest}
     *
     * @return The List<{@link SocksAuthScheme}> of this {@link SocksInitRequest}
     */
    public List<SocksAuthScheme> authSchemes() {
        return Collections.unmodifiableList(authSchemes);
    }

    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte(protocolVersion().byteValue());
        byteBuf.writeByte(authSchemes.size());
        for (SocksAuthScheme authScheme : authSchemes) {
            byteBuf.writeByte(authScheme.byteValue());
        }
    }
}
