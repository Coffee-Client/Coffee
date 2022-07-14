/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */
package coffee.client.helper.network.handler.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes {@link ByteBuf}s into {@link SocksAuthResponse}.
 * Before returning SocksResponse decoder removes itself from pipeline.
 */
public class SocksAuthResponseDecoder extends ReplayingDecoder<SocksAuthResponseDecoder.State> {

    public SocksAuthResponseDecoder() {
        super(State.CHECK_PROTOCOL_VERSION);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) {
        switch (state()) {
            case CHECK_PROTOCOL_VERSION: {
                if (byteBuf.readByte() != SocksSubnegotiationVersion.AUTH_PASSWORD.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
                    break;
                }
                checkpoint(State.READ_AUTH_RESPONSE);
            }
            case READ_AUTH_RESPONSE: {
                SocksAuthStatus authStatus = SocksAuthStatus.valueOf(byteBuf.readByte());
                out.add(new SocksAuthResponse(authStatus));
                break;
            }
            default: {
                throw new Error();
            }
        }
        channelHandlerContext.pipeline().remove(this);
    }

    @UnstableApi
    public enum State {
        CHECK_PROTOCOL_VERSION, READ_AUTH_RESPONSE
    }
}
