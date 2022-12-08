/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.network.handler.socksx;

/**
 * The version of SOCKS protocol.
 */
public enum SocksVersion {
    /**
     * SOCKS protocol version 4a (or 4)
     */
    SOCKS4a((byte) 0x04),
    /**
     * SOCKS protocol version 5
     */
    SOCKS5((byte) 0x05),
    /**
     * Unknown protocol version
     */
    UNKNOWN((byte) 0xff);

    private final byte b;

    SocksVersion(byte b) {
        this.b = b;
    }

    /**
     * Returns the {@link SocksVersion} that corresponds to the specified version field value,
     * as defined in the protocol specification.
     *
     * @return {@link #UNKNOWN} if the specified value does not represent a known SOCKS protocol version
     */
    public static SocksVersion valueOf(byte b) {
        if (b == SOCKS4a.byteValue()) {
            return SOCKS4a;
        }
        if (b == SOCKS5.byteValue()) {
            return SOCKS5;
        }
        return UNKNOWN;
    }

    /**
     * Returns the value of the version field, as defined in the protocol specification.
     */
    public byte byteValue() {
        return b;
    }
}
