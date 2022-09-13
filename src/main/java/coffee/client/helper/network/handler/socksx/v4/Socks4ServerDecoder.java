/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */
package coffee.client.helper.network.handler.socksx.v4;

import coffee.client.helper.network.handler.socksx.SocksVersion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes a single {@link Socks4CommandRequest} from the inbound {@link ByteBuf}s.
 * On successful decode, this decoder will forward the received data to the next handler, so that
 * other handler can remove this decoder later.  On failed decode, this decoder will discard the
 * received data, so that other handler closes the connection later.
 */
public class Socks4ServerDecoder extends ReplayingDecoder<Socks4ServerDecoder.State> {

    private static final int MAX_FIELD_LENGTH = 255;
    private Socks4CommandType type;
    private String dstAddr;
    private int dstPort;
    private String userId;

    public Socks4ServerDecoder() {
        super(State.START);
        setSingleDecode(true);
    }

    /**
     * Reads a variable-length NUL-terminated string as defined in SOCKS4.
     */
    private static String readString(String fieldName, ByteBuf in) {
        int length = in.bytesBefore(MAX_FIELD_LENGTH + 1, (byte) 0);
        if (length < 0) {
            throw new DecoderException("field '" + fieldName + "' longer than " + MAX_FIELD_LENGTH + " chars");
        }

        String value = in.readSlice(length).toString(CharsetUtil.US_ASCII);
        in.skipBytes(1); // Skip the NUL.

        return value;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            switch (state()) {
                case START: {
                    final int version = in.readUnsignedByte();
                    if (version != SocksVersion.SOCKS4a.byteValue()) {
                        throw new DecoderException("unsupported protocol version: " + version);
                    }

                    type = Socks4CommandType.valueOf(in.readByte());
                    dstPort = in.readUnsignedShort();
                    dstAddr = NetUtil.intToIpAddress(in.readInt());
                    checkpoint(State.READ_USERID);
                }
                case READ_USERID: {
                    userId = readString("userid", in);
                    checkpoint(State.READ_DOMAIN);
                }
                case READ_DOMAIN: {
                    // Check for Socks4a protocol marker 0.0.0.x
                    if (!"0.0.0.0".equals(dstAddr) && dstAddr.startsWith("0.0.0.")) {
                        dstAddr = readString("dstAddr", in);
                    }
                    out.add(new DefaultSocks4CommandRequest(type, dstAddr, dstPort, userId));
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

        Socks4CommandRequest m = new DefaultSocks4CommandRequest(type != null ? type : Socks4CommandType.CONNECT,
            dstAddr != null ? dstAddr : "",
            dstPort != 0 ? dstPort : 65535,
            userId != null ? userId : "");

        m.setDecoderResult(DecoderResult.failure(cause1));
        out.add(m);

        checkpoint(State.FAILURE);
    }

    @UnstableApi
    public enum State {
        START, READ_USERID, READ_DOMAIN, SUCCESS, FAILURE
    }
}
