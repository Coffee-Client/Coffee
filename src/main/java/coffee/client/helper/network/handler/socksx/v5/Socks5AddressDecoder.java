/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;

/**
 * Decodes a SOCKS5 address field into its string representation.
 *
 * @see Socks5CommandRequestDecoder
 * @see Socks5CommandResponseDecoder
 */
public interface Socks5AddressDecoder {

    Socks5AddressDecoder DEFAULT = new Socks5AddressDecoder() {

        private static final int IPv6_LEN = 16;

        @Override
        public String decodeAddress(Socks5AddressType addrType, ByteBuf in) {
            if (addrType == Socks5AddressType.IPv4) {
                return NetUtil.intToIpAddress(in.readInt());
            }
            if (addrType == Socks5AddressType.DOMAIN) {
                final int length = in.readUnsignedByte();
                final String domain = in.toString(in.readerIndex(), length, CharsetUtil.US_ASCII);
                in.skipBytes(length);
                return domain;
            }
            if (addrType == Socks5AddressType.IPv6) {
                if (in.hasArray()) {
                    final int readerIdx = in.readerIndex();
                    in.readerIndex(readerIdx + IPv6_LEN);
                    return NetUtil.bytesToIpAddress(in.array(), in.arrayOffset() + readerIdx, IPv6_LEN);
                } else {
                    byte[] tmp = new byte[IPv6_LEN];
                    in.readBytes(tmp);
                    return NetUtil.bytesToIpAddress(tmp);
                }
            } else {
                throw new DecoderException("unsupported address type: " + (addrType.byteValue() & 0xFF));
            }
        }
    };

    /**
     * Decodes a SOCKS5 address field into its string representation.
     *
     * @param addrType the type of the address
     * @param in       the input buffer which contains the SOCKS5 address field at its reader index
     */
    String decodeAddress(Socks5AddressType addrType, ByteBuf in);
}
