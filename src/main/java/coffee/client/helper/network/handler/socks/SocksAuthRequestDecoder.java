/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */
package coffee.client.helper.network.handler.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes {@link ByteBuf}s into {@link SocksAuthRequest}.
 * Before returning SocksRequest decoder removes itself from pipeline.
 */
public class SocksAuthRequestDecoder extends ReplayingDecoder<SocksAuthRequestDecoder.State> {

    private String username;

    public SocksAuthRequestDecoder() {
        super(State.CHECK_PROTOCOL_VERSION);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        switch (state()) {
            case CHECK_PROTOCOL_VERSION: {
                if (byteBuf.readByte() != SocksSubnegotiationVersion.AUTH_PASSWORD.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
                    break;
                }
                checkpoint(State.READ_USERNAME);
            }
            case READ_USERNAME: {
                int fieldLength = byteBuf.readByte();
                username = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
                checkpoint(State.READ_PASSWORD);
            }
            case READ_PASSWORD: {
                int fieldLength = byteBuf.readByte();
                String password = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
                out.add(new SocksAuthRequest(username, password));
                break;
            }
            default: {
                throw new Error();
            }
        }
        ctx.pipeline().remove(this);
    }

    @UnstableApi
    public enum State {
        CHECK_PROTOCOL_VERSION, READ_USERNAME, READ_PASSWORD
    }
}
