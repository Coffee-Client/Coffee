/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */
package coffee.client.helper.network.handler.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.IDN;

/**
 * The default {@link Socks5CommandResponse}.
 */
public final class DefaultSocks5CommandResponse extends AbstractSocks5Message implements Socks5CommandResponse {

    private final Socks5CommandStatus status;
    private final Socks5AddressType bndAddrType;
    private final String bndAddr;
    private final int bndPort;

    public DefaultSocks5CommandResponse(Socks5CommandStatus status, Socks5AddressType bndAddrType) {
        this(status, bndAddrType, null, 0);
    }

    public DefaultSocks5CommandResponse(Socks5CommandStatus status, Socks5AddressType bndAddrType, String bndAddr, int bndPort) {

        String bndAddr1 = bndAddr;
        ObjectUtil.checkNotNull(status, "status");
        ObjectUtil.checkNotNull(bndAddrType, "bndAddrType");

        if (bndAddr1 != null) {
            if (bndAddrType == Socks5AddressType.IPv4) {
                if (!NetUtil.isValidIpV4Address(bndAddr1)) {
                    throw new IllegalArgumentException("bndAddr: " + bndAddr1 + " (expected: a valid IPv4 address)");
                }
            } else if (bndAddrType == Socks5AddressType.DOMAIN) {
                bndAddr1 = IDN.toASCII(bndAddr1);
                if (bndAddr1.length() > 255) {
                    throw new IllegalArgumentException("bndAddr: " + bndAddr1 + " (expected: less than 256 chars)");
                }
            } else if (bndAddrType == Socks5AddressType.IPv6) {
                if (!NetUtil.isValidIpV6Address(bndAddr1)) {
                    throw new IllegalArgumentException("bndAddr: " + bndAddr1 + " (expected: a valid IPv6 address)");
                }
            }
        }

        if (bndPort < 0 || bndPort > 65535) {
            throw new IllegalArgumentException("bndPort: " + bndPort + " (expected: 0~65535)");
        }
        this.status = status;
        this.bndAddrType = bndAddrType;
        this.bndAddr = bndAddr1;
        this.bndPort = bndPort;
    }

    @Override
    public Socks5CommandStatus status() {
        return status;
    }

    @Override
    public Socks5AddressType bndAddrType() {
        return bndAddrType;
    }

    @Override
    public String bndAddr() {
        return bndAddr;
    }

    @Override
    public int bndPort() {
        return bndPort;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append(StringUtil.simpleClassName(this));

        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", status: ");
        } else {
            buf.append("(status: ");
        }
        buf.append(status());
        buf.append(", bndAddrType: ");
        buf.append(bndAddrType());
        buf.append(", bndAddr: ");
        buf.append(bndAddr());
        buf.append(", bndPort: ");
        buf.append(bndPort());
        buf.append(')');

        return buf.toString();
    }
}
