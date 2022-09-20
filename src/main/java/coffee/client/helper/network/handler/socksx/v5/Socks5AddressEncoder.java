/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;

/**
 * Encodes a SOCKS5 address into binary representation.
 *
 * @see Socks5ClientEncoder
 * @see Socks5ServerEncoder
 */
public interface Socks5AddressEncoder {

    Socks5AddressEncoder DEFAULT = (addrType, addrValue, out) -> {
        final byte typeVal = addrType.byteValue();
        if (typeVal == Socks5AddressType.IPv4.byteValue()) {
            if (addrValue != null) {
                out.writeBytes(NetUtil.createByteArrayFromIpAddressString(addrValue));
            } else {
                out.writeInt(0);
            }
        } else if (typeVal == Socks5AddressType.DOMAIN.byteValue()) {
            if (addrValue != null) {
                out.writeByte(addrValue.length());
                out.writeCharSequence(addrValue, CharsetUtil.US_ASCII);
            } else {
                out.writeByte(0);
            }
        } else if (typeVal == Socks5AddressType.IPv6.byteValue()) {
            if (addrValue != null) {
                out.writeBytes(NetUtil.createByteArrayFromIpAddressString(addrValue));
            } else {
                out.writeLong(0);
                out.writeLong(0);
            }
        } else {
            throw new EncoderException("unsupported addrType: " + (addrType.byteValue() & 0xFF));
        }
    };

    /**
     * Encodes a SOCKS5 address.
     *
     * @param addrType  the type of the address
     * @param addrValue the string representation of the address
     * @param out       the output buffer where the encoded SOCKS5 address field will be written to
     */
    void encodeAddress(Socks5AddressType addrType, String addrValue, ByteBuf out);
}
