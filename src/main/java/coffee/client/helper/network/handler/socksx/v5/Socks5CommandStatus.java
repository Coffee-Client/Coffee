/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/**
 * The status of {@link Socks5CommandResponse}.
 */
public class Socks5CommandStatus implements Comparable<Socks5CommandStatus> {

    public static final Socks5CommandStatus SUCCESS = new Socks5CommandStatus(0x00, "SUCCESS");
    public static final Socks5CommandStatus FAILURE = new Socks5CommandStatus(0x01, "FAILURE");
    public static final Socks5CommandStatus FORBIDDEN = new Socks5CommandStatus(0x02, "FORBIDDEN");
    public static final Socks5CommandStatus NETWORK_UNREACHABLE = new Socks5CommandStatus(0x03, "NETWORK_UNREACHABLE");
    public static final Socks5CommandStatus HOST_UNREACHABLE = new Socks5CommandStatus(0x04, "HOST_UNREACHABLE");
    public static final Socks5CommandStatus CONNECTION_REFUSED = new Socks5CommandStatus(0x05, "CONNECTION_REFUSED");
    public static final Socks5CommandStatus TTL_EXPIRED = new Socks5CommandStatus(0x06, "TTL_EXPIRED");
    public static final Socks5CommandStatus COMMAND_UNSUPPORTED = new Socks5CommandStatus(0x07, "COMMAND_UNSUPPORTED");
    public static final Socks5CommandStatus ADDRESS_UNSUPPORTED = new Socks5CommandStatus(0x08, "ADDRESS_UNSUPPORTED");
    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5CommandStatus(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5CommandStatus(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks5CommandStatus valueOf(byte b) {
        return switch (b) {
            case 0x00 -> SUCCESS;
            case 0x01 -> FAILURE;
            case 0x02 -> FORBIDDEN;
            case 0x03 -> NETWORK_UNREACHABLE;
            case 0x04 -> HOST_UNREACHABLE;
            case 0x05 -> CONNECTION_REFUSED;
            case 0x06 -> TTL_EXPIRED;
            case 0x07 -> COMMAND_UNSUPPORTED;
            case 0x08 -> ADDRESS_UNSUPPORTED;
            default -> new Socks5CommandStatus(b);
        };

    }

    public byte byteValue() {
        return byteValue;
    }

    public boolean isSuccess() {
        return byteValue == 0;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks5CommandStatus)) {
            return false;
        }

        return byteValue == ((Socks5CommandStatus) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5CommandStatus o) {
        return byteValue - o.byteValue;
    }

    @Override
    public String toString() {
        String text = this.text;
        if (text == null) {
            this.text = text = name + '(' + (byteValue & 0xFF) + ')';
        }
        return text;
    }
}
