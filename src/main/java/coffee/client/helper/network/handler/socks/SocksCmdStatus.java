/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.network.handler.socks;

public enum SocksCmdStatus {
    SUCCESS((byte) 0x00),
    FAILURE((byte) 0x01),
    FORBIDDEN((byte) 0x02),
    NETWORK_UNREACHABLE((byte) 0x03),
    HOST_UNREACHABLE((byte) 0x04),
    REFUSED((byte) 0x05),
    TTL_EXPIRED((byte) 0x06),
    COMMAND_NOT_SUPPORTED((byte) 0x07),
    ADDRESS_NOT_SUPPORTED((byte) 0x08),
    UNASSIGNED((byte) 0xff);

    private final byte b;

    SocksCmdStatus(byte b) {
        this.b = b;
    }

    /**
     * @deprecated Use {@link #valueOf(byte)} instead.
     */
    @Deprecated
    public static SocksCmdStatus fromByte(byte b) {
        return valueOf(b);
    }

    public static SocksCmdStatus valueOf(byte b) {
        for (SocksCmdStatus code : values()) {
            if (code.b == b) {
                return code;
            }
        }
        return UNASSIGNED;
    }

    public byte byteValue() {
        return b;
    }
}
