/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */
package coffee.client.helper.network.handler.socksx.v4;

import io.netty.util.internal.ObjectUtil;

/**
 * The status of {@link Socks4CommandResponse}.
 */
public class Socks4CommandStatus implements Comparable<Socks4CommandStatus> {

    public static final Socks4CommandStatus SUCCESS = new Socks4CommandStatus(0x5a, "SUCCESS");
    public static final Socks4CommandStatus REJECTED_OR_FAILED = new Socks4CommandStatus(0x5b, "REJECTED_OR_FAILED");
    public static final Socks4CommandStatus IDENTD_UNREACHABLE = new Socks4CommandStatus(0x5c, "IDENTD_UNREACHABLE");
    public static final Socks4CommandStatus IDENTD_AUTH_FAILURE = new Socks4CommandStatus(0x5d, "IDENTD_AUTH_FAILURE");
    private final byte byteValue;
    private final String name;
    private String text;

    public Socks4CommandStatus(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks4CommandStatus(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks4CommandStatus valueOf(byte b) {
        return switch (b) {
            case 0x5a -> SUCCESS;
            case 0x5b -> REJECTED_OR_FAILED;
            case 0x5c -> IDENTD_UNREACHABLE;
            case 0x5d -> IDENTD_AUTH_FAILURE;
            default -> new Socks4CommandStatus(b);
        };

    }

    public byte byteValue() {
        return byteValue;
    }

    public boolean isSuccess() {
        return byteValue == 0x5a;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks4CommandStatus)) {
            return false;
        }

        return byteValue == ((Socks4CommandStatus) obj).byteValue;
    }

    @Override
    public int compareTo(Socks4CommandStatus o) {
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
