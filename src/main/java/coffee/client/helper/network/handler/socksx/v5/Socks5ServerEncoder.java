/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * Encodes a server-side {@link Socks5Message} into a {@link ByteBuf}.
 */
@Sharable
public class Socks5ServerEncoder extends MessageToByteEncoder<Socks5Message> {

    public static final Socks5ServerEncoder DEFAULT = new Socks5ServerEncoder(Socks5AddressEncoder.DEFAULT);

    private final Socks5AddressEncoder addressEncoder;

    /**
     * Creates a new instance with the default {@link Socks5AddressEncoder}.
     */
    protected Socks5ServerEncoder() {
        this(Socks5AddressEncoder.DEFAULT);
    }

    /**
     * Creates a new instance with the specified {@link Socks5AddressEncoder}.
     */
    public Socks5ServerEncoder(Socks5AddressEncoder addressEncoder) {
        this.addressEncoder = ObjectUtil.checkNotNull(addressEncoder, "addressEncoder");
    }

    private static void encodeAuthMethodResponse(Socks5InitialResponse msg, ByteBuf out) {
        out.writeByte(msg.version().byteValue());
        out.writeByte(msg.authMethod().byteValue());
    }

    private static void encodePasswordAuthResponse(Socks5PasswordAuthResponse msg, ByteBuf out) {
        out.writeByte(0x01);
        out.writeByte(msg.status().byteValue());
    }

    /**
     * Returns the {@link Socks5AddressEncoder} of this encoder.
     */
    protected final Socks5AddressEncoder addressEncoder() {
        return addressEncoder;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Socks5Message msg, ByteBuf out) throws Exception {
        if (msg instanceof Socks5InitialResponse) {
            encodeAuthMethodResponse((Socks5InitialResponse) msg, out);
        } else if (msg instanceof Socks5PasswordAuthResponse) {
            encodePasswordAuthResponse((Socks5PasswordAuthResponse) msg, out);
        } else if (msg instanceof Socks5CommandResponse) {
            encodeCommandResponse((Socks5CommandResponse) msg, out);
        } else {
            throw new EncoderException("unsupported message type: " + StringUtil.simpleClassName(msg));
        }
    }

    private void encodeCommandResponse(Socks5CommandResponse msg, ByteBuf out) throws Exception {
        out.writeByte(msg.version().byteValue());
        out.writeByte(msg.status().byteValue());
        out.writeByte(0x00);

        final Socks5AddressType bndAddrType = msg.bndAddrType();
        out.writeByte(bndAddrType.byteValue());
        addressEncoder.encodeAddress(bndAddrType, msg.bndAddr(), out);

        out.writeShort(msg.bndPort());
    }
}
