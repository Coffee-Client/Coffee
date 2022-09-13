/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.network.handler.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/**
 * The status of {@link Socks5PasswordAuthResponse}.
 */
public class Socks5PasswordAuthStatus implements Comparable<Socks5PasswordAuthStatus> {

    public static final Socks5PasswordAuthStatus SUCCESS = new Socks5PasswordAuthStatus(0x00, "SUCCESS");
    public static final Socks5PasswordAuthStatus FAILURE = new Socks5PasswordAuthStatus(0xFF, "FAILURE");
    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5PasswordAuthStatus(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5PasswordAuthStatus(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public static Socks5PasswordAuthStatus valueOf(byte b) {
        return switch (b) {
            case 0x00 -> SUCCESS;
            case (byte) 0xFF -> FAILURE;
            default -> new Socks5PasswordAuthStatus(b);
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
        if (!(obj instanceof Socks5PasswordAuthStatus)) {
            return false;
        }

        return byteValue == ((Socks5PasswordAuthStatus) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5PasswordAuthStatus o) {
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
