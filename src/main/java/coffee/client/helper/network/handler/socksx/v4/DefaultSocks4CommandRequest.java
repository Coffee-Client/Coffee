/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */
package coffee.client.helper.network.handler.socksx.v4;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.IDN;

/**
 * The default {@link Socks4CommandRequest}.
 */
public class DefaultSocks4CommandRequest extends AbstractSocks4Message implements Socks4CommandRequest {

    private final Socks4CommandType type;
    private final String dstAddr;
    private final int dstPort;
    private final String userId;

    /**
     * Creates a new instance.
     *
     * @param type    the type of the request
     * @param dstAddr the {@code DSTIP} field of the request
     * @param dstPort the {@code DSTPORT} field of the request
     */
    public DefaultSocks4CommandRequest(Socks4CommandType type, String dstAddr, int dstPort) {
        this(type, dstAddr, dstPort, "");
    }

    /**
     * Creates a new instance.
     *
     * @param type    the type of the request
     * @param dstAddr the {@code DSTIP} field of the request
     * @param dstPort the {@code DSTPORT} field of the request
     * @param userId  the {@code USERID} field of the request
     */
    public DefaultSocks4CommandRequest(Socks4CommandType type, String dstAddr, int dstPort, String userId) {
        if (dstPort <= 0 || dstPort >= 65536) {
            throw new IllegalArgumentException("dstPort: " + dstPort + " (expected: 1~65535)");
        }
        this.type = ObjectUtil.checkNotNull(type, "type");
        this.dstAddr = IDN.toASCII(ObjectUtil.checkNotNull(dstAddr, "dstAddr"));
        this.userId = ObjectUtil.checkNotNull(userId, "userId");
        this.dstPort = dstPort;
    }

    @Override
    public Socks4CommandType type() {
        return type;
    }

    @Override
    public String dstAddr() {
        return dstAddr;
    }

    @Override
    public int dstPort() {
        return dstPort;
    }

    @Override
    public String userId() {
        return userId;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append(StringUtil.simpleClassName(this));

        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", type: ");
        } else {
            buf.append("(type: ");
        }
        buf.append(type());
        buf.append(", dstAddr: ");
        buf.append(dstAddr());
        buf.append(", dstPort: ");
        buf.append(dstPort());
        buf.append(", userId: ");
        buf.append(userId());
        buf.append(')');

        return buf.toString();
    }
}
