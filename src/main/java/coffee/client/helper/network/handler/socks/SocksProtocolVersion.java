/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.network.handler.socks;

public enum SocksProtocolVersion {
    SOCKS4a((byte) 0x04), SOCKS5((byte) 0x05), UNKNOWN((byte) 0xff);

    private final byte b;

    SocksProtocolVersion(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksProtocolVersion fromByte(byte b) {
        return valueOf(b);
    }

    public static SocksProtocolVersion valueOf(byte b) {
        for (SocksProtocolVersion code : values()) {
            if (code.b == b) {
                return code;
            }
        }
        return UNKNOWN;
    }

    public byte byteValue() {
        return b;
    }
}
