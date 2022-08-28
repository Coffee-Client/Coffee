/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v4;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.NetUtil;

/**
 * Encodes a {@link Socks4CommandResponse} into a {@link ByteBuf}.
 */
@Sharable
public final class Socks4ServerEncoder extends MessageToByteEncoder<Socks4CommandResponse> {

    public static final Socks4ServerEncoder INSTANCE = new Socks4ServerEncoder();

    private static final byte[] IPv4_HOSTNAME_ZEROED = { 0x00, 0x00, 0x00, 0x00 };

    private Socks4ServerEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Socks4CommandResponse msg, ByteBuf out) {
        out.writeByte(0);
        out.writeByte(msg.status().byteValue());
        out.writeShort(msg.dstPort());
        out.writeBytes(msg.dstAddr() == null ? IPv4_HOSTNAME_ZEROED : NetUtil.createByteArrayFromIpAddressString(msg.dstAddr()));
    }
}
