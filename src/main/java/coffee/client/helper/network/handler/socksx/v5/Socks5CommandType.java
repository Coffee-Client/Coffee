/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/**
 * The type of {@link Socks5CommandRequest}.
 */
public class Socks5CommandType implements Comparable<Socks5CommandType> {

    public static final Socks5CommandType CONNECT = new Socks5CommandType(0x01, "CONNECT");
    public static final Socks5CommandType BIND = new Socks5CommandType(0x02, "BIND");
    public static final Socks5CommandType UDP_ASSOCIATE = new Socks5CommandType(0x03, "UDP_ASSOCIATE");
    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5CommandType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5CommandType(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks5CommandType valueOf(byte b) {
        return switch (b) {
            case 0x01 -> CONNECT;
            case 0x02 -> BIND;
            case 0x03 -> UDP_ASSOCIATE;
            default -> new Socks5CommandType(b);
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
        if (!(obj instanceof Socks5CommandType)) {
            return false;
        }

        return byteValue == ((Socks5CommandType) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5CommandType o) {
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
