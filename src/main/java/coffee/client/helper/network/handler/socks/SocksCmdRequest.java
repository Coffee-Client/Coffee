/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */
package coffee.client.helper.network.handler.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;

import java.net.IDN;

/**
 * An socks cmd request.
 *
 * @see SocksCmdResponse
 * @see SocksCmdRequestDecoder
 */
public final class SocksCmdRequest extends SocksRequest {
    private final SocksCmdType cmdType;
    private final SocksAddressType addressType;
    private final String host;
    private final int port;

    public SocksCmdRequest(SocksCmdType cmdType, SocksAddressType addressType, String host, int port) {
        super(SocksRequestType.CMD);
        String host1 = host;
        ObjectUtil.checkNotNull(cmdType, "cmdType");
        ObjectUtil.checkNotNull(addressType, "addressType");
        ObjectUtil.checkNotNull(host1, "host");

        switch (addressType) {
            case IPv4:
                if (!NetUtil.isValidIpV4Address(host1)) {
                    throw new IllegalArgumentException(host1 + " is not a valid IPv4 address");
                }
                break;
            case DOMAIN:
                String asciiHost = IDN.toASCII(host1);
                if (asciiHost.length() > 255) {
                    throw new IllegalArgumentException(host1 + " IDN: " + asciiHost + " exceeds 255 char limit");
                }
                host1 = asciiHost;
                break;
            case IPv6:
                if (!NetUtil.isValidIpV6Address(host1)) {
                    throw new IllegalArgumentException(host1 + " is not a valid IPv6 address");
                }
                break;
            case UNKNOWN:
                break;
        }
        if (port <= 0 || port >= 65536) {
            throw new IllegalArgumentException(port + " is not in bounds 0 < x < 65536");
        }
        this.cmdType = cmdType;
        this.addressType = addressType;
        this.host = host1;
        this.port = port;
    }

    /**
     * Returns the {@link SocksCmdType} of this {@link SocksCmdRequest}
     *
     * @return The {@link SocksCmdType} of this {@link SocksCmdRequest}
     */
    public SocksCmdType cmdType() {
        return cmdType;
    }

    /**
     * Returns the {@link SocksAddressType} of this {@link SocksCmdRequest}
     *
     * @return The {@link SocksAddressType} of this {@link SocksCmdRequest}
     */
    public SocksAddressType addressType() {
        return addressType;
    }

    /**
     * Returns host that is used as a parameter in {@link SocksCmdType}
     *
     * @return host that is used as a parameter in {@link SocksCmdType}
     */
    public String host() {
        return addressType == SocksAddressType.DOMAIN ? IDN.toUnicode(host) : host;
    }

    /**
     * Returns port that is used as a parameter in {@link SocksCmdType}
     *
     * @return port that is used as a parameter in {@link SocksCmdType}
     */
    public int port() {
        return port;
    }

    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte(protocolVersion().byteValue());
        byteBuf.writeByte(cmdType.byteValue());
        byteBuf.writeByte(0x00);
        byteBuf.writeByte(addressType.byteValue());
        switch (addressType) {
            case IPv4, IPv6 -> {
                byteBuf.writeBytes(NetUtil.createByteArrayFromIpAddressString(host));
                byteBuf.writeShort(port);
            }
            case DOMAIN -> {
                byteBuf.writeByte(host.length());
                byteBuf.writeCharSequence(host, CharsetUtil.US_ASCII);
                byteBuf.writeShort(port);
            }
        }
    }
}
