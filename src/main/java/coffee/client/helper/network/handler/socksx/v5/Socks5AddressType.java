/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/**
 * The type of address in {@link Socks5CommandRequest} and {@link Socks5CommandResponse}.
 */
public class Socks5AddressType implements Comparable<Socks5AddressType> {

    public static final Socks5AddressType IPv4 = new Socks5AddressType(0x01, "IPv4");
    public static final Socks5AddressType DOMAIN = new Socks5AddressType(0x03, "DOMAIN");
    public static final Socks5AddressType IPv6 = new Socks5AddressType(0x04, "IPv6");
    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5AddressType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5AddressType(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks5AddressType valueOf(byte b) {
        return switch (b) {
            case 0x01 -> IPv4;
            case 0x03 -> DOMAIN;
            case 0x04 -> IPv6;
            default -> new Socks5AddressType(b);
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
        if (!(obj instanceof Socks5AddressType)) {
            return false;
        }

        return byteValue == ((Socks5AddressType) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5AddressType o) {
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
