/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/**
 * The authentication method of SOCKS5.
 */
public class Socks5AuthMethod implements Comparable<Socks5AuthMethod> {

    public static final Socks5AuthMethod NO_AUTH = new Socks5AuthMethod(0x00, "NO_AUTH");
    public static final Socks5AuthMethod GSSAPI = new Socks5AuthMethod(0x01, "GSSAPI");
    public static final Socks5AuthMethod PASSWORD = new Socks5AuthMethod(0x02, "PASSWORD");

    /**
     * Indicates that the server does not accept any authentication methods the client proposed.
     */
    public static final Socks5AuthMethod UNACCEPTED = new Socks5AuthMethod(0xff, "UNACCEPTED");
    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5AuthMethod(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5AuthMethod(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks5AuthMethod valueOf(byte b) {
        return switch (b) {
            case 0x00 -> NO_AUTH;
            case 0x01 -> GSSAPI;
            case 0x02 -> PASSWORD;
            case (byte) 0xFF -> UNACCEPTED;
            default -> new Socks5AuthMethod(b);
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
        if (!(obj instanceof Socks5AuthMethod)) {
            return false;
        }

        return byteValue == ((Socks5AuthMethod) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5AuthMethod o) {
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
