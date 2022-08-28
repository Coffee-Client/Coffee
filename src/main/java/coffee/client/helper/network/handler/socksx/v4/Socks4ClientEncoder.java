/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.NetUtil;

/**
 * Encodes a {@link Socks4CommandRequest} into a {@link ByteBuf}.
 */
@Sharable
public final class Socks4ClientEncoder extends MessageToByteEncoder<Socks4CommandRequest> {

    /**
     * The singleton instance of {@link Socks4ClientEncoder}
     */
    public static final Socks4ClientEncoder INSTANCE = new Socks4ClientEncoder();

    private static final byte[] IPv4_DOMAIN_MARKER = {0x00, 0x00, 0x00, 0x01};

    private Socks4ClientEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Socks4CommandRequest msg, ByteBuf out) {
        out.writeByte(msg.version().byteValue());
        out.writeByte(msg.type().byteValue());
        out.writeShort(msg.dstPort());
        if (NetUtil.isValidIpV4Address(msg.dstAddr())) {
            out.writeBytes(NetUtil.createByteArrayFromIpAddressString(msg.dstAddr()));
            ByteBufUtil.writeAscii(out, msg.userId());
            out.writeByte(0);
        } else {
            out.writeBytes(IPv4_DOMAIN_MARKER);
            ByteBufUtil.writeAscii(out, msg.userId());
            out.writeByte(0);
            ByteBufUtil.writeAscii(out, msg.dstAddr());
            out.writeByte(0);
        }
    }
}
