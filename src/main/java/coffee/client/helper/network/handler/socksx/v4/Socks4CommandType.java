/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */
package coffee.client.helper.network.handler.socksx.v4;

import io.netty.util.internal.ObjectUtil;

/**
 * The type of {@link Socks4CommandRequest}.
 */
public class Socks4CommandType implements Comparable<Socks4CommandType> {

    public static final Socks4CommandType CONNECT = new Socks4CommandType(0x01, "CONNECT");
    public static final Socks4CommandType BIND = new Socks4CommandType(0x02, "BIND");
    private final byte byteValue;
    private final String name;
    private String text;

    public Socks4CommandType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks4CommandType(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks4CommandType valueOf(byte b) {
        return switch (b) {
            case 0x01 -> CONNECT;
            case 0x02 -> BIND;
            default -> new Socks4CommandType(b);
        };

    }

    public byte byteValue() {
        return byteValue;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks4CommandType)) {
            return false;
        }

        return byteValue == ((Socks4CommandType) obj).byteValue;
    }

    @Override
    public int compareTo(Socks4CommandType o) {
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
