/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v5;

import coffee.client.helper.network.handler.socksx.SocksVersion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes a single {@link Socks5InitialResponse} from the inbound {@link ByteBuf}s.
 * On successful decode, this decoder will forward the received data to the next handler, so that
 * other handler can remove or replace this decoder later.  On failed decode, this decoder will
 * discard the received data, so that other handler closes the connection later.
 */
public class Socks5InitialResponseDecoder extends ReplayingDecoder<Socks5InitialResponseDecoder.State> {

    public Socks5InitialResponseDecoder() {
        super(State.INIT);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            switch (state()) {
                case INIT: {
                    final byte version = in.readByte();
                    if (version != SocksVersion.SOCKS5.byteValue()) {
                        throw new DecoderException("unsupported version: " + version + " (expected: " + SocksVersion.SOCKS5.byteValue() + ')');
                    }

                    final Socks5AuthMethod authMethod = Socks5AuthMethod.valueOf(in.readByte());
                    out.add(new DefaultSocks5InitialResponse(authMethod));
                    checkpoint(State.SUCCESS);
                }
                case SUCCESS: {
                    int readableBytes = actualReadableBytes();
                    if (readableBytes > 0) {
                        out.add(in.readRetainedSlice(readableBytes));
                    }
                    break;
                }
                case FAILURE: {
                    in.skipBytes(actualReadableBytes());
                    break;
                }
            }
        } catch (Exception e) {
            fail(out, e);
        }
    }

    private void fail(List<Object> out, Exception cause) {
        Exception cause1 = cause;
        if (!(cause1 instanceof DecoderException)) {
            cause1 = new DecoderException(cause1);
        }

        checkpoint(State.FAILURE);

        Socks5Message m = new DefaultSocks5InitialResponse(Socks5AuthMethod.UNACCEPTED);
        m.setDecoderResult(DecoderResult.failure(cause1));
        out.add(m);
    }

    @UnstableApi
    public enum State {
        INIT, SUCCESS, FAILURE
    }
}
